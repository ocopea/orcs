// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class ServiceInstanceBindResult {

    private final int status;
    private final String statusMessage;
    private final String bindInfo;

    private ServiceInstanceBindResult() {
        this(0, null, null);
    }

    public ServiceInstanceBindResult(int status, String statusMessage, String bindInfo) {
        this.status = status;
        this.statusMessage = statusMessage;
        this.bindInfo = bindInfo;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getBindInfo() {
        return bindInfo;
    }
}
