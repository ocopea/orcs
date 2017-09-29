// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2015-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 4/7/15.
 * Drink responsibly
 */
public class LoggingHelper {
    public static Logger createSubLogger(Logger microServiceLogger, Class loggerClass) {
        return LoggerFactory.getLogger(microServiceLogger.getName() + "." + loggerClass.getSimpleName());
    }
}
