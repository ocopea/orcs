// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.workflow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by liebea on 10/13/16.
 * Drink responsibly
 */
public class WorkflowBuilder {
    private final String name;
    private final Map<Task, Set<Task>> tasks = new HashMap<>();
    private int idSequence = 1;

    public WorkflowBuilder(String name) {
        this.name = name;
    }

    public Task createTask(String name, TaskExecutor executor) {
        final Task task = new Task(idSequence++, name, executor);
        tasks.put(task, new HashSet<>());
        return task;
    }

    public WorkflowBuilder withDependecy(Task doThisTask, Task... beforeThisTask) {
        Arrays.asList(beforeThisTask)
                .forEach(task -> tasks.get(task).add(doThisTask));
        return this;
    }

    public Workflow build() {
        return new Workflow(name, tasks);
    }
}
