// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Created by liebea on 1/12/15.
 * Drink responsibly
 */
public class ServiceConfiguration {

    private static final String VERSION_NOT_AVAILABLE = "Version Not Available";
    private static final String DPA_MS_PACKAGE = "com.emc.dpa.";
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceConfiguration.class);

    private final String name;
    private final String version;
    private final String description;
    private final String baseUri;
    private final LoggerConfig logger;
    private final List<ResourceConfig> inputQueues;
    private final List<ResourceConfig> datasources;
    private final List<ResourceConfig> destinations;
    private final List<ResourceConfig> blobStores;
    private final List<ResourceConfig> serviceDependencies;
    private final List<ResourceConfig> externalResources;
    private final List<ParamConfig> parameters;

    // Required by  jackson
    private ServiceConfiguration() {
        this(null, null, null, null, null, null, null, null, null, null, null);
    }

    public ServiceConfiguration(
            String name,
            String description,
            String baseUri,
            LoggerConfig logger,
            List<ResourceConfig> inputQueues,
            List<ResourceConfig> datasources,
            List<ResourceConfig> destinations,
            List<ResourceConfig> blobStores,
            List<ResourceConfig> serviceDependencies,
            List<ResourceConfig> externalResources,
            List<ParamConfig> parameters) {
        this.name = name;
        this.version = readVersion();
        this.description = description;
        this.baseUri = baseUri;
        this.logger = logger;
        this.inputQueues = inputQueues;
        this.datasources = datasources;
        this.destinations = destinations;
        this.blobStores = blobStores;
        this.serviceDependencies = serviceDependencies;
        this.externalResources = externalResources;
        this.parameters = parameters;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public LoggerConfig getLogger() {
        return logger;
    }

    public List<ResourceConfig> getInputQueues() {
        return inputQueues;
    }

    public List<ResourceConfig> getDatasources() {
        return datasources;
    }

    public List<ResourceConfig> getDestinations() {
        return destinations;
    }

    public List<ResourceConfig> getBlobStores() {
        return blobStores;
    }

    public List<ResourceConfig> getServiceDependencies() {
        return serviceDependencies;
    }

    public List<ResourceConfig> getExternalResources() {
        return externalResources;
    }

    public List<ParamConfig> getParameters() {
        return parameters;
    }

    private String readVersion() {
        String version = VERSION_NOT_AVAILABLE;
        Enumeration<URL> resources = null;
        try {
            resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
        } catch (IOException e) {
            LOGGER.warn("Unable to read resources \"META-INF/MANIFEST.MF\"", e);
            version = VERSION_NOT_AVAILABLE;
        }
        while (resources != null && resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            Attributes attributes = null;
            try {
                attributes = new Manifest(resource.openStream()).getMainAttributes();
                if (String.valueOf(attributes.get(Attributes.Name.MAIN_CLASS)).startsWith(DPA_MS_PACKAGE)) {
                    version = (String) attributes.get(Attributes.Name.SPECIFICATION_VERSION);
                    break;
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to read resource \"" + resource.getPath() + "\"", e);
            }
        }
        return version == null || version.isEmpty() ? VERSION_NOT_AVAILABLE : version;
    }
}
