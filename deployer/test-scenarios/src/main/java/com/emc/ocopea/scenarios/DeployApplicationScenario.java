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
public class DeployApplicationScenario extends BaseScenario {

    private final String templateIdKeyIn;
    private UUID appTemplateId;
    private UUID siteId;
    private final String siteIdKeyIn;
    private final String appInstanceName;
    private String appInstanceIdKeyOut;

    public DeployApplicationScenario(
            String appInstanceName,
            String appTemplateIdKeyIn,
            String siteIdKeyIn,
            String appInstanceIdKeyOut) {
        this.templateIdKeyIn = appTemplateIdKeyIn;
        this.appInstanceName = appInstanceName;
        this.siteIdKeyIn = siteIdKeyIn;
        this.appInstanceIdKeyOut = appInstanceIdKeyOut;
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

        // todo:Ugliest hack ever... do more functional? promise? future? huh?
        final Map<String, Object> contextToReturn = new HashMap<>();
        postJson(
                "hub-web",
                "commands/deploy-app",
                readResourceAsString("deploy-hackathon/deploy-command-args.json", tokenValues),
                (r) -> {
                    // Testing that the command succeeded
                    Assert.assertEquals(
                            "Failed executing deploy-app command",
                            Response.Status.CREATED.getStatusCode(),
                            r.getStatus());

                    final UUID appInstanceId = r.readEntity(UUID.class);
                    Assert.assertNotNull(appInstanceId);
                    contextToReturn.put(appInstanceIdKeyOut, appInstanceId);
                });

        String state = waitForAppToDeploy((UUID) contextToReturn.get(appInstanceIdKeyOut));
        Assert.assertEquals("RUNNING", state);
        return contextToReturn;
    }

}
