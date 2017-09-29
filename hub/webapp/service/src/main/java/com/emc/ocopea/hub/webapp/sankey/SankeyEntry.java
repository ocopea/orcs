// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp.sankey;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
* Created by liebea on 5/23/16.
* Drink responsibly
*/
public class SankeyEntry {
    private final Map<String, SankeyEdge> children;
    private final String name;
    private Long weight;

    public SankeyEntry(String name, long weight) {
        this.name = name;
        this.children = Collections.emptyMap();
        this.weight = weight;
    }

    public SankeyEntry(String name) {
        this.name = name;
        this.children = new HashMap<>();
        this.weight = null;
    }

    public Map<String, SankeyEdge> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public long getWeight() {
        if (weight != null) {
            return weight;
        } else {
            return children.values().stream().mapToLong(SankeyEdge::getWeight).sum();
        }
    }

    public void inc(long weight) {
        if (this.weight == null) {
            throw new IllegalStateException("Can't inc non-leaf node " + name);
        }
        this.weight += weight;
    }

    // todo: add javadoc, probably document the whole sankey stuff
    @NoJavadoc
    public void addPath(Long weight, String... path) {
        String currChildName = path[0];
        if (this.weight != null) {
            throw new IllegalStateException("Can't add edge to a leaf sankey node" + this.name + "/" + currChildName);
        }

        if (path.length == 1) {
            SankeyEdge childEdge = children.get(currChildName);
            if (childEdge == null) {
                childEdge = new SankeyEdge(weight, new SankeyEntry(currChildName, weight));
                children.put(currChildName, childEdge);
            } else {
                childEdge.increaseWeight(weight);
                childEdge.getChildEntry().inc(weight);
            }
        } else {
            SankeyEdge childEdge = children.get(currChildName);
            if (childEdge == null) {
                childEdge = new SankeyEdge(weight, new SankeyEntry(currChildName));
                children.put(currChildName, childEdge);
            } else {
                childEdge.increaseWeight(weight);
            }

            childEdge.getChildEntry().addPath(weight, Arrays.copyOfRange(path, 1, path.length));
        }
    }
}
