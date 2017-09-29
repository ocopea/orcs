// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.auth;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by Karun Chinthapatla on 5/19/17.
 */
@JsonPropertyOrder({"access_token","token_type","refresh_token","expires_in","scope","jti"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class OAuthAccessToken  implements Serializable {

    @JsonProperty("access_token")
    private String token = "";

    @JsonProperty("token_type")
    private String tokenType = "";

    @JsonProperty("refresh_token")
    private String refreshToken = "";

    @JsonProperty("expires_in")
    private String expiresIn = "";

    @JsonProperty("scope")
    private String scope = "";

    @JsonProperty("jti")
    private String jti = "";

    @JsonProperty("refresh_expires_in")
    private String refreshExpiresIn = "";

    public OAuthAccessToken() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String accessToken) {
        this.token = accessToken;
    }

    public String getType() {
        return tokenType;
    }

    public void setType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(String refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
