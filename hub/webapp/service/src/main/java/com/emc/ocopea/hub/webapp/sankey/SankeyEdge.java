// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp.sankey;

/**
 * Created by liebea on 5/23/16.
 * Drink responsibly
 */
public class SankeyEdge {
    private long weight;
    private final SankeyEntry childEntry;

    public SankeyEdge(Long weight, SankeyEntry childEntry) {
        this.weight = weight;
        this.childEntry = childEntry;
    }

    public Long getWeight() {
        return weight;
    }

    public SankeyEntry getChildEntry() {
        return childEntry;
    }

    public void increaseWeight(long weight) {
        this.weight += weight;
    }
}
