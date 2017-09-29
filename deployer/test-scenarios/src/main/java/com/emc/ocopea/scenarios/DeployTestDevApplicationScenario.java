// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 6/20/16.
 * Drink responsibly
 */
public class DeployTestDevApplicationScenario extends BaseScenario {

    private final String templateIdKeyIn;
    private UUID appTemplateId;
    private UUID siteId;
    private final String siteIdKeyIn;
    private final String appInstanceName;
    private String appInstanceIdKeyOut;

    public DeployTestDevApplicationScenario(
            String appInstanceName,
            String appTemplateIdKeyIn,
            String siteIdKeyIn,
            String appInstanceIdKeyOut) {
        this.templateIdKeyIn = appTemplateIdKeyIn;
        this.appInstanceName = appInstanceName;
        this.appInstanceIdKeyOut = appInstanceIdKeyOut;
        this.siteIdKeyIn = siteIdKeyIn;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        this.appTemplateId = getFromContext(context, templateIdKeyIn, UUID.class);
        this.siteId = getFromContext(context, siteIdKeyIn, UUID.class);
    }

    @Override
    protected Map<String, Object> executeScenario() {

        // Deploy the hackathon app template
        final Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("appTemplateId", appTemplateId.toString());
        tokenValues.put("appInstanceName", appInstanceName);
        tokenValues.put("siteId", siteId.toString());

        populateServiceConfigurationParams(tokenValues, siteId, appTemplateId);

        final Map<String, Object> contextToReturn = new HashMap<>();
        postJson(
                "hub-web",
                "commands/deploy-test-dev-app",
                readResourceAsString("deploy-hackathon/deploy-test-dev-command-args.json", tokenValues),
                (r) -> {
                    // Testing that the command succeeded
                    Assert.assertEquals(
                            "Failed executing deploy-test-dev-app command",
                            Response.Status.CREATED.getStatusCode(),
                            r.getStatus());

                    final UUID appInstanceId = r.readEntity(UUID.class);
                    Assert.assertNotNull(appInstanceId);
                    contextToReturn.put(appInstanceIdKeyOut, appInstanceId);

                    String state = waitForAppToDeploy(appInstanceId);
                    Assert.assertEquals("RUNNING", state);
                });
        return contextToReturn;
    }
}
