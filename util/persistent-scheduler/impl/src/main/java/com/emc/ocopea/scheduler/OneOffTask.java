// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

public class OneOffTask extends Task {
    private final String taskConsumerIdentifier;

    public OneOffTask(
            String id,
            String name,
            long executionTime,
            String payload,
            String taskConsumerIdentifier) {
        super(id, name, executionTime, payload);
        this.taskConsumerIdentifier = taskConsumerIdentifier;
    }

    private OneOffTask() {
        this(null, null, 0L, null, null);
    }

    public String getTaskConsumerIdentifier() {
        return taskConsumerIdentifier;
    }

    @Override
    public String toString() {
        return "OneOffTask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", executionTime=" + getExecutionTime() +
                ", payload='" + getPayload() + '\'' +
                ", taskConsumerIdentifier=" + taskConsumerIdentifier +
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

        OneOffTask task = (OneOffTask) o;

        return taskConsumerIdentifier.equals(task.taskConsumerIdentifier);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + taskConsumerIdentifier.hashCode();
        return result;
    }
}
