// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.inspector;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.ServiceConfig;
import com.emc.microservice.inspector.graph.Queue;
import com.emc.microservice.inspector.graph.ServiceGraph2;
import com.emc.microservice.inspector.graph.ServiceGraphBuilder2;
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
import org.apache.commons.io.IOUtils;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by liebea on 1/7/15.
 * Drink responsibly
 */
public class InspectorResource2 implements InspectorAPI2 {

    private ServiceGraphBuilder2 builder;

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

        this.builder = new ServiceGraphBuilder2(serviceRegistryApi, messagingStatsConnection);
    }

    @Override
    public Graph getGraph() {
        // Building the services graph
        ServiceGraph2 graph = builder.build();
        // Creating the GraphBuilder for building the presentation representation of the graph
        GraphBuilder b = new GraphBuilder();
        addServices(graph, b);
        addQueueData(graph, b);
        addDatasourceData(graph, b);
        addBlobstoreData(graph, b);
        return b.build();
    }

    private void addDatasourceData(ServiceGraph2 graph, GraphBuilder b) {
        for (Map.Entry<String, Map<String, String>> currDS : graph.getDatasourceParameters().entrySet()) {
            b.withData(currDS.getKey(), currDS.getValue());
        }
    }

    private void addBlobstoreData(ServiceGraph2 graph, GraphBuilder b) {
        for (Map.Entry<String, Map<String, String>> currBlobStore : graph.getBlobstoreParameters().entrySet()) {
            b.withData(currBlobStore.getKey(), currBlobStore.getValue());
        }
    }

    private void addQueueData(ServiceGraph2 graph, GraphBuilder b) {
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

    private void addServices(ServiceGraph2 graph, GraphBuilder b) {
        // Iterating on services and adding to Graph
        for (ServiceConfig currService : graph.getServices()) {
            int activeInstances = getActiveInstances(currService);

            // Service node key is the registered route since it is unique
            Node serviceNode = b.addNode(
                    currService.getServiceURI(),
                    GraphBuilder.NodeType.service,
                    currService.getServiceURI() + " (" + Integer.toString(activeInstances) + ")");

            // Adding all properties as service node graph data
            b.withData(serviceNode.getId(), currService.getParameters());
            b.withData(serviceNode.getId(), "route", currService.getRoute());

            addInputs(b, currService, serviceNode);
            addOutputs(graph, b, currService, serviceNode);
            addDatasourceNodes(b, currService, serviceNode);
            addBlobstoreNodes(b, currService, serviceNode);
            addServiceDependencies(graph, b, currService, serviceNode);
        }
    }

    private void addOutputs(ServiceGraph2 graph, GraphBuilder b, ServiceConfig currService, Node serviceNode) {
        // Creating the main destination queue and edges for the service
        if (currService.getDestinationQueueConfig() != null) {
            for (Map.Entry<String, ServiceConfig.DestinationQueueConfig> currDestinationConfig : currService
                    .getDestinationQueueConfig()
                    .entrySet()) {
                Node outputNode = b.getNodeById(currDestinationConfig.getKey());
                if (outputNode == null) {
                    outputNode = b.addNode(currDestinationConfig.getKey(),
                            GraphBuilder.NodeType.queue,
                            currDestinationConfig.getKey());
                    Queue queue = graph.getInputQueuesByURI().get(currDestinationConfig.getKey());
                    if (queue != null) {
                        QueueConfiguration outputQueueConfiguration = queue.getInputQueueConfiguration();
                        if (outputQueueConfiguration != null) {
                            b.withData(outputNode.getId(), outputQueueConfiguration.getPublicPropertyValues());
                        }
                    }
                }
                b
                        .withData(
                                currDestinationConfig.getKey(),
                                "blobstoreKeyHeaderName",
                                currDestinationConfig.getValue().getBlobstoreKeyHeaderName())
                        .withData(
                                currDestinationConfig.getKey(),
                                "blobstoreNameSpace",
                                currDestinationConfig.getValue().getBlobstoreNameSpace())
                        .withData(
                                currDestinationConfig.getKey(),
                                "logInDebug",
                                String.valueOf(currDestinationConfig.getValue().isLogInDebug()));

                b.addEdge(serviceNode, outputNode);
            }
        }
    }

    private void addInputs(GraphBuilder b, ServiceConfig currService, Node serviceNode) {
        // Creating the input queue and edge for the service
        if (currService.getInputQueueConfig() != null) {
            for (Map.Entry<String, ServiceConfig.InputQueueConfig> currInputQueue : currService
                    .getInputQueueConfig()
                    .entrySet()) {
                String queueName = currInputQueue.getKey();
                Node queueNode = b.getNodeById(queueName);
                if (queueNode == null) {
                    queueNode = b.addNode(queueName, GraphBuilder.NodeType.queue, queueName);
                }
                b
                        .withData(queueName,
                                "numberOfConsumers",
                                String.valueOf(currInputQueue.getValue().getNumberOfConsumers()))
                        .withData(queueName, "logInDebug", String.valueOf(currInputQueue.getValue().isLogInDebug()));
                b.addEdge(queueNode, serviceNode);

                //todo:patchy dependency solution since we currently don't have this data in serviceConfig
                if (queueName.contains(".queues.dependency-callback.")) {
                    String targetServiceName = queueName.substring(queueName.lastIndexOf(".") + 1);
                    Node tgtSvc = b.getNodeById(targetServiceName);

                    if (tgtSvc != null) {
                        b.addEdge(tgtSvc, queueNode, "callback", "arrow");
                    }

                }

            }
        }
    }

    private void addServiceDependencies(
            ServiceGraph2 graph,
            GraphBuilder b,
            ServiceConfig currService,
            Node serviceNode) {
        // Adding Service dependencies
        //todo:support dependencies in new inspector
    }

    private int getActiveInstances(ServiceConfig sc) {
        //todo:do
        int activeInstances = 0;
        return activeInstances;
    }

    private void addDatasourceNodes(GraphBuilder b, ServiceConfig currService, Node serviceNode) {
        for (Map.Entry<String, ServiceConfig.DataSourceConfig> currDS : currService.getDataSourceConfig().entrySet()) {
            Node dsNode = b.getNodeById(currDS.getKey());
            if (dsNode == null) {
                dsNode = b.addNode(currDS.getKey(),
                        GraphBuilder.NodeType.db,
                        currDS.getKey(),
                        currDS.getValue().getProperties());
            }

            //todo: add maxConnections as label
            b.addEdge(dsNode, serviceNode, null, "dash-line");
        }
    }

    private void addBlobstoreNodes(GraphBuilder b, ServiceConfig currService, Node serviceNode) {
        for (Map.Entry<String, ServiceConfig.DataSourceConfig> currBlobStoreConfig : currService
                .getBlobstoreConfig()
                .entrySet()) {
            Node dsNode = b.getNodeById(currBlobStoreConfig.getKey());
            if (dsNode == null) {
                dsNode = b.addNode(currBlobStoreConfig.getKey(),
                        GraphBuilder.NodeType.blobStore,
                        currBlobStoreConfig.getKey(),
                        currBlobStoreConfig.getValue().getProperties());
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
