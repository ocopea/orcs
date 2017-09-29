// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.workflow;

/**
 * Created by liebea on 10/13/16.
 * Drink responsibly
 */
public class Task {

    private final int id;
    private final String name;
    private final TaskExecutor executor;
    private TaskState state = TaskState.queued;

    Task(int id, String name, TaskExecutor executor) {
        this.id = id;
        this.name = name;
        this.executor = executor;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    TaskState getState() {
        return state;
    }

    void setState(TaskState state) {
        this.state = state;
    }

    public TaskExecutor getExecutor() {
        return executor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Task task = (Task) o;

        return id == task.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
