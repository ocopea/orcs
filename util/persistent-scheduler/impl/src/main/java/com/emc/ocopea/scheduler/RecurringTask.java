// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

public class RecurringTask extends Task {
    private final int intervalSeconds;
    private String taskFunctionIdentifier;

    public RecurringTask(
            String id,
            String name,
            long executionTime,
            String payload,
            String taskFunctionIdentifier,
            int intervalSeconds) {
        super(id, name, executionTime, payload);
        this.taskFunctionIdentifier = taskFunctionIdentifier;
        this.intervalSeconds = intervalSeconds;
    }

    private RecurringTask() {
        this(null, null, 0L, null, null, 0);
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public String getTaskFunctionIdentifier() {
        return taskFunctionIdentifier;
    }

    @Override
    public String toString() {
        return "RecurringTask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", executionTime=" + getExecutionTime() +
                ", payload='" + getPayload() + '\'' +
                ", intervalSeconds=" + intervalSeconds +
                ", taskFunctionIdentifier=" + taskFunctionIdentifier +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        RecurringTask task = (RecurringTask) o;

        if (intervalSeconds != task.intervalSeconds) {
            return false;
        }
        return taskFunctionIdentifier.equals(task.taskFunctionIdentifier);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + intervalSeconds;
        result = 31 * result + taskFunctionIdentifier.hashCode();
        return result;
    }
}
