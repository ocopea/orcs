// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon.committee;

import java.util.UUID;

/**
 * Created by liebea on 3/1/17.
 * Drink responsibly
 */
public class IdeaReviewStatus {
    private final UUID ideaId;
    private final boolean approved;

    private IdeaReviewStatus() {
        this(null, false);
    }

    public IdeaReviewStatus(UUID ideaId, boolean approved) {
        this.ideaId = ideaId;
        this.approved = approved;
    }

    public UUID getIdeaId() {
        return ideaId;
    }

    public boolean isApproved() {
        return approved;
    }

    @Override
    public String toString() {
        return "IdeaReviewStatus{" +
                "ideaId=" + ideaId +
                ", approved=" + approved +
                '}';
    }
}
