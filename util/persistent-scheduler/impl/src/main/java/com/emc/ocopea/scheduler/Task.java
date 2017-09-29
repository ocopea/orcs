// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "taskType")
@JsonSubTypes(
        {
                @JsonSubTypes.Type(value = RecurringTask.class, name = "RECURRING_TASK"),
                @JsonSubTypes.Type(value = OneOffTask.class, name = "ONE_OFF_TASK"),
        })
public class Task {
    private final String id;
    private final String name;
    private long executionTime;
    private final String payload;

    public Task(
            String id,
            String name,
            long executionTime,
            String payload) {
        this.id = id;
        this.name = name;
        this.executionTime = executionTime;
        this.payload = payload;
    }

    private Task() {
        this(null, null, 0L, null);
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public final String getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", executionTime=" + executionTime +
                ", payload='" + payload + '\'' +
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

        Task task = (Task) o;

        if (executionTime != task.executionTime) {
            return false;
        }
        if (id != null ? !id.equals(task.id) : task.id != null) {
            return false;
        }
        if (name != null ? !name.equals(task.name) : task.name != null) {
            return false;
        }
        return payload != null ? payload.equals(task.payload) : task.payload == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (executionTime ^ (executionTime >>> 32));
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        return result;
    }
}
