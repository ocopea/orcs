// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 8/11/16.
 * Drink responsibly
 */
public class ScenarioRunner {
    private final ScenariosExecution execution;
    private final ServiceWebTargetResolver urlResolver;

    public ScenarioRunner(ServiceWebTargetResolver urlResolver, ScenariosExecution execution) {
        this.urlResolver = urlResolver;
        this.execution = execution;
    }

    public class ScenarioExecutionContext {
        // list in reverse order of insertion
        private final Map<String, List<Object>> executionResults = new HashMap<>();

        /**
         * returns results from context by key, ordered from last inserted to first.
         */
        public <T> List<T> getResults(String key) {
            final List<Object> objects = executionResults.get(key);
            if (objects.isEmpty()) {
                Assert.fail("Scenario expected to have " + key + " in execution context");
            }
            //noinspection unchecked
            return (List<T>) objects;

        }

        public <T> T getLatest(String key) {
            final List<Object> objects = getResults(key);
            //noinspection unchecked
            return (T) objects.get(0);
        }

        void addResult(Map<String, Object> result) {
            result.forEach((s, o) -> {
                List<Object> objects = executionResults.get(s);
                if (objects == null) {
                    objects = new LinkedList<>();
                    executionResults.put(s, objects);
                }
                objects.add(0, o);
            });
        }
    }

    public static class ScenariosExecution {
        private final List<BaseScenario> scenarios;

        public ScenariosExecution(BaseScenario root) {
            this.scenarios = new ArrayList<>(Collections.singletonList(root));
        }

        public ScenariosExecution then(BaseScenario scenario) {
            scenarios.add(scenario);
            return this;
        }
    }

    @NoJavadoc
    public void run() {
        final ScenarioExecutionContext context = new ScenarioExecutionContext();
        execution.scenarios.forEach(scenario -> {
            scenario.init(urlResolver, context);
            final Map<String, Object> result = scenario.executeScenario();
            context.addResult(result);
        });
    }

}
