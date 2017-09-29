// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.policy;

import java.util.Map;

/**
 * Created by liebea on 2/7/16.
 * Drink responsibly
 */
public class AppPolicy {
    private final String policyType;
    private final String policyName;
    private final Map<String, String> policySettings;

    public AppPolicy(String policyType, String policyName, Map<String, String> policySettings) {
        this.policyType = policyType;
        this.policyName = policyName;
        this.policySettings = policySettings;
    }

    public String getPolicyType() {
        return policyType;
    }

    public String getPolicyName() {
        return policyName;
    }

    public Map<String, String> getPolicySettings() {
        return policySettings;
    }

    @Override
    public String toString() {
        return "AppPolicy{" +
                "policyType='" + policyType + '\'' +
                ", policyName='" + policyName + '\'' +
                ", policySettings=" + policySettings +
                '}';
    }
}
