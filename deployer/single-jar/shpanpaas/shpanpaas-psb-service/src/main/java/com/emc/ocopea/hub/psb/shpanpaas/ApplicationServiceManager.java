// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.psb.shpanpaas;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.ShpanPaaSResourceProvider;
import com.emc.ocopea.hub.application.AppServiceDependency;
import com.emc.ocopea.hub.application.ShpanPaaSAppTemplate;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by liebea on 7/22/15.
 * Drink responsibly
 */
public class ApplicationServiceManager implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceManager.class);
    private final Map<String, ShpanPaaSAppTemplate> applicationTemplateMap = new HashMap<>();
    private final Map<String, ApplicationInstance> appInstancesById = new HashMap<>();
    private ShpanPaaSResourceProvider resourceProvider;

    @NoJavadoc
    // TODO add javadoc
    public void createTemplate(MicroService serviceDescriptor) {
        applicationTemplateMap.put(
                serviceDescriptor.getIdentifier().getShortName(),
                new ShpanPaaSAppTemplate(
                        serviceDescriptor.getName(),
                        serviceDescriptor,
                        buildServiceDependencies(serviceDescriptor)
                ));
    }

    private Collection<AppServiceDependency> buildServiceDependencies(MicroService serviceDescriptor) {
        List<AppServiceDependency> dependencies = new ArrayList<>();
        dependencies.addAll(
                serviceDescriptor.getInitializationHelper().getDatasourceDescriptors()
                        .stream()
                        .map(currDSDesc ->
                                new AppServiceDependency(
                                        ServiceRegistryApi.SERVICE_TYPE_DATASOURCE,
                                        currDSDesc.getName(),
                                        currDSDesc.getDescription()))
                        .collect(Collectors.toList()));
        dependencies.addAll(serviceDescriptor.getInitializationHelper().getBlobStoreDescriptors()
                .stream()
                .map(
                        currBlobStoreDesc ->
                                new AppServiceDependency(
                                        ServiceRegistryApi.SERVICE_TYPE_BLOBSTORE,
                                        currBlobStoreDesc.getName(),
                                        currBlobStoreDesc.getName() + " Blob Store"))
                .collect(Collectors.toList()));
        return dependencies;
    }

    public Collection<ShpanPaaSAppTemplate> list() {
        return new ArrayList<>(applicationTemplateMap.values());
    }

    @NoJavadoc
    // TODO add javadoc
    public void runInstance(
            String appShortName,
            String appInstanceId,
            String baseURI,
            Collection<ServiceMapping> serviceMapping,
            String version,
            Map<String, String> environmentVariables) {

        ShpanPaaSAppTemplate shpanPaaSAppTemplate = Objects.requireNonNull(
                applicationTemplateMap.get(appShortName),
                "Unsupported app template " + appShortName);

        try {
            System.out.println(
                    "Creating " + new ObjectMapper().writeValueAsString(shpanPaaSAppTemplate.getDependencies()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ApplicationInstance instance = appInstancesById.get(appInstanceId);
        if (instance != null) {
            throw new WebApplicationException(
                    "App " + appShortName + " instance at base URL " + baseURI + " already running", 409);
        }

        // Binding the services and running it
        Map<String, String> serviceBindings = new HashMap<>();
        for (ServiceMapping curr : serviceMapping) {
            serviceBindings.put(curr.getServiceLogicalName(), curr.getServicePhysicalName());
        }
        final MicroServiceController controller =
                resourceProvider.runApplication(
                        shpanPaaSAppTemplate,
                        serviceBindings,
                        baseURI,
                        environmentVariables);

        log.info("Really ? done ?");

        appInstancesById.put(
                appInstanceId,
                new ApplicationInstance(appShortName, appInstanceId, version, baseURI, serviceMapping, controller));
    }

    @Override
    public void init(Context context) {
        log.info("Application manager init");
        resourceProvider = (ShpanPaaSResourceProvider) ResourceProviderManager.getResourceProvider();
    }

    @Override
    public void shutDown() {
        log.info("Application manager shutting down");
    }

    public Collection<ApplicationInstance> listInstances() {
        return new ArrayList<>(appInstancesById.values());

    }

    public ApplicationInstance getInstance(String instanceId) {
        return appInstancesById.get(instanceId);
    }

    public void stopApp(String appInstanceId) {
        final ApplicationInstance instance = getInstance(appInstanceId);
        instance.getController().stop();
        appInstancesById.remove(appInstanceId);
    }
}
