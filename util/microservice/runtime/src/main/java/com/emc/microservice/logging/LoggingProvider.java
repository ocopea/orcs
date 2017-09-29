// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.logging;

import java.util.Map;

/**
 * Created by liebea on 11/21/16.
 * Drink responsibly
 */
public interface LoggingProvider {

    enum LogLevel {
        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    /***
     * Set the log level
     * @param level log level
     * @param category optional - category to set logger level for
     * @param variables optional - context variables allowing setting specific loggers to the desired level
     *                             the variables can be matched with context specific key-values to enable
     *                             more granular logger settings and is implementation specific
     */
    void setLogLevel(LogLevel level, String category, Map<String, String> variables);
}
