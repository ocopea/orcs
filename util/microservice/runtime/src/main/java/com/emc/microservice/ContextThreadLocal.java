// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice;

/**
 * Created by liebea on 11/21/16.
 * Drink responsibly
 */
public class ContextThreadLocal extends ThreadLocal<Context> {
    private static final ContextThreadLocal instance = new ContextThreadLocal();

    public static void setContext(Context context) {
        instance.set(context);
    }

    public static Context getContext() {
        return instance.get();
    }
}
