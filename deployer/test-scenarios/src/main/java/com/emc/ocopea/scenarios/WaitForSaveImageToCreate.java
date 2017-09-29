// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WaitForSaveImageToCreate extends BaseScenario {

    private final String savedImageIdKeyIn;
    private UUID savedImageId;
    private final long timeOutInSeconds;

    public WaitForSaveImageToCreate(String savedImageIdKeyIn, long timeOutInSeconds) {
        this.savedImageIdKeyIn = savedImageIdKeyIn;
        this.timeOutInSeconds = timeOutInSeconds;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        this.savedImageId = getFromContext(context, savedImageIdKeyIn, UUID.class);
    }

    @Override
    protected Map<String, Object> executeScenario() {
        Map<String, Object> contextToReturn = new HashMap<>();

        long started = System.currentTimeMillis();

        final boolean []created = {false};
        while (!created[0]) {
            doGet("hub-web", "test-dev/saved-app-images/" + savedImageId.toString(), Map.class, (r, value) -> {

                Assert.assertEquals("Failed getting copy metadata", Response.Status.OK.getStatusCode(), r.getStatus());
                created[0] = value.get("state").equals("created");
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
