// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.registry.ServiceRegistryApi;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.serialization.SerializationManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

/**
 * Created by martiv6 on 25/02/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageSenderImplTest {

    @Mock
    RuntimeMessageSender runtimeMessageSender;
    @Mock
    SerializationManager serializationManager;
    @Mock
    ResourceProvider resourceProvider;
    @Mock
    ServiceRegistryApi serviceRegistryApi;
    @Mock
    Context context;

    @InjectMocks
    @Spy
    MessageSenderImpl messageSender;

    @Test
    public void testStreamMessage_validateRoutingPlanHeader() {

        final MessageRoutingPlan routingPlan = MessageRoutingPlan.builder()
                .withRoute("microservice1", "microservice1.queue.output", "blob=store", "runId", true)
                .withRoute("microservice2", "microservice2.queue.output", "blob=store", "runId", true)
                .withRoute("microservice3", "microservice3.queue.output", "blob=store", "runId", true)
                .withRoute("microservice4", "microservice4.queue.output", "blob=store", "runId", true)
                .withRoute("microservice5", "microservice5.queue.output", "blob=store", "runId", true)
                .build();

        Mockito.when(resourceProvider.getServiceRegistryApi()).thenReturn(serviceRegistryApi);
        Mockito.when(resourceProvider.getQueueConfigurationClass()).thenReturn(QueueConfiguration.class);
        Mockito
                .when(serviceRegistryApi.getQueueConfiguration(eq(QueueConfiguration.class), anyString(), eq(context)))
                .thenReturn(mock(QueueConfiguration.class));

        MessageWriter messageWriter = mock(MessageWriter.class);
        String messageGroup = "message-group";
        Map<String, String> messageHeaders = mock(Map.class);
        Map<String, String> messageContext = mock(Map.class);
        messageSender.streamMessage(messageWriter, messageHeaders, messageContext, routingPlan, messageGroup);

        ArgumentCaptor<Map> actualMessageHeadersCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito
                .verify(runtimeMessageSender)
                .streamMessage(eq(messageWriter), actualMessageHeadersCaptor.capture(), eq(messageGroup));

        Map<String, String> actualMessageHeaders = actualMessageHeadersCaptor.getValue();

        assertEquals(
                "Unexpected route properties in message headers",
                "blobstoreKeyHeaderName,runId,logInDebug,true,destinationQueueURI,microservice1.queue.output,blobstoreNameSpace,blob=store",
                actualMessageHeaders.get("__contextHeader__MS_API_OUTPUT_QUEUE_NAME_microservice1"));
        assertEquals(
                "Unexpected route properties in message headers",
                "blobstoreKeyHeaderName,runId,logInDebug,true,destinationQueueURI,microservice2.queue.output,blobstoreNameSpace,blob=store",
                actualMessageHeaders.get("__contextHeader__MS_API_OUTPUT_QUEUE_NAME_microservice2"));
        assertEquals(
                "Unexpected route properties in message headers",
                "blobstoreKeyHeaderName,runId,logInDebug,true,destinationQueueURI,microservice3.queue.output,blobstoreNameSpace,blob=store",
                actualMessageHeaders.get("__contextHeader__MS_API_OUTPUT_QUEUE_NAME_microservice3"));
        assertEquals(
                "Unexpected route properties in message headers",
                "blobstoreKeyHeaderName,runId,logInDebug,true,destinationQueueURI,microservice4.queue.output,blobstoreNameSpace,blob=store",
                actualMessageHeaders.get("__contextHeader__MS_API_OUTPUT_QUEUE_NAME_microservice4"));
        assertEquals(
                "Unexpected route properties in message headers",
                "blobstoreKeyHeaderName,runId,logInDebug,true,destinationQueueURI,microservice5.queue.output,blobstoreNameSpace,blob=store",
                actualMessageHeaders.get("__contextHeader__MS_API_OUTPUT_QUEUE_NAME_microservice5"));
        assertEquals(
                "Unexpected routing plan in message headers",
                "microservice1,microservice2,microservice3,microservice4,microservice5",
                actualMessageHeaders.get("__contextHeader__MS_API_ROUTING_PLAN"));

    }

    @Test
    public void testStreamMessage_validateRoutingPlanHeaderWhenNoRoutingPlanProvided() {

        final MessageRoutingPlan routingPlan = null;

        Mockito.when(resourceProvider.getServiceRegistryApi()).thenReturn(serviceRegistryApi);
        Mockito.when(resourceProvider.getQueueConfigurationClass()).thenReturn(QueueConfiguration.class);
        Mockito
                .when(serviceRegistryApi.getQueueConfiguration(eq(QueueConfiguration.class), anyString(), eq(context)))
                .thenReturn(mock(QueueConfiguration.class));

        MessageWriter messageWriter = mock(MessageWriter.class);
        String messageGroup = "message-group";
        Map<String, String> messageHeaders = mock(Map.class);
        Map<String, String> messageContext = mock(Map.class);
        messageSender.streamMessage(messageWriter, messageHeaders, messageContext, routingPlan, messageGroup);

        ArgumentCaptor<Map> actualMessageHeadersCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito
                .verify(runtimeMessageSender)
                .streamMessage(eq(messageWriter), actualMessageHeadersCaptor.capture(), eq(messageGroup));

        Map<String, String> actualMessageHeaders = actualMessageHeadersCaptor.getValue();

        assertTrue("Expected empty message headers", actualMessageHeaders.size() == 0);

    }

}
