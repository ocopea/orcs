// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import java.util.List;

/**
 * Defines an interface required for persistent task scheduler system in order to persist tasks
 */
public interface TaskPersister {

    /**
     * Stores a task
     */
    Task persistOneOffTask(
            String name,
            long executionTime,
            String payload,
            String taskConsumerIdentifier);

    Task persistRecurringTask(
            String name,
            long executionTime,
            String payload,
            String taskFunctionIdentifier,
            int intervalSeconds);

    /**
     * Updates the recurring task's executionTime and returns the updated task.
     *
     * @return the updated task, or null if no task with the specified id exists.
     *
     * @throws IllegalArgumentException if taskId belongs to a task which is not a recurring task.
     */
    Task updateRecurringTaskNextExecutionTime(String taskId, long time);

    /**
     * deletes a task from persistence
     *
     * @param taskId ID of the task to be deleted
     * @return the deleted task, or null if the task didn't exist
     */
    Task deleteTask(String taskId);

    /**
     * Loads tasks from persistence to memory
     */
    List<Task> loadTasks();
}
