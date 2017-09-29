// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id$
 * 
 * This computer code is copyright 2014 EMC Corporation
 * All rights reserved
 */

package com.emc.ocopea.services.rest;

import com.emc.microservice.Context;
import com.emc.microservice.ParametersBag;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.dependency.ServiceDependencyDescriptor;
import com.emc.microservice.logging.LoggingProvider;
import com.emc.microservice.resource.ManagedResource;
import com.emc.microservice.resource.ResourceManager;
import com.emc.microservice.resource.ResourceProviderManager;
import org.slf4j.Logger;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicroServiceConfigurationResource extends MicroServiceResource implements MicroServiceConfigurationAPI {

    @Override
    public ServiceConfiguration getServiceConfiguration() {

        // Getting micro-service instance from context
        Context context = getMicroServiceApplication().getMicroServiceContext();

        // Getting logger configuration
        LoggerConfig loggerConfig = getLoggerConfig(context);

        // Getting input queues configurations
        List<ResourceConfig> inputQueues = getResourceConfigs(context.getQueuesManager().getManagedResources());

        // Getting datasources config
        List<ResourceConfig> datasources = getResourceConfigs(context.getDatasourceManager().getManagedResources());

        // Getting destination config
        List<ResourceConfig> destinations = getResourceConfigs(context.getDestinationManager().getManagedResources());

        // Getting blobStores configurations
        List<ResourceConfig> blobStores = getResourceConfigs(context.getBlobStoreManager().getManagedResources());

        // Getting service dependency configurations
        List<ResourceConfig> serviceDependencies =
                getDependencyConfigs(context.getDependencyManager().getManagedResources());

        List<ResourceConfig> externalResources = new ArrayList<>();
        for (ResourceManager currExternalResourceManager : context.getExternalResourceManagers()) {
            //noinspection unchecked
            externalResources.addAll(getResourceConfigs(currExternalResourceManager.getManagedResources()));
        }

        // Get params configs
        List<ParamConfig> paramsConfig = getParamConfigs(context);

        return new ServiceConfiguration(
                context.getMicroServiceName(),
                context.getServiceDescription(),
                context.getMicroServiceBaseURI(),
                loggerConfig,
                inputQueues,
                datasources,
                destinations,
                blobStores,
                serviceDependencies,
                externalResources,
                paramsConfig
        );
    }

    private List<ParamConfig> getParamConfigs(Context context) {
        ParametersBag params = context.getParametersBag();
        List<ParamConfig> paramsConfig = Collections.emptyList();
        if (!params.getParameterDescriptorsMap().isEmpty()) {
            paramsConfig = new ArrayList<>(params.getParameterDescriptorsMap().size());
            for (Map.Entry<String, ParametersBag.MicroServiceParameterDescriptor> currEntry : params
                    .getParameterDescriptorsMap()
                    .entrySet()) {
                paramsConfig.add(
                        new ParamConfig(
                                currEntry.getKey(),
                                currEntry.getValue().getDescription(),
                                params.getString(currEntry.getKey())));

            }
        }
        return paramsConfig;
    }

    private List<ResourceConfig> getDependencyConfigs(List<ManagedDependency> managedDependencyList) {
        List<ResourceConfig> inputQueues = new ArrayList<>(managedDependencyList.size());
        if (!managedDependencyList.isEmpty()) {
            for (ManagedDependency currManagedResource : managedDependencyList) {
                Map<String, String> properties =
                        new HashMap<>(currManagedResource.getConfiguration().getPublicPropertyValues());
                ServiceDependencyDescriptor descriptor = currManagedResource.getDescriptor();
                properties.put("routing", concatStringsWSep(descriptor.getMessageRoutingTable(), ","));
                inputQueues.add(
                        new ResourceConfig(
                                descriptor.getName(),
                                properties));
            }
        }
        return inputQueues;
    }

    private static String concatStringsWSep(Iterable<String> strings, String separator) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (String s : strings) {
            sb.append(sep).append(s);
            sep = separator;
        }
        return sb.toString();
    }

    private <T extends ManagedResource> List<ResourceConfig> getResourceConfigs(List<T> managedResources) {
        List<ResourceConfig> inputQueues = new ArrayList<>(managedResources.size());
        if (!managedResources.isEmpty()) {
            for (T currManagedResource : managedResources) {
                inputQueues.add(
                        new ResourceConfig(
                                currManagedResource.getDescriptor().getName(),
                                currManagedResource.getConfiguration().getPublicPropertyValues()));
            }
        }
        return inputQueues;
    }

    private LoggerConfig getLoggerConfig(Context context) {
        Logger logger = context.getLogger();
        return new LoggerConfig(logger.getName(), getLogLevel(logger));
    }

    @Override
    public Response setLogLevel() {
        ResourceProviderManager.getResourceProvider().setLogLevel(LoggingProvider.LogLevel.DEBUG, null, null);
        return Response.ok().build();
    }

    private String getLogLevel(Logger logger) {
        String level = "Unknown";
        if (logger.isTraceEnabled()) {
            level = "Trace";
        } else if (logger.isDebugEnabled()) {
            level = "Debug";
        } else if (logger.isInfoEnabled()) {
            level = "Info";
        } else if (logger.isWarnEnabled()) {
            level = "Warn";
        } else if (logger.isErrorEnabled()) {
            level = "Error";
        }
        return level;
    }

}
