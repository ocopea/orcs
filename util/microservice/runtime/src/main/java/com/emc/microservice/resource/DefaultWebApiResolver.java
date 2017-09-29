// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.resource;

import com.emc.microservice.webclient.WebAPIResolver;
import com.emc.microservice.webclient.WebApiResolverBuilder;
import com.emc.ocopea.util.rest.RestClientUtil;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.client.WebTarget;

/**
 * Created by liebea on 1/29/17.
 * Drink responsibly
 */
public class DefaultWebApiResolver implements WebAPIResolver {

    private final BasicAuthentication authentication;
    private final boolean sslVerify;

    public DefaultWebApiResolver() {
        this(true);
    }

    public DefaultWebApiResolver(boolean sslVerify) {
        authentication = null;
        this.sslVerify = sslVerify;
    }

    public DefaultWebApiResolver(BasicAuthentication authentication) {
        this(authentication, true);
    }

    public DefaultWebApiResolver(BasicAuthentication authentication, boolean sslVerify) {
        this.authentication = authentication;
        this.sslVerify = sslVerify;
    }

    @Override
    public WebAPIResolver buildResolver(WebApiResolverBuilder builder) {
        BasicAuthentication basicAuthentication = null;
        if (builder.getUsername() != null) {
            basicAuthentication = new BasicAuthentication(builder.getUsername(), builder.getPassword());
        }
        return new DefaultWebApiResolver(basicAuthentication, builder.isVerifySsl());
    }

    @Override
    public <T> T getWebAPI(String url, Class<T> resourceWebAPI) {
        ResteasyWebTarget target = getResteasyWebTarget(url);
        return target.proxy(resourceWebAPI);
    }

    protected ResteasyWebTarget getResteasyWebTarget(String url) {
        return RestClientUtil.getWebTarget(url, sslVerify, authentication);
    }

    @Override
    public WebTarget getWebTarget(String url) {
        return getResteasyWebTarget(url);
    }
}
