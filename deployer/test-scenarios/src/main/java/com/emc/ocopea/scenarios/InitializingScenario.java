// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import java.util.Map;

/**
 * Created by yaariy1 on 9/25/2016.
 */
public class InitializingScenario extends BaseScenario {
    private final Map<String, Object> initializedParams;

    public InitializingScenario(Map<String, Object> initializedParams) {
        this.initializedParams = initializedParams;
    }

    @Override
    protected Map<String, Object> executeScenario() {
        return initializedParams;
    }
}
