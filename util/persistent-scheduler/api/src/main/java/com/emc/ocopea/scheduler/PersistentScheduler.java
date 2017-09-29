// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import java.util.function.Consumer;
import java.util.function.Function;

public interface PersistentScheduler {

    /**
     * Registers a function to be used as a recurring schedule.
     * All function registrations must happen <b>before</b> the scheduler is started.
     * @param recurringTaskIdentifier an identifier to be associated with the function. Will be persisted, and
     *     therefore should not be changed after being set
     * @param recurringTaskFunction A function to be executed by the scheduler. The function will accept the String
     *     payload that is determined when calling scheduleRecurring, and return true when the schedule should continue
     *     executing, or false when the schedule should abort
     * @throws IllegalStateException if a task is registered after the scheduler has been started, or
     *     IllegalArgumentException if a task with this identifier already exists.
     */
    void registerRecurringTask(String recurringTaskIdentifier, Function<String, Boolean> recurringTaskFunction);

    /**
     * Registers a function to be used as a one-off schedule.
     * All function registrations must happen <b>before</b> the scheduler is started.
     * @param oneOffTaskIdentifier an identifier to be associated with the function. Will be persisted, and
     *     therefore should not be changed after being set
     * @param oneOffTaskConsumer A consumer to be executed by the scheduler. The consumer will accept the String
     *     payload that is determined when calling scheduleOnce
     * @throws IllegalStateException if a task is registered after the scheduler has been started, or
     *     IllegalArgumentException if a task with this identifier already exists.
     */
    void registerOneOffTask(String oneOffTaskIdentifier, Consumer<String> oneOffTaskConsumer);

    /**
     * Create a persistent schedule for a task to be executed at a specified time in the future.
     * <p>
     * The schedule is persistent, meaning that if the scheduler is restarted, tasks which were scheduled before the
     * restart would still be executed (on time if possible, otherwise as soon as possible).
     * If the scheduler was not active (e.g. system was down or scheduler was paused/stopped) when a task was
     * due for execution, the task will be executed once the scheduler resumes activity (e.g. the system starts again
     * or the scheduler was started).
     * Execution time is best effort, meaning that a task may be executed later then scheduled due to resource stress
     * (for example, not enough executing threads). However, a task will never be executed too early.
     * Tasks are guaranteed to execute at least once. Therefore, tasks should be idempotent in case of multiple
     * executions.
     *
     * @param name a human-readable name for the task to be executed. May be attached to the thread executing the task
     *     for easier debugging. It is recommended, but not mandatory, that the name would be unique. May be null
     * @param delayInSeconds Positive number of seconds the scheduler should wait before executing the task
     * @param payload a string the task can use as context when it is executed. May be null. Should not be huge.
     * @param consumerIdentifier a String that identifies the {@code Consumer<String>} to be executed when the task
     *     is due. Scheduler should know the mapping between identifiers and consumers when starting up
     *     (either by initialization or by specific implementation that has an internal mapping).
     *
     * @throws IllegalArgumentException if delayInSeconds is less than or equals to 0, or if consumerIdentifier is
     *     unknown to scheduler
     */
    void scheduleOnce(
            String name,
            int delayInSeconds,
            String payload,
            String consumerIdentifier);

    /**
     * Create a persistent schedule for a task to recur execution at a specified time interval.
     * <p>
     * The schedule is persistent, meaning that if the scheduler is restarted, tasks which were scheduled before the
     * restart would still be executed (on time if possible, otherwise as soon as possible).
     * If the scheduler is not active (e.g. system was down or scheduler was paused/stopped) when a task is
     * due for execution, the missed execution times are dropped, and the task will resume recurring once the scheduler
     * resumes activity (e.g. the system starts again or the scheduler was started). Occurrences will happen at
     * intervals relative to the first execution after restart.
     * Execution time is best effort, meaning that a task may be executed later then scheduled due to resource stress
     * (for example, not enough executing threads). However, a task will never be executed too early.
     *
     * @param name a human-readable name for the task to be executed. May be attached to the thread executing the task
     *     for easier debugging. It is recommended, but not mandatory, that the name would be unique. May be null
     * @param intervalInSeconds Positive number of seconds the scheduler should wait between executions
     * @param payload a string the task can use as context when it is executed. May be null. Should not be huge.
     * @param functionIdentifier a String identifier for a {@code Function<String, Boolean>} to be executed when the
     *     task is due. The function will use the String payload argument and return {@code true} if the schedule
     *     should continue recurring, or {@code false} if the schedule should stop.
     *
     * @return a unique identifier to be used for aborting the schedule
     *
     * @throws IllegalArgumentException if delayInSeconds is less than or equals to 0, or if functionIdentifier is
     *     unknown by the scheduler
     */
    String scheduleRecurring(
            String name,
            int intervalInSeconds,
            String payload,
            String functionIdentifier);

    /**
     * Aborts a schedule
     * <p>
     * Abort a schedule from running in the future. Abortion is persistent, so that an aborted schedule will not
     * resume running if the scheduler is restarted.
     *
     * @param scheduleId an identifier of the schedule, obtained from a previous call to the "scheduleRecurring"
     *     methods. If no task with the specified scheduleId is found (because it was already aborted, or because it
     *     never existed), this method has no effect.
     */
    void abortSchedule(String scheduleId);

    /**
     * Pauses the scheduler such that new tasks may be scheduled, but no task will be executed until started again.
     * <p>
     * Tasks that should have been executed while the scheduler was paused will be executed when the scheduler is
     * started again.
     * If the scheduler was already paused/stopped, this method has no effect.
     */
    void pause();

    /**
     * Starts the scheduler.
     * <p>
     * This method should be called after instantiating a scheduler, or to resume a scheduler's work after it has
     * been stopped or paused.
     * Once a persistent scheduler has been started, it should load any schedules found in the database and start
     * executing them in order and on time (as guaranteed in the scheduleXXX methods).
     * If the scheduler was already started, this method has no effect.
     */
    void start();

    /**
     * Stops the scheduler and frees resources such that new tasks may be scheduled, but no task will be executed until
     * started again.
     * <p>
     * This method is similar to the {@link #pause()} method, but it also frees up resources taken by the scheduler:
     * it may destroy threads, free used memory, etc.
     * If the scheduler was not started, this method has no effect.
     * It is illegal to start the scheduler after stopping it.
     */
    void stop();
}
