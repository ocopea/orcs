// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.site.app;

import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by liebea on 10/18/16.
 * Drink responsibly
 */
public class DeployedApplicationTest {

    private Collection<DeployedApplicationEvent> roll(DeployedApplication deployedApplication) {
        Collection<DeployedApplicationEvent> eventsTriggered = Collections.emptyList();
        if (deployedApplication.getExecutionEvent() != null) {
            eventsTriggered = deployedApplication.rollEvent(deployedApplication.getExecutionEvent());
            deployedApplication.markAsClean();
        }
        return eventsTriggered;
    }

    @Test
    public void testCreate() {
        DeployedApplicationBuilder builder = getBuilder();

        DeployedApplication app = new DeployedApplication();
        app.create(builder);
        app.markAsClean();

        // until binding data services, app services should be pending
        app.getDeployedAppServices().values()
                .forEach(deployedAppService ->
                        Assert.assertEquals(DeployedAppServiceState.pending, deployedAppService.getState()));

        Assert.assertEquals("myAppInstance", app.getName());
        Assert.assertEquals(DeployedApplicationState.deploying, app.getState());

        roll(app.markDataServiceAsCreating("db1BindName"));
        roll(app.markDataServiceAsCreated("db1BindName"));
        roll(app.markDataServiceAsBinding("db1BindName"));
        roll(app.markDataServiceAsBound("db1BindName", new DataServiceBoundEvent.BindingInfo(
                Collections.emptyMap(),
                Collections.emptyList())));
        roll(app.markDataServiceAsCreating("db2BindName"));
        roll(app.markDataServiceAsCreated("db2BindName"));
        roll(app.markDataServiceAsBinding("db2BindName"));
        roll(app.markDataServiceAsBound("db2BindName", new DataServiceBoundEvent.BindingInfo(
                Collections.emptyMap(),
                Collections.emptyList())));

        // Binding data services should make app services queued
        app.getDeployedAppServices().values()
                .forEach(deployedAppService ->
                        Assert.assertEquals(DeployedAppServiceState.queued, deployedAppService.getState()));

        roll(app.markAppServiceAsDeploying("svc1"));
        roll(app.markAppServiceAsDeployed("svc1", "http://svc1.com"));
        roll(app.markAppServiceAsDeploying("svc2"));
        roll(app.markAppServiceAsDeployed("svc2", null));

        Assert.assertEquals(DeployedApplicationState.running, app.getState());
        Assert.assertEquals("http://svc1.com", app.getEntryPointURL());

    }

