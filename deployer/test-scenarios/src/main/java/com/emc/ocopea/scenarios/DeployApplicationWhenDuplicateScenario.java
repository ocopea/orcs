// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 6/20/16.
 * Drink responsibly
 */
public class DeployApplicationWhenDuplicateScenario extends BaseScenario {

    private final String templateIdKeyIn;
    private UUID appTemplateId;
    private final String appInstanceName;
    private UUID siteId;
    private final String siteIdKeyIn;

    public DeployApplicationWhenDuplicateScenario(String appInstanceName, String templateIdKeyIn, String siteIdKeyIn) {
        this.templateIdKeyIn = templateIdKeyIn;
        this.appInstanceName = appInstanceName;
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
        final Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("appTemplateId", appTemplateId.toString());
        tokenValues.put("appInstanceName", appInstanceName);
        tokenValues.put("siteId", siteId.toString());
        populateServiceConfigurationParams(tokenValues, siteId, appTemplateId);

        postJson(
                "hub-web",
                "commands/deploy-app",
                readResourceAsString("deploy-hackathon/deploy-command-args.json", tokenValues),
                (r) -> Assert.assertEquals(
                        "Failed executing deploy-app command",
                        Response.Status.CONFLICT.getStatusCode(),
                        r.getStatus()));
        return Collections.emptyMap();
    }
}
