// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.resource.ResourceDescriptor;

/**
 * Created by liebea on 2/22/15.
 * Drink responsibly
 */
public class MessagingStatsResourceDescriptor implements ResourceDescriptor {
    public static final String MESSAGING_STATS_DESCRIPTOR_NAME = "messaging-stats";

    @Override
    public String getName() {
        return MESSAGING_STATS_DESCRIPTOR_NAME;
    }
}
