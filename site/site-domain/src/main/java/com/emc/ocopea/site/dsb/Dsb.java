// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.dsb;

import com.emc.ocopea.dsb.DsbPlan;

import java.util.List;

/**
 * Created by liebea on 1/3/16.
 * Drink responsibly
 */
public class Dsb {
    private final String name;
    private final String urn;
    private final String url;
    private final String type;
    private final String description;
    private final List<DsbPlan> plans;

    public Dsb(
            String name,
            String urn,
            String url,
            String type,
            String description,
            List<DsbPlan> plans) {
        this.name = name;
        this.urn = urn;
        this.url = url;
        this.type = type;
        this.description = description;
        this.plans = plans;
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

    public List<DsbPlan> getPlans() {
        return plans;
    }
}
