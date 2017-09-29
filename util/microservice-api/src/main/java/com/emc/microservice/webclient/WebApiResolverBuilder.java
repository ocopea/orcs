// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.webclient;

/**
 * Created by liebea on 4/23/17.
 * Drink responsibly
 */
public class WebApiResolverBuilder {
    private String username = null;
    private String password = null;
    private boolean verifySsl = true;

    public WebApiResolverBuilder withBasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public WebApiResolverBuilder withVerifySsl(boolean verifySsl) {
        this.verifySsl = verifySsl;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isVerifySsl() {
        return verifySsl;
    }
}
