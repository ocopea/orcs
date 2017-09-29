// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.microservice.schedule;

import java.util.function.Function;

/**
 * Scheduler API is the API that any Scheduler implementation needs to implement in order
 * To be supported as part of the microservice library
 */
public interface SchedulerApi {

    /**
     * Starts the scheduler.
     * <p>
     * This method should be called <b>after</b> registering all recurrent tasks.
     */
    void start();

    /**
     * Stop the scheduler
     */
    void stop();

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
     * Create a persistent schedule for a task to recur execution at a specified time interval.
     * <p>
     * Execution time is best effort, meaning that a task may be executed later then scheduled due to resource stress
     * (for example, not enough executing threads). However, a task will never be executed too early.
     *
     * @param name a human-readable name for the task to be executed. May be attached to the thread executing the task
     *     for easier debugging. It is recommended, but not mandatory, that the name would be unique. May be null
     * @param intervalInSeconds Positive number of seconds the scheduler should wait between executions
     * @param payload a string the task can use as context when it is executed. May be null. Should not be huge.
     * @param functionIdentifier a String identifier for a {@code Function<String, Boolean>} to be executed when the
     *     task is due. The function should be registered for use using registerRecurringTask().
     *
     * @throws IllegalArgumentException if delayInSeconds is less than or equals to 0, or if functionIdentifier is
     *     unknown by the scheduler
     */
    void scheduleRecurring(
            String name,
            int intervalInSeconds,
            String payload,
            String functionIdentifier);
}
