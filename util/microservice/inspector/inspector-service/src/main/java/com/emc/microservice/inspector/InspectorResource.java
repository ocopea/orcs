// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.MicroserviceIdentifier;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.inspector.graph.Queue;
import com.emc.microservice.inspector.graph.Service;
import com.emc.microservice.inspector.graph.ServiceGraph;
import com.emc.microservice.inspector.graph.ServiceGraphBuilder;
import com.emc.microservice.inspector.vis.Graph;
import com.emc.microservice.inspector.vis.GraphBuilder;
import com.emc.microservice.inspector.vis.Node;
import com.emc.microservice.messaging.MessagingStatsConnection;
import com.emc.microservice.messaging.MessagingStatsResourceDescriptor;
import com.emc.microservice.messaging.QueueConfiguration;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.resource.ResourceProviderManager;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.services.rest.ResourceConfig;
import org.apache.commons.io.IOUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class InspectorResource implements InspectorAPI {

    private ServiceGraphBuilder builder;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        ResourceProvider resourceProvider = ResourceProviderManager.getResourceProvider();
        ServiceRegistryApi serviceRegistryApi = resourceProvider.getServiceRegistryApi();

        MessagingStatsConnection messagingStatsConnection = null;
        if (context.isSupportingResource(MessagingStatsResourceDescriptor.class)) {
            messagingStatsConnection = context.getManagedResourceByDescriptor(
                    MessagingStatsResourceDescriptor.class,
                    MessagingStatsResourceDescriptor.MESSAGING_STATS_DESCRIPTOR_NAME);
        }

        this.builder = new ServiceGraphBuilder(serviceRegistryApi, messagingStatsConnection);
    }

    @Override
    public Graph getGraph() {
        // Building the services graph
        ServiceGraph graph = builder.build();
        // Creating the GraphBuilder for building the presentation representation of the graph
        GraphBuilder b = new GraphBuilder();
        addServices(graph, b);
        addQueues(graph, b);
        return b.build();
    }

    private void addQueues(ServiceGraph graph, GraphBuilder b) {
        for (Queue currQueue : graph.getInputQueuesByURI().values()) {

            if (currQueue.getInputQueueConfiguration() != null) {
                b.withData(currQueue.getName(), currQueue.getInputQueueConfiguration().getPublicPropertyValues());
            }

            if (currQueue.getQueueStats() != null) {
                b.withData(
                        currQueue.getName(),
                        "messagesInQueue",
                        String.valueOf(currQueue.getQueueStats().getNumberOfMessages()));
                b.withData(
                        currQueue.getName(),
                        "messagesSinceStartup",
                        String.valueOf(currQueue.getQueueStats().getTotalMessagesSinceStartup()));
            }

        }
    }

    private void addServices(ServiceGraph graph, GraphBuilder b) {
        // Iterating on services and adding to Graph
        for (Service currService : getUniqueServices(graph)) {
            ServiceConfig serviceConfig = currService.getServiceConfig();
            int activeInstances = getActiveInstances(serviceConfig);

            // Service node key is the registered route since it is unique
            Node serviceNode = b.addNode(
                    serviceConfig.getRoute(),
                    GraphBuilder.NodeType.service,
                    serviceConfig.getServiceURI() + " (" + Integer.toString(activeInstances) + ")");

            // Adding all properties as service node graph data
            b.withData(serviceNode.getId(), serviceConfig.getParameters())
                    .withData(serviceNode.getId(), currService.getMetrics())
                    .withData(
                            serviceNode.getId(),
                            "state",
                            currService.getStateMessage());
            // FIXME can have more than one instance so state can be mixed

            addInputs(b, currService, serviceNode);
            addOutputs(graph, b, currService, serviceNode);
            addDatasourceNodes(b, currService, serviceNode);
            addBlobstoreNodes(b, currService, serviceNode);
            addServiceDependencies(graph, b, currService, serviceNode);
        }
    }

    private void addOutputs(ServiceGraph graph, GraphBuilder b, Service currService, Node serviceNode) {
        // Creating the main destination queue and edges for the service
        if (currService.getOutputQueueURI() != null) {
            Node outputNode = b.getNodeById(currService.getOutputQueueURI());
            if (outputNode == null) {
                outputNode = b.addNode(currService.getOutputQueueURI(), GraphBuilder.NodeType.queue, "input");
                Queue queue = graph.getInputQueuesByURI().get(currService.getOutputQueueURI());
                if (queue != null) {
                    QueueConfiguration outputQueueConfiguration = queue.getInputQueueConfiguration();
                    if (outputQueueConfiguration != null) {
                        b.withData(outputNode.getId(), outputQueueConfiguration.getPublicPropertyValues());
                    }
                }
            }

            b.addEdge(serviceNode, outputNode);
        }

        // Adding additional destinations
        for (ResourceConfig currDestinationConfig : currService.getDestinationConfigurations().values()) {
            Node outputNode = b.getNodeById(currDestinationConfig.getName());
            if (outputNode == null) {
                outputNode = b.addNode(currDestinationConfig.getName(),
                        GraphBuilder.NodeType.queue,
                        currDestinationConfig.getName(),
                        currDestinationConfig.getProperties());
            }

            b.addEdge(serviceNode, outputNode);
        }
    }

    private void addInputs(GraphBuilder b, Service currService, Node serviceNode) {
        // Creating the input queue and edge for the service
        if (currService.getInputQueueURI() != null) {
            Node queueNode = b.getNodeById(currService.getInputQueueURI());
            if (queueNode == null) {
                queueNode = b.addNode(currService.getInputQueueURI(), GraphBuilder.NodeType.queue, "input");
            }

            b.addEdge(queueNode, serviceNode);
        }

        // Adding additional input queues
        for (ResourceConfig currInputQueueConfig : currService.getInputQueuesConfigurations().values()) {
            Node queueNode = b.getNodeById(currInputQueueConfig.getName());
            if (queueNode == null) {
                queueNode = b.addNode(currInputQueueConfig.getName(),
                        GraphBuilder.NodeType.queue,
                        currInputQueueConfig.getName());
            }

            b.withData(queueNode.getId(), currInputQueueConfig.getProperties());
            b.addEdge(queueNode, serviceNode);
        }
    }

    private void addServiceDependencies(ServiceGraph graph, GraphBuilder b, Service currService, Node serviceNode) {
        // Adding Service dependencies
        for (ResourceConfig currServiceDependencyConfig : currService.getServiceDependencyConfigurations().values()) {

            String routing = currServiceDependencyConfig.getProperties().get("routing");
            List<String> serviceRoutes;
            if (routing == null) {
                serviceRoutes = Collections.singletonList(currServiceDependencyConfig.getName());
            } else {
                serviceRoutes = Arrays.asList(routing.split(","));
            }

            // Adding arrow to dependent service
            String dependentServiceName = serviceRoutes.get(0);
            String dependentServiceInputQueueURI =
                    new MicroserviceIdentifier(dependentServiceName).getDefaultInputQueueName();
            Node dependentServiceQueueNode = b.getNodeById(dependentServiceInputQueueURI);
            if (dependentServiceQueueNode == null) {
                dependentServiceQueueNode =
                        b.addNode(dependentServiceInputQueueURI, GraphBuilder.NodeType.queue, "input");
            }
            b.addEdge(serviceNode, dependentServiceQueueNode);

            // Getting all queues for non-last-of-the-chain services
            for (int routIdx = 0; routIdx < serviceRoutes.size() - 1; routIdx++) {
                String currServiceURI = serviceRoutes.get(routIdx);
                String nextServiceURI = serviceRoutes.get(routIdx + 1);
                String nextServiceQueueURI = new MicroserviceIdentifier(nextServiceURI).getDefaultInputQueueName();

                Node nextServiceInputQueueNode = b.getNodeById(nextServiceQueueURI);
                if (nextServiceInputQueueNode == null) {
                    nextServiceInputQueueNode = b.addNode(nextServiceQueueURI, GraphBuilder.NodeType.queue, "input");
                }

                // Connecting all services of same type to output
                for (Service currServiceType : graph.getServices()) {
                    if (currServiceType.getServiceConfig().getServiceURI().equals(currServiceURI)) {
                        Node currServiceNode = b.getNodeById(currServiceType.getServiceConfig().getRoute());
                        if (currServiceNode == null) {
                            currServiceNode = b.addNode(currServiceType.getServiceConfig().getRoute(),
                                    GraphBuilder.NodeType.service,
                                    currServiceType.getServiceConfig().getServiceURI());
                        }
                        b.addEdge(currServiceNode, nextServiceInputQueueNode);
                    }
                }
            }

            // For the last service in chain - adding the callback queue
            String lastServiceOnChainBaseURI = serviceRoutes.get(serviceRoutes.size() - 1);
            String dependencyCallbackQueueName = new MicroserviceIdentifier(currService
                    .getServiceConfig()
                    .getServiceURI()).getDependencyCallbackQueueName(lastServiceOnChainBaseURI);
            Node callbackQueueNode = b.getNodeById(dependencyCallbackQueueName);
            if (callbackQueueNode != null) {
                // Rename node with better label
                callbackQueueNode = b.addNode(dependencyCallbackQueueName,
                        GraphBuilder.NodeType.queue,
                        lastServiceOnChainBaseURI + ".output");

                // Connecting all services of same type to output
                for (Service currLastService : graph.getServices()) {
                    if (currLastService.getServiceConfig().getServiceURI().equals(lastServiceOnChainBaseURI)) {
                        Node lastNodeInChain = b.getNodeById(currLastService.getServiceConfig().getRoute());
                        if (lastNodeInChain == null) {
                            lastNodeInChain = b.addNode(currLastService.getServiceConfig().getRoute(),
                                    GraphBuilder.NodeType.service,
                                    lastServiceOnChainBaseURI);
                        }
                        b.addEdge(lastNodeInChain, callbackQueueNode);
                    }
                }
            }
        }
    }

    /**
     * Returns unique services based on their name
     *
     * @param graph service graph
     */
    private Collection<Service> getUniqueServices(ServiceGraph graph) {
        // Get a unique list of all the services based on there name
        Map<String, Service> uniqueServices = new HashMap<>();
        for (Service currService : graph.getServices()) {
            uniqueServices.put(currService.getServiceConfig().getServiceURI(), currService);
        }
        return uniqueServices.values();
    }

    private int getActiveInstances(ServiceConfig serviceConfig) {
        int activeInstances = 0;
        return activeInstances;
    }

    private void addDatasourceNodes(GraphBuilder b, Service currService, Node serviceNode) {
        for (ResourceConfig currDS : currService.getDatasourcesByName().values()) {
            Node dsNode = b.getNodeById(currDS.getName());
            if (dsNode == null) {
                dsNode =
                        b.addNode(currDS.getName(), GraphBuilder.NodeType.db, currDS.getName(), currDS.getProperties());
            }

            //todo: add maxConnections as label
            b.addEdge(dsNode, serviceNode, null, "dash-line");
        }
    }

    private void addBlobstoreNodes(GraphBuilder b, Service currService, Node serviceNode) {
        for (ResourceConfig currBlobStoreConfig : currService.getBlobStoresByName().values()) {
            Node dsNode = b.getNodeById(currBlobStoreConfig.getName());
            if (dsNode == null) {
                dsNode = b.addNode(currBlobStoreConfig.getName(),
                        GraphBuilder.NodeType.blobStore,
                        currBlobStoreConfig.getName(),
                        currBlobStoreConfig.getProperties());
            }

            b.addEdge(dsNode, serviceNode, null, "dash-line");
        }
    }

    @Override
    public Response getHtml() {
        return readStaticResource("index.html");
    }

    @Override
    public Response getCSS() {
        return readStaticResource("vis.css");
    }

    @Override
    public Response getCode() {
        return readStaticResource("vis.js");
    }

    private Response readStaticResource(final String finalResourceName) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(finalResourceName);
                IOUtils.copy(resourceAsStream, output);
            }
        };

        return Response.ok(streamingOutput).build();
    }
}
