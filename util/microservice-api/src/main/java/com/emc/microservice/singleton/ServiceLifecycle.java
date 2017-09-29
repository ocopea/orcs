// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */

package com.emc.microservice.singleton;

import com.emc.microservice.Context;

/**
 * Created by liebea on 3/22/15.
 * Drink responsibly
 */
public interface ServiceLifecycle {

    void init(Context context);

    void shutDown();

    default void pause() {
    }

    default void resume() {
    }
}
