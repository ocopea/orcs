// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice;

import javax.ws.rs.core.Application;

/**
 * Created with love by liebea on 6/18/2014.
 */
public interface MicroServiceApplication {

    Context getMicroServiceContext();

    Application getJaxRSApplication();
}
