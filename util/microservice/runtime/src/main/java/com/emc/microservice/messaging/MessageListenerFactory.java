// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;

import java.util.function.Consumer;

/**
 * Created with love by liebea on 5/28/2014.
 */
public abstract class MessageListenerFactory {

    /***
     * Instantiate message listener
     */
    public static <T extends MessageListener> T createMessageListener(Class<T> messageListenerClazz, Context context) {
        try {
            // Instantiating class
            //noinspection unchecked
            return (T) doIfLifecycle(
                    messageListenerClazz.newInstance(),
                    serviceLifecycle -> serviceLifecycle.init(context));
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Failed instantiating " + messageListenerClazz.getCanonicalName(), e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed initializing " + messageListenerClazz.getCanonicalName(), e);
        }
    }

    private static Object doIfLifecycle(Object o, Consumer<ServiceLifecycle> consumer) {
        if (o instanceof ServiceLifecycle) {
            consumer.accept((ServiceLifecycle) o);
        }
        return o;
    }
}
