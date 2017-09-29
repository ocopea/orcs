// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scheduler;

import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;
import com.emc.microservice.schedule.SchedulerConfiguration;

import java.util.Arrays;
import java.util.List;

public class PersistentSchedulerConfiguration extends SchedulerConfiguration {
    private static final String CONFIGURATION_NAME = "Persistent Scheduler Configuration";

    private static final ResourceConfigurationProperty PROPERTY_DATASOURCE_NAME =
            new ResourceConfigurationProperty(
                    "datasourceName",
                    ResourceConfigurationPropertyType.STRING,
                    "name of the datasource to use",
                    false,
                    false);

    private static final ResourceConfigurationProperty PERSIST_TASKS =
            new ResourceConfigurationProperty(
                    "persistTasks",
                    ResourceConfigurationPropertyType.BOOLEAN,
                    "Whether or not to persist tasks",
                    false,
                    false);

    private static final List<ResourceConfigurationProperty> PROPERTIES =
            Arrays.asList(PROPERTY_DATASOURCE_NAME, PERSIST_TASKS);

    public PersistentSchedulerConfiguration() {
        super(CONFIGURATION_NAME, PROPERTIES);
    }

    public PersistentSchedulerConfiguration(String datasourceName, boolean persistTasks) {
        this();
        setPropertyValues(propArrayToMap(
                new String[] {
                        PROPERTY_DATASOURCE_NAME.getName(),
                        datasourceName,
                        PERSIST_TASKS.getName(),
                        Boolean.toString(persistTasks)
                }
        ));
    }

    public String getDatasourceName() {
        return getProperty(PROPERTY_DATASOURCE_NAME.getName());
    }

    public boolean isPersistTasks() {
        final String strBool = getProperty(PERSIST_TASKS.getName());
        return strBool == null ? true : Boolean.valueOf(strBool);
    }
}
