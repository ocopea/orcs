// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.schedule;

import com.emc.microservice.Context;
import com.emc.microservice.ContextThreadLocal;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageReader;
import com.emc.microservice.resource.AbstractManagedResource;
import com.emc.microservice.serialization.SerializationManager;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.microservice.schedule.SchedulerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ManagedSchedulerImpl
        extends AbstractManagedResource<SchedulerDescriptor, SchedulerConfiguration>
        implements ManagedScheduler {
    private static final Logger logger = LoggerFactory.getLogger(ManagedSchedulerImpl.class);
    private final SchedulerApi schedulerApi;
    private final Map<String, Class<? extends ScheduleListener>> listenerClassMappings; // listenerId --> listenerClass
    private final Map<String, ScheduleListener> listenerMappings = new ConcurrentHashMap<>(); // name --> listener
    private final Context context;
    private final SerializationManager serializationManager;
    private final String schedulerTaskIdentifier;

    protected ManagedSchedulerImpl(
            SchedulerDescriptor descriptor,
            SchedulerConfiguration configuration,
            SchedulerApi schedulerApi,
            Map<String, Class<? extends ScheduleListener>> listenerClassMappings,
            Context context,
            SerializationManager serializationManager) {
        super(descriptor, configuration);
        this.listenerClassMappings = listenerClassMappings;
        this.context = context;
        this.serializationManager = serializationManager;
        this.schedulerTaskIdentifier = context.getMicroServiceBaseURI() + "|" +
                ManagedSchedulerRecurringTask.RECURRING_TASK_IDENTIFIER;

        this.schedulerApi = schedulerApi;
        this.schedulerApi.registerRecurringTask(
                schedulerTaskIdentifier,
                this.new ManagedSchedulerRecurringTask()::run
        );
        this.schedulerApi.start();
    }

    @Override
    public void create(String name, int intervalInSeconds, String listenerIdentifier) {
        create(name, intervalInSeconds, listenerIdentifier, Collections.emptyMap());
    }

    @Override
    public void create(
            String name,
            int intervalInSeconds,
            String listenerIdentifier,
            Map<String, String> headers) {
        create(name, intervalInSeconds, listenerIdentifier, headers, null, null);
    }

    @Override
    public <T> void create(
            String name,
            int intervalInSeconds,
            String listenerIdentifier,
            Map<String, String> headers,
            Class<T> payloadClass,
            T payload) {

        if (!listenerClassMappings.containsKey(listenerIdentifier)) {
            throw new IllegalArgumentException("listenerIdentifier=" + listenerIdentifier + " is unknown");
        }

        if (intervalInSeconds < 0) {
            throw new IllegalArgumentException("intervalInSeconds=" + intervalInSeconds + " must be non-negative");
        }

        if (listenerMappings.containsKey(name)) {
            throw new IllegalStateException("a schedule with name=" + name + " was already created");
        }

        String payloadObjectString = null;
        if (payload != null) {
            payloadObjectString = buildStringFromClass(payloadClass, payload);
        }

        SchedulerMessage message = new SchedulerMessage(name, listenerIdentifier, headers, payloadObjectString);

        schedulerApi.scheduleRecurring(
                name,
                intervalInSeconds,
                buildStringFromClass(SchedulerMessage.class, message),
                schedulerTaskIdentifier
        );
    }

    private ScheduleListener initScheduleListener(Class<? extends ScheduleListener> c) {
        try {
            final ScheduleListener scheduleListener = c.newInstance();
            doIfLifecycle(scheduleListener, serviceLifecycle -> serviceLifecycle.init(context));
            return scheduleListener;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed instantiating schedule listener " + c.getSimpleName(), e);
        }
    }

    private <T> T objectFromString(Class<T> clazz, String s) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))) {
            return serializationManager.getReader(clazz).readObject(in);
        } catch (IOException e1) {
            throw new IllegalStateException("Failed reading scheduled message object", e1);
        }
    }

    private <T> String buildStringFromClass(Class<T> payloadClass, T payload) {
        String payloadObjectString;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            serializationManager.getWriter(payloadClass).writeObject(payload, outputStream);
            payloadObjectString = outputStream.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            throw new IllegalStateException("Failed serializing payload ", e);
        }
        return payloadObjectString;
    }

    private void doIfLifecycle(Object o, Consumer<ServiceLifecycle> consumer) {
        if (o instanceof ServiceLifecycle) {
            consumer.accept((ServiceLifecycle) o);
        }
    }

    /**
     * A general wrapper task to be used with SchedulerApi. All recurring schedules that are scheduled are wrapped
     * using this task. The ScheduleListener identifier to be used and the original payload are passed to
     * the scheduler itself. Once the schedule is due, this task will parse the payload and call the listener's onTick
     * method.
     */
    private class ManagedSchedulerRecurringTask {
        // Do not change this identifier as it may be persisted in deployed systems
        public static  final String RECURRING_TASK_IDENTIFIER = "managed-scheduler-recurring-task";

        /** parses the payload and calls the listener */
        public boolean run(String payload) {
            ContextThreadLocal.setContext(context);

            SchedulerMessage schedulerMessage = objectFromString(SchedulerMessage.class, payload);

            String listenerIdentifier = schedulerMessage.getListenerIdentifier();
            Class<? extends ScheduleListener> listenerClass = listenerClassMappings.get(listenerIdentifier);
            if (listenerClass == null) {
                logger.warn("couldn't find listener class for message name={} listenerIdentifier={}",
                        schedulerMessage.getName(), listenerIdentifier);

                // abort this schedule
                return false;
            }

            ScheduleListener scheduleListener = listenerMappings.computeIfAbsent(
                    schedulerMessage.getName(),
                    name -> initScheduleListener(listenerClass));

            return scheduleListener.onTick(new Message() {
                @Override
                public String getMessageHeader(String headerName) {
                    return schedulerMessage.getHeaders().get(headerName);
                }

                @Override
                public Map<String, String> getMessageHeaders() {
                    return schedulerMessage.getHeaders();
                }

                @Override
                public String getContextValue(String key) {
                    return null;
                }

                @Override
                public Map<String, String> getMessageContext() {
                    return null;
                }

                @Override
                public void readMessage(MessageReader messageReader) {
                    try (ByteArrayInputStream in = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8))) {
                        messageReader.read(in);
                    } catch (IOException e1) {
                        throw new IllegalStateException("Failed reading scheduled message", e1);
                    }
                }

                @Override
                public <T> T readObject(Class<T> format) {
                    return objectFromString(format, schedulerMessage.getPayload());
                }

                @Override
                public Object getUnderlyingMessageObject() {
                    return schedulerMessage;
                }
            });
        }
    }
}
