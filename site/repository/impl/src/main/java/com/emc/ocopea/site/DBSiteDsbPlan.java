// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Collection;
import java.util.Map;

/**
 * Created by liebea on 4/9/17.
 * Drink responsibly
 */
public class DBSiteDsbPlan {
    private final String id;
    private final String name;
    private final String description;
    private final String price;
    private final Collection<DbSite.DBSiteSupportedDsbProtocol> protocols;
    private final Collection<DbSite.DBSiteSupportedDsbCopyProtocol> copyProtocols;
    private final Map<String, String> dsbSettings;

    private DBSiteDsbPlan() {
        this(null,null,null,null,null, null, null);
    }

    public DBSiteDsbPlan(
            String id,
            String name,
            String description,
            String price,
            Collection<DbSite.DBSiteSupportedDsbProtocol> protocols,
            Collection<DbSite.DBSiteSupportedDsbCopyProtocol> copyProtocols,
            Map<String, String> dsbSettings) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.protocols = protocols;
        this.copyProtocols = copyProtocols;
        this.dsbSettings = dsbSettings;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getPrice() {
        return price;
    }

    public Collection<DbSite.DBSiteSupportedDsbProtocol> getProtocols() {
        return protocols;
    }

    public Collection<DbSite.DBSiteSupportedDsbCopyProtocol> getCopyProtocols() {
        return copyProtocols;
    }

    public Map<String, String> getDsbSettings() {
        return dsbSettings;
    }

    @Override
    public String toString() {
        return "DBSiteDSBPlan{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price='" + price + '\'' +
                ", protocols=" + protocols +
                ", copyProtocols=" + copyProtocols +
                ", dsbSettings=" + dsbSettings +
                '}';
    }
}
