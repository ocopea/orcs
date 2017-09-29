// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.runner;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceController;
import com.emc.microservice.resource.ResourceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 */
public class MicroServiceRunner {

    public static final class ServiceDeploymentDescriptor {
        private final String serviceURI;
        private final MicroService microService;

        public ServiceDeploymentDescriptor(String serviceURI, MicroService microService) {
            this.serviceURI = serviceURI;
            this.microService = microService;
        }
    }

    /**
     * Run service, Run!!!
     */
    public Map<String, MicroServiceController> run(ResourceProvider resourceProvider, MicroService... services) {
        return run(
                resourceProvider,
                Stream
                        .of(services)
                        .map(o -> new ServiceDeploymentDescriptor(o.getIdentifier().getShortName(), o))
                        .collect(Collectors.toCollection(ArrayList::new)));
    }

    /**
     * Run service, Run!!!
     */
    public Map<String, MicroServiceController> run(
            ResourceProvider resourceProvider,
            List<ServiceDeploymentDescriptor> servicesByURI) {
        return
                servicesByURI.stream().collect(Collectors.toMap(o -> o.serviceURI, t -> {
                    final MicroServiceController microServiceController =
                            new MicroServiceController(resourceProvider, t.microService, t.serviceURI);
                    microServiceController.start();
                    return microServiceController;
                }));
    }
}
