// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.auth;

import java.io.Serializable;
import java.security.Principal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by Karun Chinthapatla on 6/1/17.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenDetails implements Principal, Serializable {

    @JsonProperty("user_id")
    private String userId = "";

    @JsonProperty("user_name")
    private String userName = "";

    @JsonProperty("email")
    private String email = "";

    @JsonProperty("client_id")
    private String clientId = "";

    @JsonProperty("grant_type")
    private String grantType = "";

    public TokenDetails() {
    }

    public String getUserId() {
        return userId;
    }

    public void setId(String id) {
        this.userId = id;
    }

    @Override
    public String getName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }
}