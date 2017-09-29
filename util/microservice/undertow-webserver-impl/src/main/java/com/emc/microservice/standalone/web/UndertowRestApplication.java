// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.standalone.web;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;

import javax.ws.rs.core.Application;

/**
 * Created by liebea on 9/5/2014. Enjoy it
 */
public class UndertowRestApplication extends Application implements MicroServiceApplication {
    private final Context context;

    public UndertowRestApplication(Context context) {
        this.context = context;
    }

    @Override
    public Context getMicroServiceContext() {
        return context;
    }

    @Override
    public Application getJaxRSApplication() {
        return this;
    }
}
