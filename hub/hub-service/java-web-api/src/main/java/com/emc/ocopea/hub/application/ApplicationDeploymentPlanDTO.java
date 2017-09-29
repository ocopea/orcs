// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import java.util.Map;

/**
 * Created by liebea on 7/20/16.
 * Drink responsibly
 */
public class ApplicationDeploymentPlanDTO {
    private final String appTemplateId;
    private final String appInstanceId;
    private final Map<String, SiteDeploymentPlanDTO> sites;

    private ApplicationDeploymentPlanDTO() {
        this(null, null, null);
    }

    public ApplicationDeploymentPlanDTO(
            String appTemplateId,
            String appInstanceId,
            Map<String, SiteDeploymentPlanDTO> sites) {
        this.appTemplateId = appTemplateId;
        this.appInstanceId = appInstanceId;
        this.sites = sites;
    }

    public String getAppTemplateId() {
        return appTemplateId;
    }

    public String getAppInstanceId() {
        return appInstanceId;
    }

    public Map<String, SiteDeploymentPlanDTO> getSites() {
        return sites;
    }

    private final class SiteDeploymentPlanDTO {
        private final String todo;

        private SiteDeploymentPlanDTO() {
            this(null);
        }

        private SiteDeploymentPlanDTO(String todo) {
            this.todo = todo;
        }

        public String getTodo() {
            return todo;
        }
    }
}
