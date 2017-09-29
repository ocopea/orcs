// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.messaging;

import com.emc.microservice.Context;
import com.emc.microservice.ContextImpl;
import com.emc.microservice.dependency.ManagedDependency;
import com.emc.microservice.health.HealthCheckManager;
import com.emc.microservice.metrics.CounterMetric;
import com.emc.microservice.metrics.MetricsRegistry;
import com.emc.microservice.metrics.TimerMetric;
import com.emc.microservice.resource.ResourceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by martiv6 on 25/02/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class ManagedMessageListenerImplTest {

    @Mock
    Context context;

    @Mock
    MessageListener messageListener;

    @Mock
    ManagedInputQueue managedQueue;

    ManagedMessageListenerImpl managedMessageListener;

    @Before
    public void setup() {
        MetricsRegistry metricsRegistry = mock(MetricsRegistry.class);
        when(metricsRegistry.getCounter(Mockito.anyString(), Mockito.anyMap(), any(Class.class))).thenReturn(mock(
                CounterMetric.class));
        when(metricsRegistry.getTimerMetric(Mockito.anyString(), Mockito.anyMap(), any(Class.class))).thenReturn(mock(
                TimerMetric.class));
        when(context.getMetricsRegistry()).thenReturn(metricsRegistry);
        Logger logger = mock(Logger.class);
        when(logger.isDebugEnabled()).thenReturn(true);
        when(context.createSubLogger(any(Class.class))).thenReturn(logger);
        when(context.getHealthCheckManager()).thenReturn(mock(HealthCheckManager.class));
        when(managedQueue.getDescriptor()).thenReturn(mock(InputQueueDescriptor.class));
        managedMessageListener = Mockito.spy(new ManagedMessageListenerImpl(context, messageListener, managedQueue, 1));
    }

    @Test
    public void testHandleMessage() {
        // Validate that a non error message is sent to the underlying listener
        Message message = mock(Message.class);
        managedMessageListener.onMessage(message, context);
        verify(messageListener).onMessage(eq(message), eq(context));
    }

    @Test
    public void testHandleErrorMessage_CurrentMicroserviceIsTheLastOneInRoute() {
        // Validate that a non error message is sent to the underlying listener
        Message message = mock(Message.class);
        when(message.getContextValue(ContextImpl.MS_API_ROUTING_PLAN_HEADER)).thenReturn(
                "microservice1,microservice2,microservice3");
        when(context.getMicroServiceBaseURI()).thenReturn("microservice3");
        managedMessageListener.onErrorMessage(message, context);
        verify(messageListener).onErrorMessage(eq(message), eq(context));
    }

    @Test
    public void testHandleErrorMessage_CurrentMicroserviceIsTheFirstOneInRoute() {
        Message message = mock(Message.class);
        when(message.getContextValue(ContextImpl.MS_API_ROUTING_PLAN_HEADER)).thenReturn(
                "microservice1,microservice2,microservice3");
        when(message.getUnderlyingMessageObject()).thenReturn(Mockito.mock(Object.class));

        HashMap<String, String> messageHeaders = new HashMap();
        messageHeaders.put(MessagingConstants.FAILED_HEADER, "true");
        messageHeaders.put(
                MessagingConstants.ERROR_HEADER,
                "[{\"microservice_uri\":\"microservice-uri\",\"timestamp\":1456417079688,\"error_code\":500,\"error_message\":null}]");
        when(message.getMessageHeaders()).thenReturn(messageHeaders);

        HashMap<String, String> messageContext = new HashMap();
        messageContext.put(ContextImpl.MS_API_ROUTING_PLAN_HEADER, "microservice1,microservice2,microservice3");
        when(message.getMessageContext()).thenReturn(messageContext);
        when(context.getMicroServiceBaseURI()).thenReturn("microservice1");

        ResourceManager resourceManager = mock(ResourceManager.class);
        ManagedDependency managedResource = mock(ManagedDependency.class);
        MessageSender messageSender = Mockito.mock(MessageSender.class);
        when(managedResource.getMessageSender()).thenReturn(messageSender);
        when(resourceManager.getManagedResourceByName(eq("microservice3"))).thenReturn(managedResource);

        when(context.getDependencyManager()).thenReturn(resourceManager);
        managedMessageListener.onErrorMessage(message, context);

        // Check that the underlying message listener is never called
        verify(messageListener, Mockito.never()).onErrorMessage(eq(message), eq(context));

        // Instead, the message is sent to the last microservice
        verify(messageSender).sendMessage(any(Class.class), anyObject(), eq(messageHeaders), eq(messageContext));
    }

    @Test
    public void testHandleErrorMessage_CurrentMicroserviceIsInTheMiddleOfRoute() {
        Message message = mock(Message.class);
        when(message.getContextValue(ContextImpl.MS_API_ROUTING_PLAN_HEADER)).thenReturn(
                "microservice1,microservice2,microservice3");
        when(message.getUnderlyingMessageObject()).thenReturn(Mockito.mock(Object.class));

        HashMap<String, String> messageHeaders = new HashMap();
        messageHeaders.put(MessagingConstants.FAILED_HEADER, "true");
        messageHeaders.put(
                MessagingConstants.ERROR_HEADER,
                "[{\"microservice_uri\":\"microservice-uri\",\"timestamp\":1456417079688,\"error_code\":503,\"error_message\":unexpected error}]");
        when(message.getMessageHeaders()).thenReturn(messageHeaders);

        HashMap<String, String> messageContext = new HashMap();
        messageContext.put(ContextImpl.MS_API_ROUTING_PLAN_HEADER, "microservice1,microservice2,microservice3");
        when(message.getMessageContext()).thenReturn(messageContext);
        when(context.getMicroServiceBaseURI()).thenReturn("microservice2");

        ResourceManager resourceManager = mock(ResourceManager.class);
        ManagedDependency managedResource = mock(ManagedDependency.class);
        MessageSender messageSender = Mockito.mock(MessageSender.class);
        when(managedResource.getMessageSender()).thenReturn(messageSender);
        when(resourceManager.getManagedResourceByName(eq("microservice3"))).thenReturn(managedResource);

        when(context.getDependencyManager()).thenReturn(resourceManager);
        managedMessageListener.onErrorMessage(message, context);

        // Check that the underlying message listener is never called
        verify(messageListener, Mockito.never()).onErrorMessage(eq(message), eq(context));

        // Instead, the message is sent to the last microservice
        verify(messageSender).sendMessage(any(Class.class), anyObject(), eq(messageHeaders), eq(messageContext));
    }
}
