// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * This computer code is copyright 2014 - 2015 EMC Corporation
 * All rights reserved
 */
package com.emc.dpa.timer;

import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.microservice.metrics.StopWatch;
import com.emc.microservice.metrics.TimerMetric;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Timer provides a way to run jobs regularly. It supports and pausing all jobs.
 *
 * @author shresa
 */
public class Timer {

    private static final String METRIC_TAG_JOB_NAME = "jobName";

    private boolean paused = false;
    private boolean quit = false;
    private final Map<String, Job> jobs = new HashMap<>();
    private final PriorityBlockingQueue<Job> jobQueue = new PriorityBlockingQueue<>(11, (left, right) -> {
        long diff = left.nextRun - right.nextRun;
        if (diff == 0) {
            return 0;
        }

        if (diff < 0) {
            return -1;
        }
        return 1;
    });
    private final Executor executor = Executors.newCachedThreadPool(new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "timer." + name + "." + System.currentTimeMillis());
        }
    });
    private final MetricsRegistry registry;
    private final String name;

    /**
     * Create a timer.
     *
     * @param name Name of the timer
     * @param registry Metric registry
     */
    public Timer(String name, MetricsRegistry registry) {
        Objects.requireNonNull(name, "Micro service timer name cannot be empty.");
        Objects.requireNonNull(registry, "Metrics registry cannot be empty.");
        this.name = name;
        this.registry = registry;
    }

    /**
     * Add a timer task.
     *
     * @param name Name of the timer task. The task must be unique for each timer.
     * @param runnable Actual task as runnable.
     * @param seconds Task interval in seconds. Next run is end time of task + interval.
     */
    public void add(String name, Runnable runnable, int seconds, Date nextRun) {
        Job job = new Job(name, runnable, seconds);
        jobs.put(name, job);
        job.setRunNow();
        jobQueue.add(job);
    }

    /**
     * Add a timer task.
     *
     * @param name Name of the timer task. The task must be unique for each timer.
     * @param runnable Actual task as runnable.
     * @param seconds Task interval in seconds. Next run is end time of task + interval.
     */
    public void add(String name, Runnable runnable, int seconds) {
        Job job = new Job(name, runnable, seconds);
        jobs.put(name, job);
        job.setRunNow();
        jobQueue.add(job);
    }

    /**
     * Remove a timer task
     *
     * @param name Name of the timer task to remove
     */
    public void remove(String name) {
        jobs.remove(name);
    }

    /**
     * Returns a list of timer task names.
     */
    public Collection<String> list() {
        return jobs.keySet();
    }

    /**
     * Pause the timer. This pauses all timer tasks. Tasks currently active are
     * allowed to continue.
     */
    public void pause() {
        paused = true;
    }

    /**
     * Resume the timer. If the timer is paused it resumes. The timer tasks will
     * continue with existing interval and next run.
     */
    public void resume() {
        paused = false;
    }

    /**
     * Stop the time
     */
    public void stop() {
        quit = true;
    }

    /**
     * Start the timer
     */
    public void start() {
        // using worker for main runner thread
        executor.execute(this::process);
    }

    private void process() {
        while (!quit) {
            try {
                Job job = jobQueue.poll(10, TimeUnit.SECONDS);
                if (job != null && jobs.containsKey(job.getName())) {
                    if (isTimeToRun(job) && !paused) {
                        runNow(job);
                    }

                    job.updateNextRun();
                    jobQueue.add(job);
                }
            } catch (InterruptedException e) {
                //swallowing
            }
        }
    }

    private boolean isTimeToRun(Job job) {
        long diff = job.getNextRun() - System.currentTimeMillis();
        while (diff > 0) {
            try {
                Thread.sleep(diff);
                diff = job.getNextRun() - System.currentTimeMillis();
            } catch (InterruptedException err) {
                // ignored
            }
        }

        return true;
    }

    private void runNow(Job job) {
        try (StopWatch watch = getMetricTimer(job).getStopWatch()) {
            executor.execute(job.getRunnable());
        }
    }

    private TimerMetric getMetricTimer(Job job) {
        Map<String, String> metricTags = new HashMap<>();
        metricTags.put(METRIC_TAG_JOB_NAME, job.getName());
        return registry.getTimerMetric(name, metricTags, Timer.class);
    }

    private static class Job {

        private final String name;
        private final Runnable runnable;
        private final int interval;

        private long nextRun;

        public Job(String name, Runnable runnable, int interval, long nextRun) {
            this.name = name;
            this.runnable = runnable;
            this.interval = interval;
            this.nextRun = nextRun;
        }

        Job(String name, Runnable runnable, int interval) {
            this(name, runnable, interval, System.currentTimeMillis());
        }

        String getName() {
            return name;
        }

        Runnable getRunnable() {
            return runnable;
        }

        int getInterval() {
            return interval;
        }

        long getNextRun() {
            return nextRun;
        }

        void setRunNow() {
            nextRun = -1;
        }

        void updateNextRun() {
            nextRun = System.currentTimeMillis() + (interval * 1000);
        }
    }
}
