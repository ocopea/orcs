// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This TaskPersister implementation uses memory to store tasks. this is mainly for use in tests and singleJar
 */
public class InMemoryTaskPersister implements TaskPersister {

    private Map<String, Task> tasks = new ConcurrentHashMap<>();

    @Override
    public Task persistOneOffTask(
            String name,
            long executionTime,
            String payload,
            String taskConsumerIdentifier) {
        Task task = new OneOffTask(UUID.randomUUID().toString(), name, executionTime, payload, taskConsumerIdentifier);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Task persistRecurringTask(
            String name,
            long executionTime,
            String payload,
            String taskFunctionIdentifier,
            int intervalSeconds) {
        String id = UUID.randomUUID().toString();
        Task task = new RecurringTask(id, name, executionTime, payload, taskFunctionIdentifier, intervalSeconds);
        tasks.put(id, task);
        return task;
    }

    @Override
    public Task updateRecurringTaskNextExecutionTime(String taskId, long executionTime) {
        Task task = tasks.get(taskId);

        if (task == null) {
            // task was already deleted from persistence
            return null;
        }

        if (!(task instanceof RecurringTask)) {
            throw new IllegalArgumentException("taskId=" + taskId + " does not belong to a recurring task");
        }

        RecurringTask recurringTask = (RecurringTask) task;
        Task updatedTask = new RecurringTask(
                recurringTask.getId(),
                recurringTask.getName(),
                executionTime,
                recurringTask.getPayload(),
                recurringTask.getTaskFunctionIdentifier(),
                recurringTask.getIntervalSeconds());

        // this will return null if the task was deleted while we were busy updating
        Task originalTask = tasks.replace(taskId, updatedTask);
        if (originalTask != null) {
            return updatedTask;
        } else {
            return null;
        }
    }

    @Override
    public Task deleteTask(String taskId) {
        return tasks.remove(taskId);
    }

    @Override
    public List<Task> loadTasks() {
        return new ArrayList<>(tasks.values());
    }
}
