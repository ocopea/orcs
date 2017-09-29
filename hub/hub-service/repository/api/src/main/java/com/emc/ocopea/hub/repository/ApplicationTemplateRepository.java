// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.repository;

import java.util.Collection;
import java.util.UUID;

/**
 * Created by liebea on 7/24/16.
 * Drink responsibly
 */
public interface ApplicationTemplateRepository {

    // Write
    void createApplicationTemplate(DBApplicationTemplate newAppTemplate) throws DuplicateResourceException;

    // Read
    DBApplicationTemplate getById(UUID appTemplateId);

    DBApplicationTemplate findByName(String appTemplateName);

    Collection<DBApplicationTemplate> list();

    void markAppTemplateAsDeleted(UUID appTemplateId);
}
