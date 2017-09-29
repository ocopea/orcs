// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import com.emc.ocopea.hub.AppServiceExternalDependencyProtocol;

import java.util.Collection;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class AppServiceExternalDependency {
    private final DataServiceTypeEnumDTO type;
    private final String name;
    private final Collection<AppServiceExternalDependencyProtocol> protocols;
    private final String description;

    public AppServiceExternalDependency(
            DataServiceTypeEnumDTO type,
            String name,
            Collection<AppServiceExternalDependencyProtocol> protocols,
            String description) {
        this.type = type;
        this.name = name;
        this.protocols = protocols;
        this.description = description;
    }

    public DataServiceTypeEnumDTO getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Collection<AppServiceExternalDependencyProtocol> getProtocols() {
        return protocols;
    }

    public String getDescription() {
        return description;
    }

}
