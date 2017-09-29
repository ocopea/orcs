// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.ocopea.util.MapBuilder;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by liebea on 3/26/17.
 * Drink responsibly
 */
public class JiraQueryUtil {
    private final WebAPIResolver webAPIResolver;

    public JiraQueryUtil(WebAPIResolver resolver) {
        webAPIResolver = resolver;
    }

    /**
     * Retrieve jira details
     */
    public UIJiraLoginResponse queryJira(String url, String userName, String password) {

        JiraAPI jiraTarget = webAPIResolver.getWebAPI(url, JiraAPI.class);
        JiraSession session = jiraTarget.autenticate(
                MapBuilder.<String, String>newHashMap()
                        .with("username", userName)
                        .with("password", password)
                        .build())
                .session;
        if (!"JSESSIONID".equals(session.name)) {
            throw new InternalServerErrorException("jira session not identified by JSESSIONID but by " + session.name);
        }
        return new UIJiraLoginResponse(
                jiraTarget.listProjects(session.value).stream()
                        .map(o -> new UIJiraLoginResponseProjects(o.id, o.name))
                        .collect(Collectors.toList()),
                jiraTarget.listIssueTypes(session.value).stream()
                        .map(o -> new UIJiraLoginResponseProjects(o.id, o.name))
                        .collect(Collectors.toList())
        );
    }

    interface JiraAPI {
        @POST
        @Path("/rest/auth/1/session")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        JiraSessionResponse autenticate(Map<String, String> credentials);

        @GET
        @Path("/rest/api/2/project")
        @Produces(MediaType.APPLICATION_JSON)
        List<JiraNamedObject> listProjects(@CookieParam("JSESSIONID") String sessionKey);

        @GET
        @Path("/rest/api/2/issuetype")
        @Produces(MediaType.APPLICATION_JSON)
        List<JiraNamedObject> listIssueTypes(@CookieParam("JSESSIONID") String sessionKey);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JiraNamedObject {
        @JsonProperty
        String id;
        @JsonProperty
        String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JiraSessionResponse {
        @JsonProperty
        JiraSession session;
    }

    static class JiraSession {
        @JsonProperty
        String name;
        @JsonProperty
        String value;
    }

}
