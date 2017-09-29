// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 6/20/16.
 * Drink responsibly
 */
public class NavigateToDashboardScenario extends BaseScenario {

    private final String instanceIdKeyIn;
    private UUID appInstanceId;

    public NavigateToDashboardScenario(String instanceIdKeyIn) {
        this.instanceIdKeyIn = instanceIdKeyIn;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        this.appInstanceId = getFromContext(context, instanceIdKeyIn, UUID.class);
    }

    @Override
    protected Map<String, Object> executeScenario() {
        // todo:asserts..
        doGet(
                "hub-web",
                "app-instance/" + appInstanceId.toString() + "/dashboard-stats",
                String.class,
                (r, value) -> System.out.println(value));

        // todo:asserts..
        doGet(
                "hub-web",
                "app-instance/" + appInstanceId.toString() + "/copy-history",
                String.class,
                (r, value) -> System.out.println(value));
        return Collections.emptyMap();
    }
}
