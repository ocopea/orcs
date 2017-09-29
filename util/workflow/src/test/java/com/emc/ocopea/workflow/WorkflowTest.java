// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.workflow;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by liebea on 10/13/16.
 * Drink responsibly
 */
public class WorkflowTest {

    @Test
    public void testBasic() throws InterruptedException {

        WorkflowBuilder builder = new WorkflowBuilder("test workflow");
        StringHolder str = new StringHolder();
        final Task task2 = builder.createTask("add 2", new AppendStringTask(str, '2'));
        final Task task4 = builder.createTask("add 4", new AppendStringTask(str, '4'));
        final Task task3 = builder.createTask("add 3", new AppendStringTask(str, '3'));
        final Task task1 = builder.createTask("add 1", new AppendStringTask(str, '1'));
        final Task task5 = builder.createTask("add 5", new AppendStringTask(str, '5'));
        final Task task55 = builder.createTask("add 5", new AppendStringTask(str, '5'));
        final Task task33 = builder.createTask("add 3", new AppendStringTask(str, '3'));
        final Task task11 = builder.createTask("add 1", new AppendStringTask(str, '1'));
        final Task task44 = builder.createTask("add 4", new AppendStringTask(str, '4'));
        final Task task22 = builder.createTask("add 2", new AppendStringTask(str, '2'));

        builder.withDependecy(task1, task2, task22);
        builder.withDependecy(task2, task3, task33);
        builder.withDependecy(task3, task4, task44);
        builder.withDependecy(task4, task5, task55);
        builder.withDependecy(task11, task2, task22);
        builder.withDependecy(task22, task3, task33);
        builder.withDependecy(task33, task4, task44);
        builder.withDependecy(task44, task5, task55);

        final ExecutorService executor = Executors.newFixedThreadPool(5);
        final boolean[] validated = {false};
        new SimpleWorkflowExecutor(executor).execute(builder.build(), (success, message) -> {
            System.out.printf(str.getStr());
            Assert.assertEquals("1122334455", str.getStr());
            validated[0] = true;
            executor.shutdown();
        });

        executor.awaitTermination(20, TimeUnit.SECONDS);
        Assert.assertTrue(validated[0]);

    }

    private class StringHolder {
        private String str = "";

        String getStr() {
            return str;
        }

        synchronized void append(char ch) {
            str += ch;
        }
    }

    private class AppendStringTask implements TaskExecutor {
        private final char character;
        private final StringHolder str;

        private AppendStringTask(StringHolder str, char character) {
            this.str = str;
            this.character = character;
        }

        @Override
        public void execute() {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            str.append(character);
            System.out.println(System.currentTimeMillis() + " : " + str.getStr());
        }
    }
}
