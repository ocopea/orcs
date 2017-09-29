// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import com.emc.ocopea.util.MapBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistentSchedulerTest {
    private static AtomicInteger executionCounter = new AtomicInteger(0);
    private static Queue<ExecutedTask> executedTasks = new ConcurrentLinkedQueue<>();

    /*
    Tests to add:
    - tasks with non-zero work time
    - task is scheduled but aborted before scheduling time
    - task is scheduled, aborted after it has already run

    Bad path tests to add:
    - zero/negative interval
    - zero/negative delay
    - bad task execution class
    - task failures
     */

    private static final String MY_TASK_ID = "my-task";
    private static final String MY_RECURRING_TASK_ID = "my-recurring-task";
    private PersistentScheduler scheduler;
    private TaskPersister persister;

    private static String generateOneOffPayload(long expectedSecondsDelay, int expectedOrdinal, boolean shouldFail) {
        long expectedStartTime = System.currentTimeMillis() + 1000 * expectedSecondsDelay;
        return MapBuilder
                .<String, String>newHashMap()
                .with("expectedSecondsDelay", String.valueOf(expectedSecondsDelay))
                .with("expectedStartTime", String.valueOf(expectedStartTime))
                .with("expectedOrdinal", String.valueOf(expectedOrdinal))
                .with("shouldFail", String.valueOf(shouldFail))
                .build()
                .toString();
    }

    private static String generateRecurringPayload(int intervalInSeconds, int nOccurrences, boolean shouldFail) {
        return MapBuilder
                .<String, String>newHashMap()
                .with("intervalInSeconds", String.valueOf(intervalInSeconds))
                .with("nOccurrences", String.valueOf(nOccurrences))
                .with("schedulingTime", String.valueOf(System.currentTimeMillis()))
                .with("shouldFail", String.valueOf(shouldFail))
                .build()
                .toString();
    }

    private static Map<String, String> stringToMap(String mapString) {
        String[] entries = mapString.replace("{", "").replace("}", "").split(",");
        Map<String, String> map = new HashMap<>();
        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            map.put(keyValue[0].trim(), keyValue[1].trim());
        }
        return map;
    }

    private static void sleepNoException(long sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            // don't care
        }
    }

    @Before
    public void before() {
        executedTasks.clear();
        executionCounter.set(0);
        persister = new InMemoryTaskPersister();
        scheduler = initAndStartScheduler(persister);
    }

    private PersistentScheduler initAndStartScheduler(TaskPersister persister) {
        PersistentSchedulerImpl persistentScheduler = new PersistentSchedulerImpl(persister);
        persistentScheduler.registerRecurringTask(MY_RECURRING_TASK_ID, MyRecurringTask::apply);
        persistentScheduler.registerOneOffTask(MY_TASK_ID, MyTask::accept);
        persistentScheduler.start();
        return persistentScheduler;
    }

    /**
     * stops everything and perform assertions which are common to all tests
     */
    @After
    public void after() {
        scheduler.stop();
        scheduler = null;

        System.out.println("Number of executed tasks: " + executedTasks.size());
        System.out.println("executedTasks: " + executedTasks);
        for (ExecutedTask executedTask : executedTasks) {
            executedTask.assertExecutionMatchesExpectation();
        }
    }

    /**
     * Tests that a scheduled task executes on time
     */
    @Test
    public void testScheduleOnce() {
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 1, false), MY_TASK_ID);
        sleepNoException(2100);
        Assert.assertEquals(1, executionCounter.get());
    }

    /**
     * tests that tasks executes in the right order and time even if they were scheduled out of order
     */
    @Test
    public void testOutOfOrderScheduleOnce() {
        scheduler.scheduleOnce("task", 2, generateOneOffPayload(2, 2, false), MY_TASK_ID);
        scheduler.scheduleOnce("task", 3, generateOneOffPayload(3, 3, false), MY_TASK_ID);
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 1, false), MY_TASK_ID);

        sleepNoException(3100);
        Assert.assertEquals(3, executionCounter.get());
    }

    /**
     * tests that tasks execute in the right order and time when some task is waiting to be executed, and a more
     * urgent task is then queued. we expect the more urgent task to be executed first
     */
    @Test
    public void testOutOfOrderScheduleOnceWithPreemption() {
        scheduler.scheduleOnce("task", 3, generateOneOffPayload(3, 2, false), MY_TASK_ID);
        sleepNoException(1000);
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 1, false), MY_TASK_ID);

        sleepNoException(3100);
        Assert.assertEquals(2, executionCounter.get());
    }

    /**
     * scheduler is paused when a task is due. verifies that the task is executed when the scheduler resumes work
     */
    @Test
    public void testTaskIsDueWhenSchedulerPaused() {
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(2, 1, false), MY_TASK_ID);
        scheduler.pause();
        sleepNoException(2000);
        scheduler.start();
        sleepNoException(100);
        Assert.assertEquals(1, executionCounter.get());
    }

    /**
     * verifies that a completed task is removed from persistence, and wouldn't get executed again if restarted
     */
    @Test
    public void testCompletedTaskIsNotExecutedAgainAfterRestart() {
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 1, false), MY_TASK_ID);
        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());

        // simulate restart
        scheduler.stop();
        scheduler = initAndStartScheduler(persister);
        scheduler.start();

        // verify the completed task is not executed again
        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());
    }

    /**
     * verifies that recurring tasks execute on time, and stops executing when the task returns false
     */
    @Test
    public void testScheduleRecurring() {
        scheduler.scheduleRecurring("recurring-task", 1, generateRecurringPayload(1, 3, false), MY_RECURRING_TASK_ID);

        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());

        sleepNoException(1000);
        Assert.assertEquals(2, executionCounter.get());

        sleepNoException(1000);
        Assert.assertEquals(3, executionCounter.get());

        // make sure it stops executing after 3 times
        sleepNoException(2000);
        Assert.assertEquals(3, executionCounter.get());
    }

    /**
     * verifies that aborting a recurring task makes it stop recurring
     */
    @Test
    public void testScheduleRecurringWithAbort() {
        String scheduleId =
                scheduler.scheduleRecurring("t", 1, generateRecurringPayload(1, 300, false), MY_RECURRING_TASK_ID);
        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());

        sleepNoException(1000);
        Assert.assertEquals(2, executionCounter.get());

        scheduler.abortSchedule(scheduleId);
        sleepNoException(2000);
        Assert.assertEquals(2, executionCounter.get());
    }

    /**
     * verifies that a recurring schedule is resumed after restart
     */
    @Test
    public void testScheduleRecurringWithRestart() {
        scheduler.scheduleRecurring("t", 1, generateRecurringPayload(1, 4, false), MY_RECURRING_TASK_ID);

        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());

        sleepNoException(1000);
        Assert.assertEquals(2, executionCounter.get());

        // using the same persister with a new scheduler - simulates restart
        scheduler.stop();
        scheduler = initAndStartScheduler(persister);
        scheduler.start();
        sleepNoException(1000);
        Assert.assertEquals(3, executionCounter.get());

        sleepNoException(1000);
        Assert.assertEquals(4, executionCounter.get());

        // make sure it stops after 4 times
        sleepNoException(1000);
        Assert.assertEquals(4, executionCounter.get());
    }

    /**
     * verifies that a scheduled task is executed on time if a restart occurred while waiting for it
     */
    @Test
    public void testScheduleOnceWithRestart() {
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 1, false), MY_TASK_ID);

        // using the same persister with a new scheduler - simulates restart
        scheduler.stop();
        scheduler = initAndStartScheduler(persister);
        scheduler.start();
        sleepNoException(2000);

        Assert.assertEquals(1, executionCounter.get());
    }

    /**
     * verifies that no tasks are executed when the scheduler is paused
     */
    @Test
    public void testPause() {
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 1, false), MY_TASK_ID);
        scheduler.pause();
        sleepNoException(2000);
        Assert.assertEquals(0, executionCounter.get());
    }

    /**
     * performs concurrent random PersistentScheduler operations for 10 seconds to look for race conditions and
     * exceptions
     */
    @Test
    public void testConcurrentRandomOperations() throws InterruptedException {
        final boolean[] keepGoing = new boolean[1];
        keepGoing[0] = true;
        Random random = new Random();

        Thread taskAdder = new Thread(() -> {
            while (keepGoing[0]) {
                if (random.nextBoolean()) {
                    scheduler.scheduleOnce("task1", 1 + random.nextInt(10), "", MY_TASK_ID);
                }
                sleepNoException(random.nextInt(500));
            }
        });

        Thread taskAdder2 = new Thread(() -> {
            while (keepGoing[0]) {
                if (random.nextBoolean()) {
                    scheduler.scheduleOnce("task2", 1 + random.nextInt(10), "", MY_TASK_ID);
                }
                sleepNoException(random.nextInt(500));
            }
        });

        Thread recurringTaskAdder = new Thread(() -> {
            while (keepGoing[0]) {
                if (random.nextDouble() < 0.2) {
                    scheduler.scheduleRecurring("recurring", 1, "", MY_RECURRING_TASK_ID);
                }
                sleepNoException(random.nextInt(500));
            }
        });

        Thread pauser = new Thread(() -> {
            while (keepGoing[0]) {
                if (random.nextDouble() < 0.3) {
                    scheduler.pause();
                }
                sleepNoException(random.nextInt(500));
            }
        });

        Thread starter = new Thread(() -> {
            while (keepGoing[0]) {
                if (random.nextDouble() < 0.3) {
                    scheduler.start();
                }
                sleepNoException(random.nextInt(500));
            }
        });

        taskAdder.start();
        taskAdder2.start();
        recurringTaskAdder.start();
        pauser.start();
        starter.start();
        sleepNoException(10000);

        keepGoing[0] = false;
        taskAdder.join();
        pauser.join();
        starter.join();
        recurringTaskAdder.join();
    }

    /** tests that scheduler continues to run even if the task fails */
    @Test
    public void testFailingOneOffTask() throws Exception {
        scheduler.scheduleOnce(
                "task",
                1,
                generateOneOffPayload(1, 1, true),
                MY_TASK_ID);
        sleepNoException(2100);
        Assert.assertEquals(1, executionCounter.get());
    }

    /** tests that scheduler continues to run even if a recurring task fails */
    @Test
    public void testFailingRecurringTask() throws Exception {
        scheduler.scheduleRecurring("recurring-task", 1, generateRecurringPayload(1, 3, true), MY_RECURRING_TASK_ID);

        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());

        sleepNoException(1000);
        Assert.assertEquals(2, executionCounter.get());

        sleepNoException(1000);
        Assert.assertEquals(3, executionCounter.get());

        // make sure it stops executing after 3 times
        sleepNoException(2000);
        Assert.assertEquals(3, executionCounter.get());
    }

    @Test
    public void testSchedulerKeepsRunningWhenPersisterFailsUpdatingTaskExecutionTime() throws Exception {
        scheduler.stop();
        TaskPersister persisterSpy = Mockito.spy(new InMemoryTaskPersister());
        scheduler = initAndStartScheduler(persisterSpy);
        scheduler.start();

        scheduler.scheduleRecurring("recurring-task", 1, generateRecurringPayload(1, 3, false), MY_RECURRING_TASK_ID);

        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());

        Mockito
                .doThrow(SQLException.class)
                .when(persisterSpy)
                .updateRecurringTaskNextExecutionTime(Mockito.anyString(), Mockito.anyLong());

        sleepNoException(1000);
        Assert.assertEquals(2, executionCounter.get());


        sleepNoException(1000);
        Assert.assertEquals(3, executionCounter.get());

        // make sure it stops executing after 3 times
        sleepNoException(2000);
        Assert.assertEquals(3, executionCounter.get());
    }

    /** tests that tasks execute properly even if executed tasks are not deleted from persistence */
    @Test
    public void testSchedulerKeepsRunningWhenPersisterFailsDeletingTask() throws Exception {
        scheduler.stop();
        TaskPersister persisterSpy = Mockito.spy(new InMemoryTaskPersister());
        scheduler = initAndStartScheduler(persisterSpy);
        scheduler.start();

        Mockito.doThrow(SQLException.class).when(persisterSpy).deleteTask("1");
        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 1, false), MY_TASK_ID);
        sleepNoException(1100);
        Assert.assertEquals(1, executionCounter.get());

        scheduler.scheduleOnce("task", 1, generateOneOffPayload(1, 2, false), MY_TASK_ID);
        sleepNoException(1100);
        Assert.assertEquals(2, executionCounter.get());

        Assert.assertEquals(2, executionCounter.get());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScheduleOnceWithNonRegisteredTask() throws Exception {
        scheduler.scheduleOnce("task", 2, generateOneOffPayload(2, 2, false), "non-registered-task");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testScheduleRecurringWithNonRegisteredTask() throws Exception {
        scheduler.scheduleRecurring("recurring-task", 1, generateRecurringPayload(1, 3, false), "non-registered-task");
    }

    /**
     * helper test that verifies encode/decode of test tasks payload
     */
    @Test
    public void testExpectedTaskExecutionDetailsToStringFromString() {
        String originalPayload = generateOneOffPayload(12345L, 65432, false);
        ExpectedExecutionInfo decoded = ExpectedExecutionInfo.fromString(originalPayload);
        Assert.assertEquals(65432, decoded.getExpectedOrdinal());
        Assert.assertEquals(12345L, decoded.getExpectedSecondsDelay());
    }

    public static class MyTask {
        private static final org.slf4j.Logger log = LoggerFactory.getLogger(MyTask.class);

        public static void accept(String payload) {
            long actualStartTime = System.currentTimeMillis();
            int actualOrdinal = executionCounter.incrementAndGet();

            if (payload.isEmpty()) {
                log.debug("executing task with actualOrdinal=" + actualOrdinal +
                        " actualStartTime=" + actualStartTime);
            } else {
                ExpectedExecutionInfo expectedExecutionDetails = ExpectedExecutionInfo.fromString(payload);
                long startTimeDiff = actualStartTime - expectedExecutionDetails.getExpectedStartTime();
                log.debug("executing task with payload=" + payload +
                        ". actualOrdinal=" + actualOrdinal +
                        " actualStartTime=" + actualStartTime +
                        " startTimeDiff=" + startTimeDiff);

                executedTasks.add(
                        new ExecutedTask(
                                ExecutedTask.TaskType.ONE_OFF,
                                expectedExecutionDetails,
                                actualStartTime,
                                actualOrdinal
                        )
                );

                if (expectedExecutionDetails.isShouldFail()) {
                    throw new IllegalArgumentException("MyTask failed");
                }
            }
        }
    }

    public static class MyRecurringTask {
        private static final org.slf4j.Logger log = LoggerFactory.getLogger(MY_RECURRING_TASK_ID);

        public static boolean apply(String payload) {
            long actualStartTime = System.currentTimeMillis();
            int actualOrdinal = executionCounter.incrementAndGet();

            if (payload.isEmpty()) {
                System.out.println("executing recurring task with actualOrdinal=" + actualOrdinal +
                        " actualStartTime=" + actualStartTime);
                return true;
            } else {
                Map<String, String> info = stringToMap(payload);
                int nOccurrences = Integer.valueOf(info.get("nOccurrences"));
                Long schedulingTime = Long.valueOf(info.get("schedulingTime"));
                Long intervalInSeconds = Long.valueOf(info.get("intervalInSeconds"));
                boolean shouldFail = Boolean.valueOf(info.get("shouldFail"));
                log.debug("executing recurring task. nOccurrences=" + nOccurrences + " executionCounter=" +
                        actualOrdinal);

                executedTasks.add(new ExecutedTask(
                        ExecutedTask.TaskType.RECURRING,
                        new ExpectedExecutionInfo(
                                1,
                                schedulingTime + intervalInSeconds * actualOrdinal * 1000,
                                actualOrdinal, false),
                        actualStartTime,
                        actualOrdinal));

                if (shouldFail) {
                    if (executionCounter.get() == nOccurrences) {
                        return false;
                    }
                    throw new IllegalArgumentException("MyRecurringTask failed");
                }

                return executionCounter.get() != nOccurrences;
            }
        }
    }

    private static class ExpectedExecutionInfo {
        private final long expectedSecondsDelay;
        private final long expectedStartTime;
        private final int expectedOrdinal;
        private final boolean shouldFail;

        public ExpectedExecutionInfo(
                long expectedSecondsDelay,
                long expectedStartTime,
                int expectedOrdinal,
                boolean shouldFail) {
            this.expectedSecondsDelay = expectedSecondsDelay;
            this.expectedStartTime = expectedStartTime;
            this.expectedOrdinal = expectedOrdinal;
            this.shouldFail = shouldFail;
        }

        public static ExpectedExecutionInfo fromString(String expectedDetails) {
            Map<String, String> info = stringToMap(expectedDetails);
            Long expectedSecondsDelay = Long.valueOf(info.get("expectedSecondsDelay"));
            Long expectedStartTime = Long.valueOf(info.get("expectedStartTime"));
            Integer expectedOrdinal = Integer.valueOf(info.get("expectedOrdinal"));
            boolean shouldFail = Boolean.valueOf(info.getOrDefault("shouldFail", "false"));
            return new ExpectedExecutionInfo(expectedSecondsDelay, expectedStartTime, expectedOrdinal, shouldFail);
        }

        public long getExpectedSecondsDelay() {
            return expectedSecondsDelay;
        }

        public long getExpectedStartTime() {
            return expectedStartTime;
        }

        public int getExpectedOrdinal() {
            return expectedOrdinal;
        }

        public boolean isShouldFail() {
            return shouldFail;
        }

        @Override
        public String toString() {
            return "ExpectedExecutionInfo{" +
                    "expectedSecondsDelay=" + expectedSecondsDelay +
                    ", expectedStartTime=" + expectedStartTime +
                    ", expectedOrdinal=" + expectedOrdinal +
                    ", shouldFail=" + shouldFail +
                    '}';
        }
    }

    private static class ExecutedTask {
        private final TaskType taskType;
        private ExpectedExecutionInfo expectedExecutionInfo;
        private long startTime;
        private long ordinal;
        private long startTimeDiff;

        public ExecutedTask(
                TaskType taskType,
                ExpectedExecutionInfo expected,
                long startTime,
                long ordinal) {
            this.taskType = taskType;
            this.expectedExecutionInfo = expected;
            this.startTime = startTime;
            this.ordinal = ordinal;
            this.startTimeDiff = startTime - expected.getExpectedStartTime();
        }

        public void assertExecutionMatchesExpectation() {
            Assert.assertTrue(
                    "task executed prematurely. executedTask=" + this,
                    startTime >= expectedExecutionInfo.getExpectedStartTime());

            Assert.assertTrue(
                    "task executed too late (more than 100ms after it was supposed to run). executedTask=" + this,
                    startTime - expectedExecutionInfo.getExpectedStartTime() < 100);

            if (taskType.equals(TaskType.ONE_OFF)) {
                Assert.assertEquals(
                        "task executed in the wrong order relative to other tasks. executedTask=" + this,
                        expectedExecutionInfo.getExpectedOrdinal(),
                        ordinal);
            }
        }

        public enum TaskType {ONE_OFF, RECURRING}

        @Override
        public String toString() {
            return "ExecutedTask{" +
                    "taskType=" + taskType +
                    ", expectedExecutionInfo=" + expectedExecutionInfo +
                    ", startTime=" + startTime +
                    ", ordinal=" + ordinal +
                    ", startTimeDiff=" + startTimeDiff +
                    '}';
        }
    }
}
