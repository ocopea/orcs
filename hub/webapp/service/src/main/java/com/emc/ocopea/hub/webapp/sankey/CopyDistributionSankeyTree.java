// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp.sankey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liebea on 5/23/16.
 * Drink responsibly
 */
public class CopyDistributionSankeyTree {
    private final SankeyEntry root;

    public CopyDistributionSankeyTree(String appInstanceName) {
        this.root = new SankeyEntry(appInstanceName);
    }

    public void addLeaf(Long weight, String dataLocation, String purpose, String dataService) {
        root.addPath(weight, dataService, purpose, dataLocation);
    }

    public SankeyEntry getRoot() {
        return root;
    }

    public List<List<Object>> toSankeyFormat() {
        List<List<Object>> data = new ArrayList<>();
        mapEntry(data, root);
        return data;
    }

    private List<List<Object>> mapEntry(List<List<Object>> data, SankeyEntry curr) {
        curr.getChildren().entrySet().forEach(e -> {
                    data.add(Arrays.asList(curr.getName(), e.getKey(), e.getValue().getWeight()));
                    mapEntry(data, e.getValue().getChildEntry());
                }
        );

        return data;
    }
}
