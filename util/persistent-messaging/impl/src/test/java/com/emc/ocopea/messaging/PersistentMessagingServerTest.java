// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.messaging;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by liebea on 4/2/17.
 * Drink responsibly
 */
public class PersistentMessagingServerTest {

    @Test
    public void testMessageBeingConsumedWithMediumBuffer() throws InterruptedException {
        testConsumeWithBuffer(5);
    }

    @Test
    public void testMessageBeingConsumedWithSmallestBuffer() throws InterruptedException {
        testConsumeWithBuffer(2);
    }

    @Test
    public void testMessageBeingConsumedWithLargeBuffer() throws InterruptedException {
        testConsumeWithBuffer(30);
    }

    @Test
    public void testCrashRecovery() throws InterruptedException {
        final InMemoryMessagePersister persister = new InMemoryMessagePersister();
        PersistentMessagingServer connection = new PersistentMessagingServer(persister);
        connection.createQueue("q1", 1000, 0, 1);
        connection.start();

        sendStringMessage(connection, "1");
        sendStringMessage(connection, "2");
        sendStringMessage(connection, "3");
        connection.pause();

        Connection connectionAfterCrash = new PersistentMessagingServer(persister);
        connectionAfterCrash.createQueue("q1", 1000, 0, 1);
        AtomicInteger count = new AtomicInteger(0);
        connectionAfterCrash.subscribe("q1", "Saruman", message -> count.incrementAndGet());
        connectionAfterCrash.start();
        Thread.sleep(100);
        Assert.assertEquals(3, count.get());
    }

    private void sendStringMessage(
            Connection connection,
            String messageText) {
        sendStringMessage(connection, messageText, "q1", Collections.emptyMap());
    }

    private void sendStringMessage(
            Connection connection,
            String messageText,
            String queueName,
            Map<String, String> headers
    ) {
        connection.sendMessage(queueName, new StringBufferInputStream(messageText), headers);
    }

    @Test
    public void testRestart1() throws InterruptedException {
        Connection connection = createConnectionWithQueue(1000, 2, 1);
        final AtomicInteger count = setupConsumer(connection, "mukaka1");

        sendStringMessage(connection, "momo");
        // Letting it fail once
        Thread.sleep(500);

        // restart
        connection.pause();
        connection.start();
        Thread.sleep(2500);
        Assert.assertEquals(2, count.get());
    }

    @Test
    public void testRandomPauseStart() throws InterruptedException {
        AtomicInteger count = new AtomicInteger(0);
        final int sleepingBound = 50;
        final AtomicBoolean keepGoing = new AtomicBoolean(true);
        AtomicBoolean paused = new AtomicBoolean(false);
        Set<Integer> receivedMessages = new ConcurrentSkipListSet<>();
        final long seed = System.currentTimeMillis();
        System.out.println("testRandomPauseStart seed: " + seed);
        final Random random = new Random(seed);

        Connection connection = createConnectionWithQueue(1000, 2, 0);
        final Consumer<Message> consumer = message -> {
            final Integer messageNumber = Integer.valueOf(messageToString(message));
            receivedMessages.add(messageNumber);
            if (paused.get()) {
                System.out.println("race condition met: processing message " + messageNumber + " while queue is " +
                        "paused!");
            }

            if (random.nextInt(100) < 10) { // simulate 10% failures
                throwExWithNoStackTrace(message);
            }
        };

        IntStream.range(1,10).forEach((i) -> connection.subscribe("q1", "c" + i, consumer));

        Thread senderThread = new Thread(() -> {
            while (keepGoing.get()) {
                sleepNoException(random.nextInt(sleepingBound));
                sendMessage(connection, String.valueOf(count.incrementAndGet()), "q1");
            }
        });

        Thread senderThread2 = new Thread(() -> {
            while (keepGoing.get()) {
                sleepNoException(random.nextInt(sleepingBound));
                sendMessage(connection, String.valueOf(count.incrementAndGet()), "q1");
            }
        });

        Thread pauseStartThread = new Thread(() -> {
            while (keepGoing.get()) {
                long sleep = random.nextInt(sleepingBound);
                sleepNoException(sleep);
                if (sleep % 2 == 0) {
                    connection.start();
                    paused.set(false);
                } else {
                    connection.pause();
                    paused.set(true);
                }
            }
        });

        senderThread.start();
        senderThread2.start();
        pauseStartThread.start();
        sleepNoException(15000);
        keepGoing.set(false);

        senderThread.join();
        senderThread2.join();
        pauseStartThread.join();

        System.out.println("starting connection to drain messages");
        paused.set(false);
        connection.start();

        int attempts = 0;
        while (attempts < 60) {
            int prevCount = count.get();
            sleepNoException(1000);
            if (prevCount == count.get()) {
                break;
            }
            attempts += 1;
        }
        Assert.assertEquals(count.get(), receivedMessages.size());
    }

