// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import com.emc.ocopea.util.PostgresUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.postgresql.util.PGobject;

import java.lang.reflect.Field;
import java.util.UUID;

public class TaskTest {

    @Before
    public void before() throws Exception {
        Field isTestMode = PostgresUtil.class.getDeclaredField("isTestMode");
        isTestMode.setAccessible(true);
        isTestMode.set(null, false);
    }

    @Test
    public void serializeDeserializeTaskTest() throws Exception {
        Task task = new Task(UUID.randomUUID().toString(), "task", System.currentTimeMillis(), "payload");
        Object serializedTask = PostgresUtil.objectToJsonBParameter(task, null);
        Task deserializedTask = PostgresUtil.fromJsonB(((PGobject) serializedTask).getValue(), Task.class);
        Assert.assertEquals(task, deserializedTask);
    }

    @Test
    public void serializeDeserializeOneOffTaskTest() throws Exception {
        String id = UUID.randomUUID().toString();
        OneOffTask task = new OneOffTask(id, "task", System.currentTimeMillis(), "payload", "consumer");
        Object serializedTask = PostgresUtil.objectToJsonBParameter(task, null);
        Task deserializedTask = PostgresUtil.fromJsonB(((PGobject) serializedTask).getValue(), Task.class);
        Assert.assertEquals(task, deserializedTask);
    }

    @Test
    public void serializeDeserializeRecurringTaskTest() throws Exception {
        String id = UUID.randomUUID().toString();
        Task task = new RecurringTask(id, "task", System.currentTimeMillis(), "payload", "function", 5);
        Object serializedTask = PostgresUtil.objectToJsonBParameter(task, null);
        Task deserializedTask = PostgresUtil.fromJsonB(((PGobject) serializedTask).getValue(), Task.class);
        Assert.assertEquals(task, deserializedTask);
    }
}
