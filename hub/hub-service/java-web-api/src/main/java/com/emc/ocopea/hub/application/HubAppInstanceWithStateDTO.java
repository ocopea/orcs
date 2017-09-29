// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liebea on 1/24/16.
 * Drink responsibly
 */
public class HubAppInstanceWithStateDTO extends HubAppInstanceConfigurationDTO {
    private final String webEntryPointURL;
    private final String state;
    private final String stateMessage;

    private HubAppInstanceWithStateDTO() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public HubAppInstanceWithStateDTO(
            UUID id,
            String name,
            UUID appTemplateId,
            UUID siteId,
            UUID baseSavedImageId,
            UUID creatorUserId,
            String deploymentType,
            Date created,
            String webEntryPointURL,
            String state,
            String stateMessage) {
        super(id, name, appTemplateId, siteId, baseSavedImageId, creatorUserId, deploymentType, created);
        this.webEntryPointURL = webEntryPointURL;
        this.state = state;
        this.stateMessage = stateMessage;
    }

    public String getWebEntryPointURL() {
        return webEntryPointURL;
    }

    public String getState() {
        return state;
    }

    public String getStateMessage() {
        return stateMessage;
    }

    @Override
    public String toString() {
        return "HubAppInstanceWithStateDTO{" +
                "webEntryPointURL='" + webEntryPointURL + '\'' +
                ", state='" + state + '\'' +
                ", stateMessage='" + stateMessage + '\'' +
                '}' + super.toString();
    }
}
