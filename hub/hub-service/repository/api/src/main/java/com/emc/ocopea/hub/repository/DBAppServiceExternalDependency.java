// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import java.util.Collection;
import java.util.Map;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class DBAppServiceExternalDependency {
    private final String type;
    private final String name;
    private final Collection<DBAppServiceExternalDependencyProtocol> protocols;
    private final String description;

    public DBAppServiceExternalDependency(
            String type,
            String name,
            Collection<DBAppServiceExternalDependencyProtocol> protocols,
            String description) {
        this.type = type;
        this.name = name;
        this.protocols = protocols;
        this.description = description;
    }

    private DBAppServiceExternalDependency() {
        this(null,null, null, null);
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Collection<DBAppServiceExternalDependencyProtocol> getProtocols() {
        return protocols;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "DBAppServiceExternalDependency{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", protocol=" + protocols +
                ", description='" + description + '\'' +
                '}';
    }

    public static class DBAppServiceExternalDependencyProtocol {
        private final String protocol;
        private final String version;
        private final Map<String, String> conditions;
        private final Map<String, String> settings;

        private DBAppServiceExternalDependencyProtocol() {
            this(null,null,null, null);
        }

        public DBAppServiceExternalDependencyProtocol(
                String protocol,
                String version,
                Map<String, String> conditions,
                Map<String, String> settings) {
            this.protocol = protocol;
            this.version = version;
            this.conditions = conditions;
            this.settings = settings;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getVersion() {
            return version;
        }

        public Map<String, String> getConditions() {
            return conditions;
        }

        public Map<String, String> getSettings() {
            return settings;
        }

        @Override
        public String toString() {
            return "DBAppServiceExternalDependencyProtocol{" +
                    "protocol='" + protocol + '\'' +
                    ", version='" + version + '\'' +
                    ", conditions=" + conditions +
                    ", settings=" + settings +
                    '}';
        }
    }

}
