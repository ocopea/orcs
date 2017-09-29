// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 8/14/16.
 * Drink responsibly
 */
public interface SavedImageRepository {

    Collection<DBSavedImage> list();

    Collection<DBSavedImage> findByAppTemplateId(UUID appTemplateId);

    DBSavedImage get(UUID savedImageId);

    void add(DBSavedImage config) throws DuplicateResourceException;

    void updateImageState(UUID savedImageId, DBSavedImage.DBSavedImageState state);
}
