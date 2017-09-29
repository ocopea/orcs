// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PersistentSchedulerImpl implements PersistentScheduler {

    private static final Logger log = LoggerFactory.getLogger(PersistentSchedulerImpl.class);
    private final TaskPersister persister;
    private ExecutorService threadPool;
    private final ConcurrentSkipListMap<Long, Task> tasks; // executionTime -> task
    private final Map<String, Consumer<String>> oneOffTaskConsumerMapping = new ConcurrentHashMap<>();
    private final Map<String, Function<String, Boolean>> recurringTaskFunctionMapping = new ConcurrentHashMap<>();
    private final Set<String> abortedTasks = new ConcurrentSkipListSet<>();
    private volatile long waitingUntil = Long.MAX_VALUE;
    private volatile boolean keepGoing = false;
    private Thread dispatchThread;
    private final Object lock = new Object();

    public PersistentSchedulerImpl(TaskPersister persister) {
        this.persister = persister;
        tasks = new ConcurrentSkipListMap<>(
                persister
                        .loadTasks()
                        .stream()
                        .collect(Collectors.toMap(Task::getExecutionTime, Function.identity()))
        );
    }

    @Override
    public void registerRecurringTask(String recurringTaskIdentifier, Function<String, Boolean> recurringTaskFunction) {
        if (keepGoing) {
            throw new IllegalStateException("task registrations should be completed prior to starting the scheduler");
        }
        if (recurringTaskFunctionMapping.containsKey(recurringTaskIdentifier)) {
            throw new IllegalArgumentException("a task with recurringTaskIdentifier=" + recurringTaskIdentifier +
                    " already registered");
        }
        log.info("registering recurring task identifier '" + recurringTaskIdentifier + "'");
        recurringTaskFunctionMapping.put(recurringTaskIdentifier, recurringTaskFunction);
    }

    @Override
    public void registerOneOffTask(String oneOffTaskIdentifier, Consumer<String> oneOffTaskConsumer) {
        if (keepGoing) {
            throw new IllegalStateException("task registrations should be completed prior to starting the scheduler");
        }
        if (oneOffTaskConsumerMapping.containsKey(oneOffTaskIdentifier)) {
            throw new IllegalArgumentException("a task with oneOffTaskIdentifier=" + oneOffTaskIdentifier +
                    " already registered");
        }
        log.info("registering one-off task identifier '" + oneOffTaskIdentifier + "'");
        oneOffTaskConsumerMapping.put(oneOffTaskIdentifier, oneOffTaskConsumer);
    }

    @Override
    public void scheduleOnce(
            String name,
            int delayInSeconds,
            String payload,
            String consumerIdentifier) {

        Consumer<String> taskConsumer = oneOffTaskConsumerMapping.get(consumerIdentifier);
        if (taskConsumer == null) {
            throw new IllegalArgumentException("consumerIdentifier=" + consumerIdentifier + " unknown to scheduler");
        }

        if (delayInSeconds <= 0) {
            throw new IllegalArgumentException("delayInSeconds must be positive. delayInSeconds=" + delayInSeconds);
        }

        long executionTime = System.currentTimeMillis() + delayInSeconds * 1000;
        Task task = persister.persistOneOffTask(name, executionTime, payload, consumerIdentifier);
        addTask(task);
    }

    @Override
    public String scheduleRecurring(
            String name,
            int intervalInSeconds,
            String payload,
            String functionIdentifier) {

        Function<String, Boolean> taskFunction = recurringTaskFunctionMapping.get(functionIdentifier);
        if (taskFunction == null) {
            throw new IllegalArgumentException("functionIdentifier=" + functionIdentifier + " is unknown to scheduler");
        }

        if (intervalInSeconds <= 0) {
            throw new IllegalArgumentException("intervalInSeconds must be positive. intervalInSeconds=" +
                    intervalInSeconds);
        }

        long executionTime = System.currentTimeMillis() + intervalInSeconds * 1000;
        Task task = persister.persistRecurringTask(
                name,
                executionTime,
                payload,
                functionIdentifier,
                intervalInSeconds);
        addTask(task);
        return task.getId();
    }

    @Override
    public void abortSchedule(String scheduleId) {
        log.info("aborting schedule. scheduleId=" + scheduleId);
        Task task = persister.deleteTask(scheduleId);
        if (task != null) {
            abortedTasks.add(scheduleId);
        }
    }

    @Override
    public synchronized void pause() {
        keepGoing = false;
        waitingUntil = Long.MAX_VALUE;
        try {
            if (dispatchThread != null) {
                synchronized (lock) {
                    lock.notifyAll();
                }
                dispatchThread.join();
                dispatchThread = null;
            }
        } catch (InterruptedException e) {
            // don't care
        }
    }

    @Override
    public synchronized void start() {
        log.info("starting persistent scheduler");
        keepGoing = true;
        waitingUntil = Long.MAX_VALUE;

        if (threadPool == null) {
            threadPool = Executors.newCachedThreadPool();
        }

        if (dispatchThread == null) {
            dispatchThread = new Thread(() -> {
                log.info("persistent scheduler dispatch thread started");
                while (keepGoing) {
                    try {
                        Task task = waitForTaskToExecute();
                        if (task == null) {
                            continue;
                        }
                        handleTask(task);
                    } catch (Exception ex) {
                        // catch all exceptions that might happen so that scheduler will always keep running
                        log.warn("exception thrown from scheduler loop", ex);
                    }
                }
                log.info("persistent scheduler dispatch thread exiting");
            });
            dispatchThread.setName("scheduler-dispatch");
            dispatchThread.start();
        }
    }

    /**
     * waits for the first task's execution time and returns it. the returned task is removed from the tasks map.
     * may return null if keepGoing was set to false.
     * otherwise, will block until a task is available, and then return it.
     */
    private Task waitForTaskToExecute() {
        Task task = null;
        synchronized (lock) {
            while (System.currentTimeMillis() < waitingUntil) {
                if (task != null) {
                    // we were awakened from wait before task execution time, that means a more urgent task was added,
                    // so put the current task back.
                    tasks.put(task.getExecutionTime(), task);
                    task = null;
                }

                if (!keepGoing) {
                    break;
                }

                Map.Entry<Long, Task> firstEntry = tasks.pollFirstEntry();
                long waitTime;
                if (firstEntry == null) {
                    log.debug("no tasks pending, will sleep for 5 seconds");
                    waitingUntil = Long.MAX_VALUE;
                    waitTime = 5000L;
                    task = null;
                } else {
                    task = firstEntry.getValue();
                    waitingUntil = task.getExecutionTime();
                    waitTime = task.getExecutionTime() - System.currentTimeMillis();
                    log.debug("wait {}ms til taskId={} is due. payload={}", waitTime, task.getId(), task.getPayload());
                }

                if (waitTime > 0) {
                    try {
                        lock.wait(waitTime);
                    } catch (InterruptedException e) {
                        // don't care
                        log.debug("interrupted while waiting for task");
                    }
                }
            }
        }
        return task;
    }

    @Override
    public synchronized void stop() {
        log.info("stopping task scheduler");
        pause();
        threadPool.shutdown();
        try {
            boolean terminated = threadPool.awaitTermination(10, TimeUnit.SECONDS);
            if (!terminated) {
                log.warn("timed out trying to terminate thread pool executor");
            }
        } catch (InterruptedException e) {
            log.warn("interrupted while trying to terminate thread pool executor", e);
        }
        threadPool = null;
    }

    private void addTask(Task task) {
        tasks.put(task.getExecutionTime(), task);
        if (task.getExecutionTime() < waitingUntil) {
            // may miss execution time if we don't wake up the executor thread
            if (dispatchThread != null) {
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }
        log.info("added task=" + task);
    }

    private void executeTask(Task task) {
        threadPool.submit(() -> {
            if (task instanceof RecurringTask) {
                executeRecurringTask((RecurringTask) task);
            } else {
                executeOneOffTask((OneOffTask) task);
            }
        });
    }

    private void handleTask(Task task) {
        String id = task.getId();
        if (abortedTasks.remove(id)) {
            log.debug("deleting aborted task from persistence. id=" + id);
            try {
                persister.deleteTask(id);
            } catch (Exception ex) {
                log.warn("failed deleting aborted task from persistence. task=" + task, ex);
            }
        } else {
            log.debug("executing task. id=" + id + " payload=" + task.getPayload());
            executeTask(task);
        }
        waitingUntil = Long.MAX_VALUE;
    }

    private void executeOneOffTask(OneOffTask task) {
        Consumer<String> taskConsumer = oneOffTaskConsumerMapping.get(task.getTaskConsumerIdentifier());
        if (taskConsumer == null) {
            log.warn("taskConsumerIdentifier=" + task.getTaskConsumerIdentifier() + " is not mapped to any class. " +
                    "not executing task=" + task);
        } else {
            try {
                taskConsumer.accept(task.getPayload());
            } catch (Exception e) {
                log.warn("scheduled task failed. task=" + task, e);
            }
        }
        log.debug("task handled, deleting from persistence. task=" + task);

        try {
            persister.deleteTask(task.getId());
        } catch (Exception ex) {
            log.warn("failed to delete task from persistence. task=" + task, ex);
        }
    }

    private void executeRecurringTask(RecurringTask task) {
        log.debug("executing recurring task. id=" + task.getId() + " payload=" + task.getPayload());
        long startTime = System.currentTimeMillis();

        // No need to persist recurring task occurrences. If we crash, the root task will be discovered and
        // resume running. However, we do need to persist the last execution time.
        boolean continueRecurring = true;

        Function<String, Boolean> taskFunction = recurringTaskFunctionMapping.get(task.getTaskFunctionIdentifier());
        if (taskFunction == null) {
            log.warn("taskFunctionIdentifier=" + task.getTaskFunctionIdentifier() + " is not mapped to any class. " +
                    "aborting task=" + task);
            continueRecurring = false;
        } else {
            try {
                continueRecurring = taskFunction.apply(task.getPayload());
            } catch (Exception e) {
                log.warn("recurring scheduled task failed. task=" + task, e);
            }
        }

        if (continueRecurring) {
            long nextExecutionTime = startTime + task.getIntervalSeconds() * 1000;
            Task updatedTask;
            try {
                updatedTask = persister.updateRecurringTaskNextExecutionTime(task.getId(), nextExecutionTime);

            } catch (Exception ex) {
                log.warn("failed to persist task's next execution time. task=" + task, ex);
                // adding next execution and hope to recover on the next occurrence.
                updatedTask = new RecurringTask(
                        task.getId(),
                        task.getName(),
                        nextExecutionTime,
                        task.getPayload(),
                        task.getTaskFunctionIdentifier(),
                        task.getIntervalSeconds());
            }
            addTask(updatedTask);
        } else {
            log.info("recurring task returned false, aborting. task=" + task);
            abortSchedule(task.getId());
        }
    }
}
