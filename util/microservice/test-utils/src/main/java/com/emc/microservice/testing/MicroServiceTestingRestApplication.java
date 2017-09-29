// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.testing;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;

import javax.ws.rs.core.Application;

/**
 * Created by liebea on 7/6/2014. Enjoy it
 */
public class MicroServiceTestingRestApplication extends Application implements MicroServiceApplication {
    private final Context context;

    public MicroServiceTestingRestApplication(Context context) {
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