    private void sleepNoException(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // pass
        }
    }

    @Test
    public void testRestart2() throws InterruptedException {
        Connection connection = createConnectionWithQueue(1000, 2, 1);
        final AtomicInteger count = setupConsumer(connection, "mukaka2");

        sendStringMessage(connection, "momo");
        // pause before it has failed
        Thread.sleep(100);

        // restart
        connection.pause();
        connection.start();
        Thread.sleep(2500);
        Assert.assertEquals(2, count.get());

    }

    @Test
    public void testPause() throws InterruptedException {
        Connection connection = createConnectionWithQueue(1000, 2, 1);
        AtomicInteger count = createSomeMessyQueues(connection);

       // just pausing
        Thread.sleep(55L);
        connection.pause();
        int countRightAfterPause = count.get();
        System.out.println(countRightAfterPause);
        Thread.sleep(100L);
        System.out.println(count.get());
        int countNow = count.get();
        Assert.assertTrue("Messages should be drained from the queue", countNow > countRightAfterPause);
        Thread.sleep(100L);
        Assert.assertEquals("Messages should stopped being consumed as the queue is paused now", countNow, count.get());
        Assert.assertTrue(countNow > 0 && countNow < 10000);
    }

    private AtomicInteger createSomeMessyQueues(Connection connection) {
        AtomicInteger count = new AtomicInteger(0);
        final Consumer<Message> consumer = message -> {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count.incrementAndGet();
        };

        IntStream.range(0,30).forEach((i) -> connection.subscribe("q1", "Saruman" + i, consumer));

        IntStream.range(0,10000).parallel().forEach(
                (i) -> sendStringMessage(connection, String.valueOf(i)));
        return count;
    }

    @Test
    public void testUnsubscribe() throws InterruptedException {
        Connection connection = createConnectionWithQueue(1000, 2, 1);
        AtomicInteger count = createSomeMessyQueues(connection);

        Thread.sleep(55L);

        // Un-subscribing
        IntStream.range(0,30).forEach((i) -> connection.unsubscribe("q1", "Saruman" + i));
        int countRightAfterPause = count.get();
        System.out.println(countRightAfterPause);
        Thread.sleep(100L);
        System.out.println(count.get());
        int countNow = count.get();
        Assert.assertTrue("Messages should be drained from the queue until finished un-subscribing", countNow >
                countRightAfterPause);
        Thread.sleep(100L);
        Assert.assertEquals("Messages should stopped being consumed as the queue is has no subscribersnow", countNow,
                count.get());
        Assert.assertTrue(countNow > 0 && countNow < 10000);
    }


    private AtomicInteger setupConsumer(Connection connection, final String someThingToPrint) {
        final AtomicInteger count = new AtomicInteger(0);
        connection.subscribe("q1", "c1", message -> {
            System.out.println(someThingToPrint + " " + count.get());
            if (count.getAndIncrement() == 0) {
                try {
                    Thread.sleep(200);
                    System.out.println(someThingToPrint + " bye " + count.get());
                    throwExWithNoStackTrace(message);
                } catch (InterruptedException e) {
                    //haha
                }
            }else {
                System.out.println(someThingToPrint + " bye " +  + count.get());
            }

        });
        return count;
    }

    @Test
    public void testRetry() throws InterruptedException {
        Connection connection = createConnectionWithQueue(1000, 1, 0);
        rollThoseMessages(10, connection, 30, 1, 10);
        connection = createConnectionWithQueue(10, 1, 0);
        rollThoseMessages(10, connection, 30, 1, 10);
        connection = createConnectionWithQueue(10, 2, 0);
        rollThoseMessages(1, connection, 30, 1, 10);
        connection = createConnectionWithQueue(30, 2, 0);
        rollThoseMessages(5, connection, 30, 2, 10);
    }

    @Test
    public void testPauseAndRestart() throws InterruptedException {
        PersistentMessagingServer connection = createConnectionWithQueue(1000, 2, 0);
        rollThoseMessages(10, connection, 30, 2, 100, () -> {
            connection.pause();
            connection.start();
        }, false);
    }

    private void testConsumeWithBuffer(int bufferSize) throws InterruptedException {
        testMessagesConsumption(1, bufferSize);
        testMessagesConsumption(2, bufferSize);
        testMessagesConsumption(3, bufferSize);
        testMessagesConsumption(5, bufferSize);
        testMessagesConsumption(10, bufferSize);
    }

    private void testMessagesConsumption(int numberOfConsumers, int memoryBufferSize)
            throws InterruptedException {

        PersistentMessagingServer connection = createConnectionWithQueue(memoryBufferSize);

        rollThoseMessages(numberOfConsumers, connection, 30, 0, 10);
    }

    private void rollThoseMessages(
            int numberOfConsumers,
            Connection connection,
            int secondsToWait,
            int consumerFailures,
            int numberOfMessages) throws InterruptedException {
        rollThoseMessages(numberOfConsumers, connection, secondsToWait, consumerFailures, numberOfMessages, null, true);
    }

    private void rollThoseMessages(
            int numberOfConsumers,
            Connection connection,
            int secondsToWait,
            int consumerFailures,
            int numberOfMessages,
            Runnable somethingToDoInTheMiddleOfThings,
            boolean expectingExactlyOnce
    ) throws InterruptedException {

        Assert.assertTrue(
                "for this test method I require numberOfMessages to be even and larger than 2. this is my " +
                        "testing method, so if you don't like that - don't use it",
                numberOfMessages % 2 == 0 && numberOfMessages > 2);
        List<HelloConsumer> consumers = new ArrayList<>(numberOfConsumers);
        final ConcurrentHashMap<String, AtomicInteger> failures = new ConcurrentHashMap<>();
        for (int cIdx = 0; cIdx < numberOfConsumers; cIdx++) {
            final String consumerName = "c" + cIdx;
            final HelloConsumer c = new HelloConsumer(consumerName, consumerFailures, failures);
            consumers.add(c);
            connection.subscribe("q1", consumerName, c);
        }

        // Making senders run in parallel - first half
        final int halfTheMessages = numberOfMessages / 2;
        IntStream.range(0, halfTheMessages).parallel().forEach(
                (i) -> sendMessage(connection, "hello", "q1")
        );

        // Allowing the test to do funky stuff in the middle of things
        if (somethingToDoInTheMiddleOfThings != null) {
            somethingToDoInTheMiddleOfThings.run();
        }

        // Making senders run in parallel - second half
        IntStream.range(halfTheMessages, numberOfMessages).parallel().forEach(
                (i) -> sendMessage(connection, "hello", "q1")
        );

        // Letting stuff work trying to see if it is done
        final long startedPolling = System.currentTimeMillis();
        int sum = consumers.stream().mapToInt(HelloConsumer::getNumberOfConsumedMessages).sum();
        while (sum < numberOfMessages) {
            // Checking timeout
            if (System.currentTimeMillis() - startedPolling > secondsToWait * 1000) {
                break;
            }
            Thread.sleep(100);
            sum = consumers.stream().mapToInt(HelloConsumer::getNumberOfConsumedMessages).sum();
        }

        // Should consume in total n messages. for some scenarios because we guarantee "at least once" we can get the
        // same message twice. this is fine
        if (expectingExactlyOnce) {
            Assert.assertEquals(numberOfMessages, sum);
        } else {
            Assert.assertTrue("got " + sum, sum >= numberOfMessages);
        }

        // Messages should split somehow, it doesn't matter so much how..
        consumers.forEach((c) -> Assert.assertTrue(c.getNumberOfConsumedMessages() > 0));

        // We verify that all messages were delivered to consumers (avoid counting same message twice as delivered
        Assert.assertEquals("Not all messages have been processed!", numberOfMessages, failures.keySet().size());
    }

    private PersistentMessagingServer createConnectionWithQueue(int memoryBufferSize) {
        return createConnectionWithQueue(memoryBufferSize, 2, 1);
    }

    private PersistentMessagingServer createConnectionWithQueue(
            int memoryBufferSize,
            int reties,
            int secondsToSleepBetweenMessageRetries) {
        PersistentMessagingServer connection = new PersistentMessagingServer(new InMemoryMessagePersister());
        connection.createQueue("q1", memoryBufferSize, secondsToSleepBetweenMessageRetries, reties);
        connection.start();
        return connection;
    }

    private void sendMessage(Connection connection, String messageContent, String queueName) {
        sendStringMessage(connection, messageContent, queueName, Collections.singletonMap("h1", "hv1"));
    }

    private class HelloConsumer implements Consumer<Message> {
        private final String consumerName;
        private int numberOfConsumedMessages = 0;
        private int numberOfFailuresToCause;
        private final ConcurrentMap<String, AtomicInteger> processedMessagesToFailures;

        public HelloConsumer(
                String consumerName,
                int numberOfFailuresToCause,
                ConcurrentMap<String, AtomicInteger> processedMessagesToFailures) {
            this.consumerName = consumerName;
            this.numberOfFailuresToCause = numberOfFailuresToCause;
            this.processedMessagesToFailures = processedMessagesToFailures;
        }

        @Override
        public void accept(Message message) {
            final AtomicInteger failuresForMessage = processedMessagesToFailures.computeIfAbsent(
                    message.getId(), (id) -> new AtomicInteger(numberOfFailuresToCause));
            //System.out.println(consumerName + ":" + message.getId() + ":" + failuresForMessage.get());
            if (failuresForMessage.get() == 0) {
                final String result = messageToString(message);
                numberOfConsumedMessages++;
                Assert.assertEquals("hello", result);
            } else {
                failuresForMessage.decrementAndGet();
                throwExWithNoStackTrace(message);
            }
        }

        public int getNumberOfConsumedMessages() {
            return numberOfConsumedMessages;
        }
    }

    private void throwExWithNoStackTrace(Message message) {
        final RuntimeException exception = new RuntimeException("Ha ha ha - take that shpanMessaging! failed " +
                "processing message with id: " + message.getId());
        exception.setStackTrace(new StackTraceElement[0]);
        throw exception;
    }

    private static String messageToString(Message message) {
        final String[] result = new String[1];
        message.readMessage(inputStream -> result[0] = new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n")));
        return result[0];
    }
}
