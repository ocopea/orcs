// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.demo.dsb.h2;

import java.util.Date;
import java.util.Map;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class H2Instance {
    private final String name;
    private final String originCopyId;
    private final Date creationTime;
    private final boolean readonly;
    private final Map<String, String> dsbSettings;
    private Long size = null;

    public H2Instance(
            String name,
            String originCopyId,
            Date creationTime,
            boolean readonly,
            Map<String, String> dsbSettings) {
        this.name = name;
        this.originCopyId = originCopyId;
        this.creationTime = creationTime;
        this.readonly = readonly;
        this.dsbSettings = dsbSettings;
    }

    public String getName() {
        return name;
    }

    public String getOriginCopyId() {
        return originCopyId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Map<String, String> getDsbSettings() {
        return dsbSettings;
    }

}
