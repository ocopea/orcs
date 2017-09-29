// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.vis;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liebea on 1/11/15.
 * Drink responsibly
 */
public class GraphBuilder {
    private final Map<String, Node> nodesById = new HashMap<>();
    private final Map<String, Edge> edges = new HashMap<>();
    private final Map<String, Map<String, String>> data = new HashMap<>();

    public enum NodeType {
        service("box"),
        queue("dot"),
        db("database"),
        blobStore("database");

        NodeType(String shape) {
            this.shape = shape;
        }

        String shape;
    }

    public Node addNode(String id, NodeType type, String label) {
        return addNode(id, type, label, null);
    }

    @NoJavadoc
    public Node addNode(String id, NodeType type, String label, Map<String, String> extraProperties) {
        Node node = new Node(id, label, type.shape);
        nodesById.put(id, node);
        Map<String, String> props = createData(id);
        props.put("type", type.name());
        if (extraProperties != null) {
            props.putAll(extraProperties);
        }
        return node;
    }

    private Map<String, String> createData(String id) {
        Map<String, String> props = data.get(id);
        if (props == null) {
            props = new HashMap<>();
            data.put(id, props);
        }
        return props;
    }

    public void addEdge(Node fromNode, Node toNode) {
        addEdge(fromNode, toNode, null, "arrow");
    }

    public void addEdge(Node fromNode, Node toNode, String label, String style) {
        String edgeKey = fromNode.getId() + "-" + toNode.getId();
        if (!edges.containsKey(edgeKey)) {
            edges.put(edgeKey, new Edge(fromNode.getId(), toNode.getId(), style, label));
        }
    }

    public Node getNodeById(String id) {
        return nodesById.get(id);
    }

    public Graph build() {
        return new Graph(new ArrayList<>(nodesById.values()), new ArrayList<>(edges.values()), data);
    }

    public GraphBuilder withData(String id, Map<String, String> data) {
        createData(id).putAll(data);
        return this;
    }

    public GraphBuilder withData(String id, String key, String value) {
        createData(id).put(key, value);
        return this;
    }

}
