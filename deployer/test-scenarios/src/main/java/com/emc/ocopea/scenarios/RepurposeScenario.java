// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yaariy1 on 9/26/2016.
 */
public class RepurposeScenario extends BaseScenario {

    private final String originAppInstanceIdKeyIn;
    private final String copyInstanceIdKeyIn;
    private final String appInstanceName;
    private final String purpose;
    private String appInstanceIdKeyOut;
    private UUID originAppInstanceId;
    private UUID copyId;

    public RepurposeScenario(
            String appInstanceName,
            String purpose,
            String originAppInstanceIdKeyIn,
            String copyInstanceIdKeyIn,
            String appInstanceIdKeyOut) {
        this.originAppInstanceIdKeyIn = originAppInstanceIdKeyIn;
        this.copyInstanceIdKeyIn = copyInstanceIdKeyIn;
        this.appInstanceName = appInstanceName;
        this.purpose = purpose;
        this.appInstanceIdKeyOut = appInstanceIdKeyOut;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        this.originAppInstanceId = getFromContext(context, originAppInstanceIdKeyIn, UUID.class);
        this.copyId = getFromContext(context, copyInstanceIdKeyIn, UUID.class);
    }

    @Override
    protected Map<String, Object> executeScenario() {
        final Map<String, Object> contextToReturn = new HashMap<>();

        final Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put("originAppInstanceId", originAppInstanceId.toString());
        tokenValues.put("copyId", copyId.toString());
        tokenValues.put("appInstanceName", appInstanceName);
        tokenValues.put("purpose", purpose);

        // todo:Ugliest hack ever... do more functional? promise? future? huh?
        postJson(
                "hub-web",
                "commands/repurpose-app",
                readResourceAsString("deploy-hackathon/repurpose-command-args.json", tokenValues),
                (r) -> {
                    // Testing that the command succeeded
                    Assert.assertEquals(
                            "Failed executing repurpose command",
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
