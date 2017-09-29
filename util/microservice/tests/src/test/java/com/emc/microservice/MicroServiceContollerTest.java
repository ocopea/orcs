// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice;

import com.emc.microservice.config.ConfigurationAPI;
import com.emc.microservice.resource.ResourceConfigurationProperty;
import com.emc.microservice.resource.ResourceConfigurationPropertyType;
import com.emc.microservice.resource.ResourceProvider;
import com.emc.microservice.restapi.WebServerConfiguration;
import com.emc.ocopea.util.MapBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MicroServiceContollerTest {
    private static final Logger log = LoggerFactory.getLogger(MicroServiceContollerTest.class);

    @Test(timeout = 10000)
    public void testLifeCycle() throws InterruptedException {
        ResourceProvider resourceProvider = new ResourceProvider(new InMemoryConfigurationAPI());
        final MicroServiceController controller = new MicroServiceController(
                resourceProvider,
                new TestMicroService(),
                "TEST-api");
        new Thread(controller::start).start();

        Thread.sleep(100);
        Assert.assertEquals(
                "service controller start called but lacking configuration",
                MicroServiceState.STARTING,
                controller.getState());
        resourceProvider.getServiceRegistryApi().registerWebServer("default", new MockWebServerConfiguration(8080));
        resourceProvider.getServiceRegistryApi().registerServiceConfig(
                "TEST-api",
                ServiceConfig.generateServiceConfig(
                        "TEST-api",
                        null,
                        null,
                        null,
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        MapBuilder.<String, String>newHashMap().with("foo", "bar").build(),
                        Collections.emptyMap()
                ));
        while (controller.getState() == MicroServiceState.STARTING) {
            Thread.sleep(1000);
        }
        Assert.assertEquals(
                "service controller start called, configuration supplied",
                MicroServiceState.RUNNING,
                controller.getState());
        controller.stop();
        while (controller.getState() == MicroServiceState.RUNNING) {
            Thread.sleep(1000);
        }
        Assert.assertEquals(
                "service controller stop called",
                MicroServiceState.STOPPED,
                controller.getState());
    }

    public static class MockWebServerConfiguration extends WebServerConfiguration {
        private static final ResourceConfigurationProperty PROPERTY_PORT =
                new ResourceConfigurationProperty("port", ResourceConfigurationPropertyType.INT, "port", true, false);

        public MockWebServerConfiguration() {
            super("Mock WebServer", Collections.singletonList(PROPERTY_PORT));
        }

        private MockWebServerConfiguration(int port) {
            this();
            setPropertyValues(propArrayToMap(new String[]{PROPERTY_PORT.getName(), String.valueOf(port)}));
        }
    }

    private static class TestMicroService extends MicroService {

        private TestMicroService() {
            super(
                    "TEST",
                    "T",
                    "foo bar",
                    1,
                    log,
                    new MicroServiceInitializationHelper()
                            .withParameter("foo", "foo param", null, true));
        }
    }


    private static class InMemoryConfigurationAPI implements ConfigurationAPI {
        private Map<String, Object> configuration = new HashMap<>();

        @Override
        public Collection<String> list(String path) {
            return getNode(path, false).keySet();
        }

        @Override
        public String readData(String path) {
            int lastSlashIndex = path.lastIndexOf("/");
            String dirPath = path.substring(0, lastSlashIndex >= 0 ? lastSlashIndex : 0);
            String localKey = path.substring(lastSlashIndex + 1);
            return (String) getNode(dirPath, false).get(localKey);
        }

        @Override
        public boolean isDirectory(String path) {
            Map<String, Object> node = getNode(path, false);
            return node != null;
        }

        @Override
        public boolean exists(String path) {
            int lastSlashIndex = path.lastIndexOf("/");
            String dirPath = path.substring(0, lastSlashIndex >= 0 ? lastSlashIndex : 0);
            String localKey = path.substring(lastSlashIndex + 1);
            Map<String, Object> node = getNode(dirPath, false);
            return node != null && node.containsKey(localKey);
        }

        @Override
        public void writeData(String path, String data) {
            int lastSlashIndex = path.lastIndexOf("/");
            String dirPath = path.substring(0, lastSlashIndex >= 0 ? lastSlashIndex : 0);
            String localKey = path.substring(lastSlashIndex + 1);
            getNode(dirPath, true).put(localKey, data);
        }

        @Override
        public void mkdir(String path) {
            getNode(path, true);
        }

        private Map<String, Object> getNode(String path, boolean createMissing) {
            Map<String, Object> node = configuration;
            for (String part : path.split("/")) {
                try {
                    node = getNextElement(node, part, createMissing);
                } catch (ClassCastException e) {
                    node = null;
                }
                if (node == null) {
                    return null;
                }
            }
            return node;
        }

        private Map<String, Object> getNextElement(
                Map<String, Object> node,
                String key,
                boolean createMissing) {
            if (createMissing) {
                return (Map<String, Object>) node.computeIfAbsent(key, (str) -> new HashMap<String, Object>());
            } else {
                return (Map<String, Object>) node.get(key);
            }
        }
    }
}
