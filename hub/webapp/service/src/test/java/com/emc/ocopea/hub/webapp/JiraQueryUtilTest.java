// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.CookieParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.WebTarget;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by liebea on 3/26/17.
 * Drink responsibly
 */
public class JiraQueryUtilTest {

    @Test
    public void testJiraQuery() {
        JiraQueryUtil jiraQueryUtil = new JiraQueryUtil(new MyWebAPIResolver());

        final UIJiraLoginResponse jiraData = jiraQueryUtil.queryJira("http://jira.com", "shpandrak", "1234");
        Assert.assertEquals(1, jiraData.getIssueTypes().size());
        Assert.assertEquals("iId", jiraData.getIssueTypes().iterator().next().getId());
        Assert.assertEquals("iName", jiraData.getIssueTypes().iterator().next().getName());
        Assert.assertEquals(1, jiraData.getProjects().size());
        Assert.assertEquals("pId", jiraData.getProjects().iterator().next().getId());
        Assert.assertEquals("pName", jiraData.getProjects().iterator().next().getName());

    }

    @Test
    public void testInvalidSession() {
        JiraQueryUtil jiraQueryUtil = new JiraQueryUtil(new MyWebAPIResolver(false));

        try {
            jiraQueryUtil.queryJira("http://jira.com", "shpandrak", "1234");
            Assert.fail("Should have failed");

        } catch (InternalServerErrorException ex) {
            // Yey, this is expected
        }

    }

    private static class MyWebAPIResolver implements WebAPIResolver {
        private final boolean valid;

        MyWebAPIResolver() {
            this(true);
        }

        MyWebAPIResolver(boolean valid) {
            this.valid = valid;
        }

        @Override
        public WebAPIResolver buildResolver(WebApiResolverBuilder builder) {
            return this;
        }

        @Override
        public <T> T getWebAPI(String s, Class<T> aClass) {
            //noinspection unchecked
            return (T)new JiraQueryUtil.JiraAPI(){
                @Override
                public JiraQueryUtil.JiraSessionResponse autenticate(Map<String, String> credentials) {
                    final JiraQueryUtil.JiraSessionResponse response = new JiraQueryUtil.JiraSessionResponse();
                    final JiraQueryUtil.JiraSession session = new JiraQueryUtil.JiraSession();

                    if (valid) {
                        session.name = "JSESSIONID";
                    } else {
                        session.name = "bad";
                    }
                    session.value = "sessionValue";
                    response.session = session;
                    return response;
                }

                @Override
                public List<JiraQueryUtil.JiraNamedObject> listProjects(@CookieParam("JSESSIONID") String sessionKey) {
                    final JiraQueryUtil.JiraNamedObject o = new JiraQueryUtil.JiraNamedObject();
                    o.name = "pName";
                    o.id = "pId";
                    return Collections.singletonList(o);
                }

                @Override
                public List<JiraQueryUtil.JiraNamedObject> listIssueTypes(@CookieParam("JSESSIONID") String sessionKey) {
                    final JiraQueryUtil.JiraNamedObject o = new JiraQueryUtil.JiraNamedObject();
                    o.name = "iName";
                    o.id = "iId";
                    return Collections.singletonList(o);
                }
            };
        }

        @Override
        public WebTarget getWebTarget(String s) {
            return null;
        }
    }
}