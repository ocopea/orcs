// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

/**
 * Created by liebea on 1/12/15.
 * Drink responsibly
 */
public class LoggerConfig {
    private final String category;
    private final String level;

    // Required by  jackson
    @SuppressWarnings("UnusedDeclaration")
    private LoggerConfig() {
        this(null, null);
    }

    public LoggerConfig(String category, String level) {
        this.category = category;
        this.level = level;
    }

    public String getCategory() {
        return category;
    }

    public String getLevel() {
        return level;
    }
}
