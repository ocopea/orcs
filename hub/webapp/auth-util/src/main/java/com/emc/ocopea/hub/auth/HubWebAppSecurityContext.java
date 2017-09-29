// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.auth;

import javax.ws.rs.core.SecurityContext;
import java.io.Serializable;

/**
 * Custom Security Context.
 *
 * @author Deisss (MIT License)
 */
public class HubWebAppSecurityContext implements SecurityContext, Serializable {
    private AuthUser user;
    private String scheme;

    public HubWebAppSecurityContext(AuthUser user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    @Override
    public AuthUser getUserPrincipal() {
        return this.user;
    }

    @Override
    public boolean isUserInRole(String s) {
        return user.getRoles().contains(s);
    }

    @Override
    public boolean isSecure() {
        return "https".equals(this.scheme);
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}