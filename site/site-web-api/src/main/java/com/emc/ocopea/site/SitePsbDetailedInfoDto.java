// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site;

import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 2/21/17.
 * Drink responsibly
 */
public class SitePsbDetailedInfoDto extends SitePsbInfoDto {
    private final List<SitePSBSpaceInfo> spaces;

    private SitePsbDetailedInfoDto() {
        this(null, null, null, null, null);
    }

    public SitePsbDetailedInfoDto(String urn, String name, String type, String version, List<SitePSBSpaceInfo> spaces) {
        super(urn, name, type, version);
        this.spaces = spaces;
    }

    public List<SitePSBSpaceInfo> getSpaces() {
        return spaces;
    }

    @Override
    public String toString() {
        return "SitePsbDetailedInfoDto{" +
                "spaces=" + spaces +
                '}';
    }

    public static class SitePSBSpaceInfo {
        private final String name;
        private final Map<String, String> properties;

        private SitePSBSpaceInfo() {
            this(null, null);
        }

        public SitePSBSpaceInfo(String name, Map<String, String> properties) {
            this.name = name;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String toString() {
            return super.toString() +
                    "SitePSBSpaceInfo{" +
                    "name='" + name + '\'' +
                    ", properties=" + properties +
                    '}';
        }
    }
}
