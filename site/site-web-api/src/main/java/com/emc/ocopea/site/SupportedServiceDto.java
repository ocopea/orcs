// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 7/21/15.
 * Drink responsibly
 */
public class SupportedServiceDto {
    private final String urn;
    private final String name;
    private final String type;
    private final String description;
    private final List<SupportedServicePlanDto> plans;

    private SupportedServiceDto() {
        this(null, null, null, null, null);
    }

    public SupportedServiceDto(
            String urn,
            String name,
            String type,
            String description,
            List<SupportedServicePlanDto> plans) {
        this.urn = urn;
        this.name = name;
        this.type = type;
        this.description = description;
        this.plans = plans;
    }

    public String getUrn() {
        return urn;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public List<SupportedServicePlanDto> getPlans() {
        return plans;
    }

    public static class SupportedServiceProtocolDto {
        private final String protocolName;
        private final String protocolVersion;
        private final Map<String, String> properties;

        // For  jackson
        private SupportedServiceProtocolDto() {
            this(null, null, null);
        }

        public SupportedServiceProtocolDto(
                String protocolName, String protocolVersion,
                Map<String, String> properties) {
            this.protocolName = protocolName;
            this.protocolVersion = protocolVersion;
            this.properties = properties;
        }

        public String getProtocolName() {
            return protocolName;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String toString() {
            return "DSBSupportedProtocolDto{" +
                    "protocolName='" + protocolName + '\'' +
                    ", protocolVersion='" + protocolVersion + '\'' +
                    ", properties=" + properties +
                    '}';
        }

    }

    public static class SupportedServicePlanDto {
        private final String id;
        private final String name;
        private final String description;
        private final String price;
        private final Collection<SupportedServiceProtocolDto> supportedProtocols;
        private final Map<String, String> dsbSettings;

        private SupportedServicePlanDto() {
            this(null, null, null, null, null, null);
        }

        public SupportedServicePlanDto(
                String id,
                String name,
                String description,
                String price,
                Collection<SupportedServiceProtocolDto> supportedProtocols,
                Map<String, String> dsbSettings) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.supportedProtocols = supportedProtocols;
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

        public Collection<SupportedServiceProtocolDto> getSupportedProtocols() {
            return supportedProtocols;
        }

        public Map<String, String> getDsbSettings() {
            return dsbSettings;
        }

        @Override
        public String toString() {
            return "SupportedServicePlanDto{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", price='" + price + '\'' +
                    ", supportedProtocols=" + supportedProtocols +
                    ", dsbSettings=" + dsbSettings +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SupportedServiceDto{" +
                "urn='" + urn + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", plans=" + plans +
                '}';
    }
}
