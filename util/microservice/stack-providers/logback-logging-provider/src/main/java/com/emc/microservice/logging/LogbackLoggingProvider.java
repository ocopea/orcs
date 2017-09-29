// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.logging;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by liebea on 3/6/17.
 * Drink responsibly
 */
public class LogbackLoggingProvider implements LoggingProvider {

    @Override
    public void setLogLevel(LogLevel level, String category, Map<String, String> variables) {
        ch.qos.logback.classic.Logger theLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                category == null ? ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME : category);

        switch (level) {
            case DEBUG:
                theLogger.setLevel(Level.DEBUG);
                break;
            case ERROR:
                theLogger.setLevel(Level.ERROR);
                break;
            case TRACE:
                theLogger.setLevel(Level.TRACE);
                break;
            case INFO:
                theLogger.setLevel(Level.INFO);
                break;
            case WARN:
                theLogger.setLevel(Level.WARN);
                break;
            default:
                theLogger.setLevel(Level.ERROR);
        }

    }
}
