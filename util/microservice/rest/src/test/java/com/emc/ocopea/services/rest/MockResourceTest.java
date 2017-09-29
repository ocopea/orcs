// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.services.rest;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.schedule.ScheduleListener;
import com.emc.microservice.testing.MicroServiceTestHelper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.plugins.server.resourcefactory.POJOResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;

public abstract class MockResourceTest {

    @Before
    public void init() {

    }

    @After
    public void tearDown() {
        microServiceTestHelper.stopTestMode();
    }

    public static class MockMicroServiceApplication extends Application implements MicroServiceApplication {
        private final Context context;

        public MockMicroServiceApplication(Context context) {
            this.context = context;
        }

        @Override
        public Context getMicroServiceContext() {
            return context;
        }

        @Override
        public Application getJaxRSApplication() {
            return this;
        }
    }

    public static class MockMicroService extends MicroService {
        private static final Logger logger = LoggerFactory.getLogger(MockMicroService.class);

        public MockMicroService() {
            super(
                    "Mock Micro Service",
                    "mock-api",
                    "A microservice of mockery",
                    77,
                    logger,
                    new MicroServiceInitializationHelper()
                            .withDatasource("mock-db", "Database where mockery is stored")
                            .withParameter(
                                    "boo-timeout",
                                    "Maximum time (in seconds) before audience boos",
                                    60 * 60 * 24)
                            .withInputQueue("ListenerQueue", "Listener Data Processor Queue",
                                    DummyMessageListener.class,
                                    null,
                                    "module", "function", "hostname")
                            .withDestination("PutDownRetorts", "Listener Data Processor Topic")
                            .withRestResource(APIMetaResource.class, "REST end point description")
                            .withScheduler("persistent-scheduler")
                            .withSchedulerListenerMapping(
                                    DummyScheduleListener.SCHEDULE_LISTENER_IDENTIFIER,
                                    DummyScheduleListener.class
                            )
            );
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected void initializeService(Context context) {
            System.out.println("HELP");
            context
                    .getSchedulerManager()
                    .getManagedResourceByName("persistent-scheduler")
                    .create("my-recurring-task", 1, DummyScheduleListener.SCHEDULE_LISTENER_IDENTIFIER);
        }

        public static class DummyMessageListener implements MessageListener {
            @Override
            public void onMessage(Message message, Context context) {
            }

            @Override
            public void onErrorMessage(Message message, Context context) {

            }
        }

        public static class DummyScheduleListener implements ScheduleListener {
            public static final String SCHEDULE_LISTENER_IDENTIFIER = "mock-recurring-task";
            @Override
            public boolean onTick(Message message) {
                System.out.println("DummyScheduleListener handling message=" + message);
                return true;
            }
        }
    }

    protected final MockMicroService service;
    protected final MicroServiceTestHelper microServiceTestHelper;
    protected final Dispatcher dispatcher;

    public MockResourceTest(Class<?>... resourceClasses) {

        final Map<Class<?>, Object> context = ResteasyProviderFactory.getContextDataMap();
        context.put(Dispatcher.class, dispatcher = MockDispatcherFactory.createDispatcher());
        service = new MockMicroService();
        microServiceTestHelper = new MicroServiceTestHelper(service).withSchedulerListenerMapping
                ("mock-recurring-task", MockMicroService.DummyScheduleListener.class);
        microServiceTestHelper.startServiceInTestMode();
        context.put(Application.class, new MockMicroServiceApplication(microServiceTestHelper.getContext()));

        for (final Class<?> resourceClass : resourceClasses) {
            dispatcher.getRegistry().addResourceFactory(new POJOResourceFactory(resourceClass));
        }

        dispatcher.getProviderFactory().registerProvider(ResteasyJacksonProvider.class);

    }

    protected Document xml(final MockHttpResponse response) throws DocumentException {
        return new SAXReader().read(new ByteArrayInputStream(response.getOutput()));
    }

    protected MockHttpResponse get(final String path, final MediaType applicationXmlType) throws URISyntaxException {
        final MockHttpRequest request = MockHttpRequest.get(path);
        request.accept(Arrays.asList(applicationXmlType));
        final MockHttpResponse response = new MockHttpResponse();

        dispatcher.invoke(request, response);
        return response;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, ?> json(final MockHttpResponse response) throws IOException {
        final JsonFactory factory = new JsonFactory();
        final JsonParser parser = factory.createParser(response.getOutput());
        final ObjectMapper objectMapper = new ObjectMapper(factory);
        return objectMapper.readValue(parser, Map.class);
    }

}
