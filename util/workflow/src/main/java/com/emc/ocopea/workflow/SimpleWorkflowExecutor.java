// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Created by liebea on 10/13/16.
 * Drink responsibly
 */
public class SimpleWorkflowExecutor {
    private static final Logger log = LoggerFactory.getLogger(SimpleWorkflowExecutor.class);
    private final Executor executor;

    public SimpleWorkflowExecutor(Executor executor) {
        this.executor = executor;
    }

    public void execute(Workflow workflow, WorkflowExecutionListener listener) {
        pullTasks(workflow, listener);
    }

    private void pullTasks(Workflow workflow, WorkflowExecutionListener listener) {
        workflow.pullTasks().forEach(task ->
                executor.execute(() -> {
                    doTask(workflow, listener, task);
                }));
    }

    private void doTask(Workflow workflow, WorkflowExecutionListener listener, Task task) {
        try {
            task.getExecutor().execute();
            workflow.reportTaskDone(task);
            if (workflow.getState() == WorkflowState.done) {
                listener.workflowDone(true, null);
            } else {
                pullTasks(workflow, listener);
            }
        } catch (Exception ex) {
            log.error("failed executing workflow " + workflow.getName(), ex);
            workflow.reportTaskFailed(task);
            listener.workflowDone(false, ex.getMessage());
        }
    }

    public interface WorkflowExecutionListener {
        void workflowDone(boolean success, String message);
    }

}
