// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */
package com.emc.dpa.timer;

import com.emc.microservice.metrics.MetricsRegistryImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shresa
 */
public class TimerTest {
    private static final String TASK2 = "task2";
    private static final String TASK1 = "task1";

    private MetricsRegistryImpl registry;
    private Timer timer;

    private Map<String, List<String>> sensing;

    @Before
    public void setup() {
        registry = new MetricsRegistryImpl("test.registry");
        timer = new Timer("test", registry);
        sensing = new HashMap<>();

        addTwoTasks();
    }

    @Test
    public void testThatTimerSupportsAddingTasks() {
        Assert.assertTrue(timer.list().contains(TASK1));
        Assert.assertTrue(timer.list().contains(TASK2));
    }

    @Test
    public void testThatTimerSupportsRemovingTasks() {
        Assert.assertTrue(timer.list().contains(TASK1));
        timer.remove(TASK1);
        Assert.assertFalse(timer.list().contains(TASK1));
    }

    @Test
    public void testThatTasksRunCorrectNumberOfTimes() throws Exception {
        timer.start();
        Thread.sleep(5 * 1000);

        Assert.assertEquals(3, sensing.get(TASK1).size());
        Assert.assertEquals(2, sensing.get(TASK2).size());
    }

    @Test
    public void testThatMetricsIsUpdated() throws Exception {
        timer.start();
        Thread.sleep(5 * 1000);

        Assert.assertEquals(2, registry.getRegistry().getTimers().size());
    }

    private void addTwoTasks() {
        timer.add(TASK1, getTask(TASK1), 2);
        timer.add(TASK2, getTask(TASK2), 3);
    }

    private Runnable getTask(final String name) {
        final List<String> holder = new ArrayList<>();
        sensing.put(name, holder);
        return new Runnable() {
            @Override
            public void run() {
                holder.add(name);
            }
        };
    }
}
