// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 8/1/16.
 * Drink responsibly
 */
public interface ConnectedSiteRepository {

    // Write
    void addConnectedSite(DbConnectedSite connectedSite) throws DuplicateResourceException;

    // Read
    Collection<DbConnectedSite> list();

    DbConnectedSite findByURN(String urn);

    DbConnectedSite getById(UUID id);
}
