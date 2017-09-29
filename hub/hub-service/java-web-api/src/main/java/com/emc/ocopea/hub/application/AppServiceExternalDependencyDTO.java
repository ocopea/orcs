// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Collection;
import java.util.Map;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class AppServiceExternalDependencyDTO {
    private final DataServiceTypeEnumDTO type;
    private final String name;
    private final Collection<AppServiceExternalDependencyProtocolDTO> protocols;
    private final String description;

    private AppServiceExternalDependencyDTO() {
        this(null, null, null, null);
    }

    public AppServiceExternalDependencyDTO(
            DataServiceTypeEnumDTO type,
            String name,
            Collection<AppServiceExternalDependencyProtocolDTO> protocols,
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

    public Collection<AppServiceExternalDependencyProtocolDTO> getProtocols() {
        return protocols;
    }

    public String getDescription() {
        return description;
    }

    public static class AppServiceExternalDependencyProtocolDTO {
        private final String protocolName;
        private final String version;
        private final Map<String, String> conditions;
        private final Map<String, String> settings;

        @SuppressWarnings("unused")
        private AppServiceExternalDependencyProtocolDTO() {
            this(null, null, null, null);
        }

        public AppServiceExternalDependencyProtocolDTO(
                String protocolName,
                String version,
                Map<String, String> conditions,
                Map<String, String> settings) {
            this.protocolName = protocolName;
            this.version = version;
            this.conditions = conditions;
            this.settings = settings;
        }

        public String getProtocolName() {
            return protocolName;
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
            return "AppServiceExternalDependencyProtocolDTO{" +
                    "protocolName='" + protocolName + '\'' +
                    ", version='" + version + '\'' +
                    ", conditions=" + conditions +
                    ", settings=" + settings +
                    '}';
        }
    }

}
