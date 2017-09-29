// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yaariy1 on 9/25/2016.
 */
public class MappingScenario extends BaseScenario {
    private final Map<String, String> mappings;
    private final Map<String, Object> mappedContext = new HashMap<>();

    public MappingScenario(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
        mappings.forEach((key, value) -> {
            mappedContext.put(value, context.getLatest(key));
        });
    }

    @Override
    protected Map<String, Object> executeScenario() {
        return mappedContext;
    }
}
