// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import com.emc.ocopea.util.PostgresUtil;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import com.emc.ocopea.util.database.NativeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PostgresTaskPersister implements TaskPersister {
    private static final Logger log = LoggerFactory.getLogger(PostgresTaskPersister.class);
    private final NativeQueryService nqs;
    private final String namespace;
    private final String schedulerTableFullName;

    public PostgresTaskPersister(DataSource dataSource, String schedulerSchemaName, String namespace) {
        this.namespace = namespace;
        this.nqs = new BasicNativeQueryService(dataSource);

        String sanitizedSchemaName = PostgresUtil.sanitizeIdentifier(schedulerSchemaName);
        String schedulerTableName = "scheduler_tasks";
        this.schedulerTableFullName = sanitizedSchemaName + "." + schedulerTableName;

        synchronized (PostgresTaskPersister.class) {

            if (!PostgresUtil.isSchemaExists(dataSource, sanitizedSchemaName)) {
                throw new IllegalStateException("schema " + sanitizedSchemaName + " does not exist");
            }

            if (!PostgresUtil.isTableExists(dataSource, sanitizedSchemaName, schedulerTableName)) {
                log.info("creating database table " + schedulerTableFullName + " for persistent scheduler tasks");
                nqs.executeUpdate(
                        "CREATE TABLE " + schedulerTableFullName + " (" +
                                "key UUID NOT NULL, " +
                                "namespace VARCHAR(1024) NOT NULL, " +
                                "task JSONB NOT NULL, " +
                                "CONSTRAINT pk_task_id PRIMARY KEY (key)" +
                                ")");
                nqs.executeUpdate(
                        "CREATE INDEX idx_tasks_namespace ON " + schedulerTableFullName + " (namespace)");
            }
        }
    }

    private Task loadTask(UUID key) {
        return nqs.getSingleValue(
                "SELECT * FROM " + schedulerTableFullName + " WHERE key=?",
                (rset, pos) -> {
                    try (Reader reader = rset.getCharacterStream("task")) {
                        return PostgresUtil.fromJsonB(reader, Task.class);
                    } catch (IOException e) {
                        throw new IllegalStateException("persisted task json could not be deserialized", e);
                    }
                },
                Collections.singletonList(key)
        );
    }

    private void persistTask(UUID key, Task task) {
        try {
            nqs.executeUpdate(
                    "INSERT INTO " + schedulerTableFullName + " (key,namespace,task) VALUES (?,?,?)",
                    Arrays.asList(key, namespace, PostgresUtil.objectToJsonBParameter(task, nqs))
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed persisting task=" + task, ex);
        }
    }

    @Override
    public Task persistOneOffTask(String name, long executionTime, String payload, String taskConsumerIdentifier) {
        final UUID key = UUID.randomUUID();
        OneOffTask task = new OneOffTask(key.toString(), name, executionTime, payload, taskConsumerIdentifier);
        persistTask(key, task);
        return task;
    }

    @Override
    public Task persistRecurringTask(
            String name,
            long executionTime,
            String payload,
            String functionIdentifier,
            int intervalSeconds) {
        final UUID key = UUID.randomUUID();
        RecurringTask task =
                new RecurringTask(key.toString(), name, executionTime, payload, functionIdentifier, intervalSeconds);
        persistTask(key, task);
        return task;
    }

    @Override
    public Task updateRecurringTaskNextExecutionTime(String taskId, long time) {
        UUID key = UUID.fromString(taskId);
        Task task = loadTask(key);
        task.setExecutionTime(time);
        try {
            nqs.executeUpdate(
                    "UPDATE " + schedulerTableFullName + " SET task=? WHERE key=?",
                    Arrays.asList(PostgresUtil.objectToJsonBParameter(task, nqs), key)
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed updating task=" + task, ex);
        }
        return task;
    }

    @Override
    public Task deleteTask(String taskId) {
        UUID key = UUID.fromString(taskId);
        Task deletedTask = loadTask(key);
        nqs.executeUpdate(
                "DELETE FROM " + schedulerTableFullName + " WHERE key=?",
                Collections.singletonList(key)
        );
        log.info("successfully deleted task=" + deletedTask);
        return deletedTask;
    }

    @Override
    @SuppressWarnings("unchecked") // TODO any better solution?
    public List<Task> loadTasks() {
        List<Task> tasks = nqs.getList(
                "SELECT * FROM " + schedulerTableFullName + " WHERE namespace=?",
                (rset, pos) -> {
                    try (Reader data = rset.getCharacterStream("task")) {
                        return PostgresUtil.fromJsonB(data, Task.class);
                    } catch (IOException e) {
                        throw new IllegalStateException("persisted task json could not be deserialized", e);
                    }
                },
                Collections.singletonList(namespace)
        );

        log.info("loaded " + tasks.size() + " tasks from persistence:");
        tasks.forEach(task -> log.info("\t" + task));
        return tasks;
    }
}
