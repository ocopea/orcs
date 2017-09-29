// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.ocopea.hub.HubWebAppUtil;
import com.emc.ocopea.hub.application.HubWebApi;
import com.emc.ocopea.util.JsonUtil;

import javax.ws.rs.InternalServerErrorException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A utility class for managing nazgul application integrations.
 */
public class IntegrationsUtil {

    private static final String HUB_CONFIG_JIRA_INTEGRATION = "jira-integration";
    private static final String HUB_CONFIG_PIVOTAL_TRACKER_INTEGRATION = "pivotal-tracker-integration";

    private IntegrationsUtil() {}

    /**
     * Returns a list of integrations, or an empty list if no integrations exist
     */
    public static List<UIIntegrationDetails> listIntegrations(Supplier<HubWebApi> hubWebApiSupplier) {
        List<UIIntegrationDetails> integrations = new ArrayList<>();

        // Getting jira integration details if available
        final Map<String, String> jiraConnectivityDetails =
                readHubConfigAsMap(hubWebApiSupplier, HUB_CONFIG_JIRA_INTEGRATION);
        if (jiraConnectivityDetails != null) {
            integrations.add(new UIIntegrationDetails("jira", null, jiraConnectivityDetails));
        }

        // Getting pivotal tracker integration details if available
        final Map<String, String> pivotalTrackerConnectivityDetails =
                readHubConfigAsMap(hubWebApiSupplier,HUB_CONFIG_PIVOTAL_TRACKER_INTEGRATION);
        if (pivotalTrackerConnectivityDetails != null) {
            integrations.add(new UIIntegrationDetails("pivotal-tracker", null, pivotalTrackerConnectivityDetails));
        }

        return integrations;
    }

    public static void addPivotalTrackerIntegration(
            Supplier<HubWebApi> hubWebApiSupplier,
            UICommandAddPivotalTrackerIntegration addPivotalTrackerIntegration) {
        writeHubConfig(hubWebApiSupplier, addPivotalTrackerIntegration, HUB_CONFIG_PIVOTAL_TRACKER_INTEGRATION);
    }

    public static void addJiraIntegration(
            Supplier<HubWebApi> hubWebApiSupplier,
            UICommandAddJiraIntegration addJiraIntegration) {
        writeHubConfig(hubWebApiSupplier, addJiraIntegration, HUB_CONFIG_JIRA_INTEGRATION);
    }

    private static <T> void writeHubConfig(Supplier<HubWebApi> hubWebApiSupplier, T object, String key) {
        HubWebAppUtil.wrap("writing hub config with key " + key, () -> {
            try {
                hubWebApiSupplier.get().writeHubConfig(key, JsonUtil.toJson(object));
            } catch (Exception e) {
                throw new InternalServerErrorException("Failed writing hub config with key " + key + e.getMessage(), e);
            }
        });
    }

    /***
     * Reading hub config using a key.
     * @return config parsed as strings map or null if config was not initialized
     */
    private static Map<String, String> readHubConfigAsMap(Supplier<HubWebApi> hubWebApiSupplier, String key) {
        final String configResponse = hubWebApiSupplier.get().readHubConfig(key);
        if (configResponse != null && !configResponse.isEmpty()) {
            return JsonUtil.readMap(configResponse);
        } else {
            return null;
        }
    }
}
