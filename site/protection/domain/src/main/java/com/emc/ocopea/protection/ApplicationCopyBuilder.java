// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationCopyBuilder {
    private final UUID appInstanceId;
    private final Date timeStamp;

    // key = dsbURN-serviceId
    Map<String, ApplicationCopyScheduledEvent.DataServiceInfo> dataServiceCopyMap = new HashMap<>();

    // key = appName
    Map<String, ApplicationCopyScheduledEvent.AppServiceInfo> appServiceCopyMap = new HashMap<>();

    public ApplicationCopyBuilder(UUID appInstanceId, Date timeStamp) {
        this.appInstanceId = appInstanceId;
        this.timeStamp = timeStamp;
    }

    /**
     * Add a data service
     */
    public ApplicationCopyBuilder withDataService(
            String dsbUrn,
            String dsbUrl,
            String serviceId,
            String bindName,
            String facility,
            Map<String, String> dsbSettings) {
        dataServiceCopyMap.put(
                dsbUrn + '-' + serviceId,
                new ApplicationCopyScheduledEvent.DataServiceInfo(
                        dsbUrn,
                        dsbUrl,
                        serviceId,
                        bindName,
                        facility,
                        dsbSettings));
        return this;
    }

    /**
     * Add an app service
     */
    public ApplicationCopyBuilder withAppService(
            String psbUrn,
            String appServiceName,
            String appImageName,
            String appImageType,
            String appImageVersion) {
        appServiceCopyMap.put(
                appServiceName,
                new ApplicationCopyScheduledEvent.AppServiceInfo(
                        psbUrn,
                        appServiceName,
                        appImageName,
                        appImageType,
                        appImageVersion));
        return this;
    }

    ApplicationCopyScheduledEvent build() {
        return new ApplicationCopyScheduledEvent(
                UUID.randomUUID(),
                appInstanceId,
                1,
                new Date(),
                null,
                timeStamp,
                new ArrayList<>(dataServiceCopyMap.values()),
                new ArrayList<>(appServiceCopyMap.values()));
    }

}
