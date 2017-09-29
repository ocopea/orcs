// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub;

import com.emc.ocopea.hub.auth.AuthUser;

import javax.ws.rs.core.SecurityContext;

/**
 * Created by liebea on 10/6/16.
 * Drink responsibly
 */
public class UserContextService {
    private final SecurityContext securityContext;

    public UserContextService(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public AuthUser getUser() {
        return (AuthUser)securityContext.getUserPrincipal();
    }
}
