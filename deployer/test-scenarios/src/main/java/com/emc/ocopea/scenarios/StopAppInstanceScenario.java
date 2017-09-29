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
public class StopAppInstanceScenario extends BaseScenario {
    private final String appInstanceIdKeyIn;
    private UUID appInstanceId;

    public StopAppInstanceScenario(String appInstanceIdKeyIn) {
        this.appInstanceIdKeyIn = appInstanceIdKeyIn;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        this.appInstanceId = getFromContext(context, appInstanceIdKeyIn, UUID.class);
    }

    @Override
    protected Map<String, Object> executeScenario() {

        // Deploy the hackathon app template
        final Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("appInstanceId", appInstanceId.toString());

        final Map<String, Object> contextToReturn = new HashMap<>();
        postJson(
                "hub-web",
                "commands/stop-app",
                readResourceAsString("stop-app-command-args.json", tokenValues),
                (r) -> Assert.assertEquals(
                        "Failed executing sto-app command",
                        Response.Status.NO_CONTENT.getStatusCode(),
                        r.getStatus()));

        waitForAppToStop(this.appInstanceId);

        return contextToReturn;
    }
}
