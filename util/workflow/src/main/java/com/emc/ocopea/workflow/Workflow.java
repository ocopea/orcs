// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.workflow;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by liebea on 10/13/16.
 * Drink responsibly
 */
public class Workflow {
    private final String name;
    private final Map<Task, Set<Task>> tasks;
    private WorkflowState state = WorkflowState.created;

    Workflow(String name, Map<Task, Set<Task>> tasks) {
        this.name = name;
        this.tasks = tasks;
    }

    public String getName() {
        return name;
    }

    public Map<Task, Set<Task>> getTasks() {
        return tasks;
    }

    private boolean taskDone(Task task) {
        return task.getState() == TaskState.done;
    }

    private boolean taskReady(Task task) {
        return task.getState() == TaskState.queued &&
                tasks.get(task).stream().allMatch(this::taskDone);
    }

    public synchronized Set<Task> pullTasks() {
        return tasks.keySet()
                .stream()
                .filter(this::taskReady)
                .peek(task -> task.setState(TaskState.running))
                .collect(Collectors.toSet());
    }

    public synchronized void reportTaskFailed(Task task) {
        task.setState(TaskState.failed);
        this.state = WorkflowState.failed;
    }

    public synchronized void reportTaskDone(Task task) {
        task.setState(TaskState.done);
        if (tasks.keySet().stream().allMatch(this::taskDone)) {
            this.state = WorkflowState.done;
        }
    }

    public WorkflowState getState() {
        return state;
    }
}
