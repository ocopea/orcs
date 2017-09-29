// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.vis;

import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final Map<String, Map<String, String>> data;

    // Required by  jackson
    private Graph() {
        this(null, null, null);
    }

    public Graph(List<Node> nodes, List<Edge> edges, Map<String, Map<String, String>> data) {
        this.nodes = nodes;
        this.edges = edges;
        this.data = data;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Map<String, Map<String, String>> getData() {
        return data;
    }
}
