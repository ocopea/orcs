// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 10/26/16.
 * Drink responsibly
 */
public class ApplicationDataServiceCopy {
    private final String dsbUrn;
    private final String dsbUrl;
    private final String serviceId;
    private final String bindName;
    private final String facility;
    private final Map<String, String> dsbSettings;
    private ApplicationDataServiceCopyState state;
    private String stateMessage;
    private Date stateTimestamp;
    private UUID copyId = null;
    private String copyRepoURN = null;

    public ApplicationDataServiceCopy(
            String dsbUrn,
            String dsbUrl,
            String serviceId,
            String bindName,
            String facility,
            Map<String, String> dsbSettings,
            ApplicationDataServiceCopyState state,
            String stateMessage,
            Date stateTimestamp) {
        this.dsbUrn = dsbUrn;
        this.dsbUrl = dsbUrl;
        this.serviceId = serviceId;
        this.bindName = bindName;
        this.facility = facility;
        this.dsbSettings = dsbSettings;
        this.state = state;
        this.stateMessage = stateMessage;
        this.stateTimestamp = stateTimestamp;

    }

    public String getDsbUrn() {
        return dsbUrn;
    }

    public String getDsbUrl() {
        return dsbUrl;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getBindName() {
        return bindName;
    }

    public String getFacility() {
        return facility;
    }

    public Map<String, String> getDsbSettings() {
        return dsbSettings;
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

    public UUID getCopyId() {
        return copyId;
    }

    public void setCopyId(String copyRepoURN, UUID copyId) {
        this.copyRepoURN = copyRepoURN;
        this.copyId = copyId;
    }

    void setState(ApplicationDataServiceCopyState state, Date timestamp, String stateMessage) {
        this.state = state;
        this.stateMessage = stateMessage;
        this.stateTimestamp = timestamp;
    }

    public String getCopyRepoURN() {
        return copyRepoURN;
    }
}
