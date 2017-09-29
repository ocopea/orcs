// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.idea;

/**
 * Created by liebea on 5/29/15.
 * Drink responsibly
 */
public class IdeaForSubmission {
    private final String name;
    private final String description;
    private final String docName;
    private final String docKey;

    private IdeaForSubmission() {
        this(null, null, null, null);
    }

    public IdeaForSubmission(String name, String description, String docName, String docKey) {
        this.name = name;
        this.description = description;
        this.docName = docName;
        this.docKey = docKey;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDocName() {
        return docName;
    }

    public String getDocKey() {
        return docKey;
    }
}
