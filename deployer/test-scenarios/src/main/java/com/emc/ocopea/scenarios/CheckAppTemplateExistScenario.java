// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.scenarios;

import com.emc.ocopea.scenarios.ScenarioRunner;
import com.emc.ocopea.scenarios.ServiceWebTargetResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;

/**
 * Created by liebea on 6/20/16.
 * Drink responsibly
 */
public class CheckAppTemplateExistScenario extends BaseScenario {

    private final String appTemplateName;
    private String appTemplateIdKeyOut;

    public CheckAppTemplateExistScenario(String appTemplateName, String appTemplateIdKeyOut) {
        this.appTemplateName = appTemplateName;
        this.appTemplateIdKeyOut = appTemplateIdKeyOut;
    }

    @Override
    void init(ServiceWebTargetResolver webTargetResolver, ScenarioRunner.ScenarioExecutionContext context) {
        super.init(webTargetResolver, context);
    }

    @Override
    protected Map<String, Object> executeScenario() {
        final String id = getAppTemplateIdFromName(appTemplateName);
        doGet("hub-web", "app-template/" + id, String.class, (r, value) -> {
            final Map<String, String> tokenValues = new HashMap<>();
            tokenValues.put("appTemplateId", id);

            assertJsonEquals(
                    readResourceAsString("ngin-template-exists/hack-template.json", tokenValues),
                    value);
        });
        return Collections.singletonMap(appTemplateIdKeyOut,  UUID.fromString(id));
    }
}
