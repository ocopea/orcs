// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaitForCopyToBeInCreatedState extends BaseScenario {

    private final String appInstanceIdKeyIn;
    private final String appCopyIdInKey;
    private UUID appCopyId;
    private UUID appInstanceId;
    private final long timeOutInSeconds;

    public WaitForCopyToBeInCreatedState(String appInstanceIdKeyIn, String appCopyIdInKey, long timeOutInSeconds) {
        this.appInstanceIdKeyIn = appInstanceIdKeyIn;
        this.appCopyIdInKey = appCopyIdInKey;
        this.timeOutInSeconds = timeOutInSeconds;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        this.appCopyId = getFromContext(context, appCopyIdInKey, UUID.class);
        this.appInstanceId = getFromContext(context, appInstanceIdKeyIn, UUID.class);
    }

    @Override
    protected Map<String, Object> executeScenario() {
        Map<String, Object> contextToReturn = new HashMap<>();

        long started = System.currentTimeMillis();

        final boolean []created = {false};
        while (!created[0]) {
            doGet(
                    "hub-web",
                    "app-instance/" + appInstanceId.toString() + "/app-copy/" + appCopyId.toString(),
                    Map.class,
                    (r, value) -> {
                        Assert.assertEquals(
                                "Failed getting copy metadata",
                                Response.Status.OK.getStatusCode(),
                                r.getStatus());
                        created[0] = value.get("status").equals("created");
                    });
            if (!created[0]) {
                if (started + (timeOutInSeconds * 1000) < System.currentTimeMillis()) {
                    Assert.fail("Took too long for copy to create, bye now");
                }
                sleepNoException(1000);
            }

        }
        return contextToReturn;
    }

}
