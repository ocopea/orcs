// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.junit.Assert;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Created by liebea on 6/20/16.
 * Drink responsibly
 */
public abstract class BaseScenario {
    private Client client = null;
    private ServiceWebTargetResolver webTargetResolver;

    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        this.webTargetResolver = webTargetResolver;
    }

    protected <T> T getFromContext(ScenarioRunner.ScenarioExecutionContext context, String name, Class<T> clazz) {
        Object object = context.getLatest(name);
        if (object == null) {
            throw new IllegalArgumentException("missing " + name + " in scenario context");
        }
        if (!clazz.isInstance(object)) {
            throw new IllegalArgumentException(
                    name + " in scenario context is not " + clazz.getName() +
                    " but " + object.getClass().getName());
        }
        return clazz.cast(object);
    }

    protected abstract Map<String, Object> executeScenario();

    private WebTarget getTarget(String serviceURN, String path) {
        return getWebTargetResolver().resolveWebTarget(serviceURN, path);
    }

    private ServiceWebTargetResolver getWebTargetResolver() {
        return Objects.requireNonNull(webTargetResolver, "Scenario Not initialized. must invoke the init method");
    }

    public interface RestConverter<T> {
        T convert(Response r);
    }

    public interface RestConsumer<T> {
        void consume(Response r, T value);
    }

    protected <T> T doGet(String serviceURN, String path, Class<T> defaultConversionClass) {
        return doGet(serviceURN, path, defaultConversionClass, null);
    }

    protected <T> T doGet(String serviceURN, String path, Class<T> defaultConversionClass, RestConsumer<T> consumer) {
        return doGet(serviceURN, path, r -> r.readEntity(defaultConversionClass), consumer);
    }

    protected <T> T doGet(String serviceURN, String path, RestConverter<T> converter) {
        return doGet(serviceURN, path, converter, null);
    }

    protected <T> T doGet(String serviceURN, String path, RestConverter<T> converter, RestConsumer<T> consumer) {
        Invocation invocation = getTarget(serviceURN, path).request().buildGet();
        Response r = invoke(invocation);
        try {
            if (r.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                Assert.fail("Http response " + r.getStatusInfo().getReasonPhrase()
                        + (r.hasEntity() ? ": " + r.readEntity(String.class) : ""));
            }
            T value = converter.convert(r);
            if (consumer != null) {
                consumer.consume(r, value);
            }
            return value;
        } finally {
            r.close();
        }
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    protected void postJson(String serviceURN, String path, InputStream jsonInputStream, Consumer<Response> consumer) {
        postJson(serviceURN, path, convertStreamToString(jsonInputStream), consumer);
    }

    protected void postJson(String serviceURN, String path, String json, Consumer<Response> consumer) {

        Invocation invocation = getTarget(serviceURN, path)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .buildPost(Entity.json(json));
        Response r = invoke(invocation);
        try {
            consumer.accept(r);
        } finally {
            r.close();
        }
    }

    private Response invoke(Invocation invocation) {
        return invocation.invoke();
    }

    protected String readResourceAsString(String name) {
        return readResourceAsString(name, Collections.emptyMap());
    }

    protected String readResourceAsString(String name, Map<String, String> tokenValues) {
        try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(name);
             Reader r = new InterpolationFilterReader(
                     new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8),
                     new HashMap<>(tokenValues))) {
            return IOUtils.toString(r);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    protected String getAppTemplateIdFromName(String appTemplateName) {
        final Map[] templates = doGet("hub-web", "app-template", Map[].class);
        Assert.assertNotNull("App Templates returned null", templates);
        Assert.assertTrue("No app templates found", templates.length > 0);
        final Optional<Map> first =
                Arrays.stream(templates).filter(map -> map.get("name").equals(appTemplateName)).findFirst();
        Assert.assertTrue("App Template by name " + appTemplateName + " not found",first.isPresent());
        final String id = (String) first.get().get("id");
        Assert.assertNotNull(appTemplateName + " didn't return an id", id);
        try {
            UUID.fromString(id);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Invalid UUID " + id);
        }
        return id;
    }

    protected String waitForAppToDeploy(UUID appInstanceId) {
        int attempt = 0;
        String state = null;

        int maxRetries = 20;
        while (attempt < maxRetries) {
            Map stateMap = doGet("hub-web", "app-instance/" + appInstanceId + "/state", Map.class, (response, value) ->
                    Assert.assertEquals(
                            "Failed getting app instance status",
                            Response.Status.OK.getStatusCode(),
                            response.getStatus()));

            state = stateMap.get("state").toString();

            System.out.println(state);
            switch (state) {
                case "DEPLOYING":
                    sleepNoException(3000);
                    break;
                case "RUNNING":
                    attempt = maxRetries;
                    break;
                default:
                    Assert.fail("App became with status: " + state);
            }

            ++attempt;
        }
        return state;
    }

    protected String waitForAppToStop(UUID appInstanceId) {
        int attempt = 0;
        String state = null;

        int maxRetries = 20;
        while (attempt < maxRetries) {
            Map stateMap = doGet("hub-web", "app-instance/" + appInstanceId + "/state", Map.class, (response, value) ->
                    Assert.assertEquals("Failed getting app instance status",
                            Response.Status.OK.getStatusCode(), response.getStatus()));

            state = stateMap.get("state").toString();

            System.out.println(state);
            switch (state) {
                case "STOPPING":
                    sleepNoException(3000);
                    break;
                case "STOPPED":
                    attempt = maxRetries;
                    break;
                default:
                    Assert.fail("App became with status: " + state);
            }

            ++attempt;
        }
        return state;
    }

    protected void sleepNoException(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void populateServiceConfigurationParams(Map<String, String> tokenValues,
                                                      UUID siteId,
                                                      UUID appTemplateId) {
        final Map siteSpaces = doGet(
                "hub-web",
                "site-topology/" + siteId.toString(),
                Map.class
        );

        final List spacesList = (List)siteSpaces.get("spaces");
        if (spacesList.isEmpty()) {
            Assert.fail("site " + siteId + " has no spaces");
        }
        final String space = spacesList.iterator().next().toString();

        final Map supportedConfigurationsOnSite = doGet(
                "hub-web",
                "test-dev/site/" + siteId.toString() + "/app-template-configuration/" + appTemplateId.toString(),
                Map.class
        );

        List appServiceConfigurations = (List) supportedConfigurationsOnSite.get("appServiceConfigurations");
        Map supportedVersions = (Map)((Map)(appServiceConfigurations.get(0))).get("supportedVersions");

        if (supportedVersions.isEmpty()) {
            Assert.fail("No supported configuration for app on site");
        }
        final Map.Entry<?,?> entry = (Map.Entry<?, ?>) supportedVersions.entrySet().iterator().next();
        tokenValues.put("artifactRegistryName", entry.getKey().toString());
        tokenValues.put("hackSvcVersion", ((List)entry.getValue()).iterator().next().toString());
        tokenValues.put("space", space);

        parseDataService(tokenValues, supportedConfigurationsOnSite, "hackathon-db", "db");
        parseDataService(tokenValues, supportedConfigurationsOnSite, "hack-docs", "blob");
    }

    private void parseDataService(Map<String, String> tokenValues,
                                  Map supportedConfigurationsOnSite,
                                  String dataServiceName,
                                  String prefix) {
        //noinspection unchecked
        ((List<Map>)supportedConfigurationsOnSite.get("dataServiceConfigurations"))
                .stream()
                .filter(m -> dataServiceName.equals(m.get("dataServiceName").toString()))
                .forEach(map -> {
                    final Map dsbPlans = (Map)((Map) map.get("dsbPlans")).values().iterator().next();
                    tokenValues.put(prefix + "DsbUrn", dsbPlans.get("name").toString());
                    final Map plan = (Map) ((List) dsbPlans.get("plans")).get(0);
                    tokenValues.put(prefix + "DsbPlan", plan.get("id").toString());
                    tokenValues.put(prefix + "DsbProtocol", ((List)plan.get("protocols")).get(0).toString());
                });
    }

}
