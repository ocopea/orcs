// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector.vis;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class Edge {
    private final String id;
    private final String from;
    private final String to;
    private final String style;
    private final String label;

    // Required by  jackson
    @SuppressWarnings("UnusedDeclaration")
    private Edge() {
        this(null, null, null, null);
    }

    public Edge(String from, String to, String style, String label) {
        this.id = from + "|" + to;
        this.from = from;
        this.to = to;
        this.style = style;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getStyle() {
        return style;
    }

    public String getLabel() {
        return label;
    }
}
