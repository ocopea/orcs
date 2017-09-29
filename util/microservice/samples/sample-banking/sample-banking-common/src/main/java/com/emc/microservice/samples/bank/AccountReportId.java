// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

/**
 * Created by liebea on 11/19/14.
 * Drink responsibly
 */
public class AccountReportId {
    private final String nameSpace;
    private final String key;

    public AccountReportId() {
        this(null, null);
    }

    public AccountReportId(String nameSpace, String key) {
        this.nameSpace = nameSpace;
        this.key = key;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public String getKey() {
        return key;
    }
}