    @Test
    public void testIdempotency() {
        DeployedApplicationBuilder builder = getBuilder();

        DeployedApplication app = new DeployedApplication();
        app.create(builder);
        app.markAsClean();

        // until binding data services, app services should be pending
        app.getDeployedAppServices().values()
                .forEach(deployedAppService ->
                        Assert.assertEquals(DeployedAppServiceState.pending, deployedAppService.getState()));
        Assert.assertEquals(DeployedApplicationState.deploying, app.getState());


        roll(app.markDataServiceAsCreating("db2BindName"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));
        roll(app.markDataServiceAsCreated("db2BindName"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));
        roll(app.markDataServiceAsBinding("db2BindName"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));
        Collection<DeployedApplicationEvent> downStreamEvents =
                roll(app.markDataServiceAsBound("db2BindName", new DataServiceBoundEvent.BindingInfo(
                        Collections.emptyMap(),
                        Collections.emptyList())));

        // Expecting two app services to be queued
        Assert.assertEquals(1, downStreamEvents.size());
        downStreamEvents.forEach(event ->
                Assert.assertEquals(AppServiceStateChangeEvent.class, event.getClass())
        );

        roll(app.markDataServiceAsBound("db2BindName", new DataServiceBoundEvent.BindingInfo(
                Collections.emptyMap(),
                Collections.emptyList())))
                .forEach(event ->  Assert.fail("Not idempotent! " + event.toString()));


        roll(app.markDataServiceAsCreating("db1BindName"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));

        roll(app.markDataServiceAsCreated("db1BindName"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));
        roll(app.markDataServiceAsBinding("db1BindName"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));

        downStreamEvents = roll(app.markDataServiceAsBound("db1BindName", new DataServiceBoundEvent.BindingInfo(
                Collections.emptyMap(),
                Collections.emptyList())));

        // Expecting two app services to be queued
        Assert.assertEquals(1, downStreamEvents.size());
        downStreamEvents.forEach(event ->
                Assert.assertEquals(AppServiceStateChangeEvent.class, event.getClass())
        );

        roll(app.markDataServiceAsBound("db1BindName", new DataServiceBoundEvent.BindingInfo(
                Collections.emptyMap(),
                Collections.emptyList())))
                .forEach(event ->  Assert.fail("Not idempotent! " + event.toString()));

        // Binding data services should make app services queued
        app.getDeployedAppServices().values()
                .forEach(deployedAppService ->
                        Assert.assertEquals(DeployedAppServiceState.queued, deployedAppService.getState()));

        roll(app.markAppServiceAsDeploying("svc1"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));
        roll(app.markAppServiceAsDeployed("svc1", "http://svc1.com"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));
        roll(app.markAppServiceAsDeploying("svc2"))
                .forEach(event ->  Assert.fail("Unexpected downstream event" + event.toString()));
        downStreamEvents = roll(app.markAppServiceAsDeployed("svc2", null));

        // Expecting app deployed successfully event to be triggered
        Assert.assertEquals(1, downStreamEvents.size());
        Assert.assertTrue(downStreamEvents.iterator().next().getClass().equals
                (ApplicationDeployedSuccessfullyEvent.class));

        Assert.assertEquals(DeployedApplicationState.running, app.getState());
        Assert.assertEquals("http://svc1.com", app.getEntryPointURL());

    }

    private DeployedApplicationBuilder getBuilder() {
        final UUID appInstanceId = UUID.randomUUID();
        return new DeployedApplicationBuilder(appInstanceId, "myAppInstance", "template", "1", "svc1")
                .withAppService(
                        "svc1",
                        "psb1",
                        "psb1Url",
                        "svc1",
                        "ShpanSpace",
                        "artifactRegistry1",
                        "img1",
                        "java",
                        "2",
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        "pepe",
                        Collections.singleton(80),
                        80)
                .withAppService(
                        "svc2",
                        "psb1",
                        "psb1Url",
                        "svc2",
                        "ShpanSpace",
                        "artifactRegistry1",
                        "img2",
                        "java",
                        "3.01",
                        Collections.emptyMap(),
                        Collections.emptyMap(),
                        "pop",
                        Collections.singleton(8080),
                        8080)
                .withDataService("dsb1", "http://dsb1", "plan-a", "db1BindName", "db1Id", new HashMap<>())
                .withDataService("dsb2", "http://dsb2", "plan-b", "db2BindName", "db2Id", new HashMap<>())
                .withDataServiceMappings("svc1", "dsb1", "db1BindName")
                .withDataServiceMappings("svc1", "dsb2", "db2BindName")
                .withDataServiceMappings("svc2", "dsb2", "db2BindName");
    }

    @Test
    public void testCreateDuplicate() {
        DeployedApplicationBuilder builder = getBuilder();

        DeployedApplication app = new DeployedApplication();
        app.create(builder);
        app.markAsClean();

        // until binding data services, app services should be pending
        app.getDeployedAppServices().values()
                .forEach(deployedAppService ->
                        Assert.assertEquals(DeployedAppServiceState.pending, deployedAppService.getState()));

        Assert.assertEquals("myAppInstance", app.getName());
        Assert.assertEquals(DeployedApplicationState.deploying, app.getState());

        try {
            roll(app.create(builder));
            Assert.fail("Creating app twice should have thrown an exception");
        } catch (Exception ex) {
            Assert.assertNotNull(ex.getMessage());
        }
    }
}
