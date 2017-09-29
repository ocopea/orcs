// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 1/3/17.
 * Drink responsibly
 */
public class DbSite {
    private final Collection<DbSiteDsb> dsbs;
    private final Collection<DbCrb> crbs;
    private final Collection<DbSitePsb> psbs;
    private final Collection<DbSiteArtifactRegistry> artifactRegistries;

    private DbSite() {
        this(null, null, null, null);
    }

    public Collection<DbSiteDsb> getDsbs() {
        return dsbs;
    }

    public Collection<DbCrb> getCrbs() {
        return crbs;
    }

    public Collection<DbSitePsb> getPsbs() {
        return psbs;
    }

    public Collection<DbSiteArtifactRegistry> getArtifactRegistries() {
        return artifactRegistries;
    }

    @Override
    public String toString() {
        return "DBSite{" +
                "dsbs=" + dsbs +
                ", crbs=" + crbs +
                ", psbs=" + psbs +
                ", artifactRegistries=" + artifactRegistries +
                '}';
    }

    public DbSite(
            Collection<DbSiteDsb> dsbs,
            Collection<DbCrb> crbs,
            Collection<DbSitePsb> psbs,
            Collection<DbSiteArtifactRegistry> artifactRegistries) {
        this.dsbs = dsbs;
        this.crbs = crbs;
        this.psbs = psbs;
        this.artifactRegistries = artifactRegistries;
    }

    public static class DbSiteDsb {
        private final String name;
        private final String urn;
        private final String url;
        private final String type;
        private final String description;
        private final List<DBSiteDsbPlan> plans;

        public DbSiteDsb(
                String name,
                String urn,
                String url,
                String type,
                String description,
                List<DBSiteDsbPlan> plans) {
            this.name = name;
            this.urn = urn;
            this.url = url;
            this.type = type;
            this.description = description;
            this.plans = plans;
        }

        private DbSiteDsb() {
            this(null, null, null, null, null, null);
        }

        public String getName() {
            return name;
        }

        public String getUrn() {
            return urn;
        }

        public String getUrl() {
            return url;
        }

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }

        public List<DBSiteDsbPlan> getPlans() {
            return plans;
        }

        @Override
        public String toString() {
            return "DBSiteDsb{" +
                    "name='" + name + '\'' +
                    ", urn='" + urn + '\'' +
                    ", url='" + url + '\'' +
                    ", type='" + type + '\'' +
                    ", description='" + description + '\'' +
                    ", plans=" + plans +
                    '}';
        }
    }

    public static class DBSiteSupportedDsbProtocol {
        private final String protocolName;
        private final String protocolVersion;
        private final Map<String, String> properties;

        private DBSiteSupportedDsbProtocol() {
            this(null, null, null);
        }

        public DBSiteSupportedDsbProtocol(String protocolName, String protocolVersion, Map<String, String> properties) {
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
            return "DBSiteSupportedDSBProtocol{" +
                    "protocolName='" + protocolName + '\'' +
                    ", protocolVersion='" + protocolVersion + '\'' +
                    ", properties=" + properties +
                    '}';
        }
    }

    public static class DBSiteSupportedDsbCopyProtocol {
        private final String protocolName;
        private final String protocolVersion;

        private DBSiteSupportedDsbCopyProtocol() {
            this(null, null);
        }

        public DBSiteSupportedDsbCopyProtocol(String protocolName, String protocolVersion) {
            this.protocolName = protocolName;
            this.protocolVersion = protocolVersion;
        }

        public String getProtocolName() {
            return protocolName;
        }

        public String getProtocolVersion() {
            return protocolVersion;
        }

        @Override
        public String toString() {
            return "DBSiteSupportedDSBCopyProtocol{" +
                    "protocolName='" + protocolName + '\'' +
                    ", protocolVersion='" + protocolVersion + '\'' +
                    '}';
        }
    }

    public static class DbCrb {
        private final String urn;
        private final String url;
        private final String name;
        private final String type;
        private final String version;

        public DbCrb(String urn, String url, String name, String type, String version) {
            this.urn = urn;
            this.url = url;
            this.name = name;
            this.type = type;
            this.version = version;
        }

        private DbCrb() {
            this(null, null, null, null, null);
        }

        public String getUrn() {
            return urn;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return "DbCrb{" +
                    "urn='" + urn + '\'' +
                    ", url='" + url + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", version='" + version + '\'' +
                    '}';
        }
    }

    public static class DbSitePsb {
        private final String urn;
        private final String url;
        private final String name;
        private final String type;
        private final String version;
        private final int maxAppServiceIdLength;

        public DbSitePsb(String urn, String url, String name, String type, String version, int maxAppServiceIdLength) {
            this.urn = urn;
            this.url = url;
            this.name = name;
            this.type = type;
            this.version = version;
            this.maxAppServiceIdLength = maxAppServiceIdLength;
        }

        private DbSitePsb() {
            this(null, null, null, null, null, 0);
        }

        public String getUrn() {
            return urn;
        }

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getVersion() {
            return version;
        }

        public int getMaxAppServiceIdLength() {
            return maxAppServiceIdLength;
        }

        @Override
        public String toString() {
            return "DbSitePsb{" +
                    "urn='" + urn + '\'' +
                    ", url='" + url + '\'' +
                    ", name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", version='" + version + '\'' +
                    ", maxAppServiceIdLength=" + maxAppServiceIdLength +
                    '}';
        }
    }

    public static class DbSiteArtifactRegistry {
        private final String name;
        private final String type;
        private final Map<String, String> parameters;

        private DbSiteArtifactRegistry() {
            this(null, null, null);
        }

        public DbSiteArtifactRegistry(String name, String type, Map<String, String> parameters) {
            this.name = name;
            this.type = type;
            this.parameters = parameters;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        @Override
        public String toString() {
            return "DBSiteArtifactRegistry{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", parameters=" + parameters +
                    '}';
        }
    }
}
