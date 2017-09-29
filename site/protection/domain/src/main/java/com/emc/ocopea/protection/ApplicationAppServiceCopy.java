// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.Map;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationAppServiceCopy {
    private final String psbURN;
    private final String appServiceName;
    private final String appImageName;
    private final String appImageType;
    private final String appImageVersion;
    private ApplicationDataServiceCopyState state;
    private String stateMessage;
    private Date stateTimestamp;
    private Map<String, String> appConfiguration = null;

    public ApplicationAppServiceCopy(String psbURN, String appServiceName, String appImageName, 
                                     String appImageType, String appImageVersion, 
                                     ApplicationDataServiceCopyState state, String stateMessage, 
                                     Date stateTimestamp) {
        this.psbURN = psbURN;
        this.appServiceName = appServiceName;
        this.appImageName = appImageName;
        this.appImageType = appImageType;
        this.appImageVersion = appImageVersion;
        this.state = state;
        this.stateMessage = stateMessage;
        this.stateTimestamp = stateTimestamp;
    }

    public void setAppConfiguration(Map<String, String> appConfiguration) {
        this.appConfiguration = appConfiguration;
    }

    void setState(ApplicationDataServiceCopyState state, Date timestamp, String stateMessage) {
        this.state = state;
        this.stateMessage = stateMessage;
        this.stateTimestamp = timestamp;
    }

    public String getPsbURN() {
        return psbURN;
    }

    public String getAppServiceName() {
        return appServiceName;
    }

    public String getAppImageName() {
        return appImageName;
    }

    public String getAppImageType() {
        return appImageType;
    }

    public String getAppImageVersion() {
        return appImageVersion;
    }

    public ApplicationDataServiceCopyState getState() {
        return state;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    public Date getStateTimestamp() {
        return stateTimestamp;
    }

    public Map<String, String> getAppConfiguration() {
        return appConfiguration;
    }
}
