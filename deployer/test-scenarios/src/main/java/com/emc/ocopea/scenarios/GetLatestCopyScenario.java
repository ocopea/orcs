// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import org.junit.Assert;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by yaariy1 on 9/26/2016.
 */
public class GetLatestCopyScenario extends BaseScenario {

    private final String instanceIdKeyIn;
    private UUID appInstanceId;
    private final String latestCopyInstanceIdKeyOut;

    public GetLatestCopyScenario(String instanceIdKeyIn, String latestCopyInstanceIdKeyOut) {
        this.instanceIdKeyIn = instanceIdKeyIn;
        this.latestCopyInstanceIdKeyOut = latestCopyInstanceIdKeyOut;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        this.appInstanceId = getFromContext(context, instanceIdKeyIn, UUID.class);
    }

    @Override
    protected Map<String, Object> executeScenario() {
        Map<String, Object> contextToReturn = new HashMap<>();

        doGet("hub-web", "app-instance/" + appInstanceId.toString() + "/copy-history", Map.class, (r, value) -> {

            Assert.assertEquals("Failed copy-history query", Response.Status.OK.getStatusCode(), r.getStatus());

            Assert.assertTrue(value.containsKey("copies"));
            final List<Map> copies = (List<Map>) value.get("copies");
            final Map lastCopy = copies.stream().max((o1, o2) -> {
                Long timeStamp1 = (Long) o1.get("timeStamp");
                Long timeStamp2 = (Long) o2.get("timeStamp");
                if (timeStamp1 > timeStamp2) {
                    return 1;
                }
                if (timeStamp2 > timeStamp1) {
                    return -1;
                }
                return 0;
            }).get();

            final UUID latestCopyId = UUID.fromString((String) lastCopy.get("copyId"));
            contextToReturn.put(latestCopyInstanceIdKeyOut, latestCopyId);
        });
        return contextToReturn;
    }

}
