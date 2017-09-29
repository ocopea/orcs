// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.idea;

import java.util.UUID;

/**
 * Created by liebea on 5/31/15.
 * Drink responsibly
 */
public class VoteInfo {
    private final UUID ideaId;

    private VoteInfo() {
        this(null);
    }

    public VoteInfo(UUID ideaId) {
        this.ideaId = ideaId;
    }

    public UUID getIdeaId() {
        return ideaId;
    }
}
