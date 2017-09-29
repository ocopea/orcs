// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.idea;

import java.util.UUID;

/**
 * Created by liebea on 5/29/15.
 * Drink responsibly
 */
public class SubmittedIdea {
    private final UUID id;
    private final String name;
    private final String description;
    private final String docName;
    private final String docKey;
    private final IdeaStatusEnum status;
    private final long votes;

    public SubmittedIdea() {
        this(null, null, null, null, null, null, 0);
    }

    public SubmittedIdea(
            UUID id,
            String name,
            String description,
            String docName,
            String docKey,
            IdeaStatusEnum status,
            long votes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.docName = docName;
        this.docKey = docKey;
        this.status = status;
        this.votes = votes;
    }

    public UUID getId() {
        return id;
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

    public IdeaStatusEnum getStatus() {
        return status;
    }

    public long getVotes() {
        return votes;
    }
}
