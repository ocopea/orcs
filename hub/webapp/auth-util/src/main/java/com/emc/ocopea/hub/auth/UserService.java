// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.auth;

import com.emc.microservice.Context;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.microservice.webclient.WebAPIResolver;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Mock authenticating service, uses a list of username password pairs kept in memory and authenticates by string
 * comparison.
 */
public class UserService implements ServiceLifecycle {

    private static Map<UUID, AuthUser> users = new HashMap<>();

    public Collection<AuthUser> list() {
        return new ArrayList<>(users.values());
    }

    public AuthUser getById(UUID id) {
        return users.get(id);
    }

    private WebAPIResolver webAPIResolver;
    private String uaaEndPoint;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);


    /**
     * This method authenticates the user with the user list from the local store
     * @param userName - Username
     * @param password - password
     * @return AuthUser
     */
    public AuthUser authenticate(String userName, String password) {
        return users
                .values()
                .stream()
                .filter(authUser ->
                        authUser.getName().equals(userName) && authUser.getPassword().equals(password))
                .findFirst()
                .orElse(null);
    }

    public AuthUser createUser(String userName, String password, String firstName, String lastName, String email) {
        UUID uuid = UUID.nameUUIDFromBytes(userName.getBytes());
        AuthUser newUser = new AuthUser(uuid, userName, password, firstName, lastName, email);
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    /**
     * This method authenticate the user against the PCF UAA database and return the token for that user.
     * @param userName - Username of the PCF user
     * @param password - Password of the user
     * @return Token String
     */
    public String authenticateAndGetToken(String userName, String password) {
        // POST to /outh/token endpoint to authorize and get the token
        if (uaaEndPoint.isEmpty()) {
            throw new WebApplicationException("UAA endpoint not configured");
        }
        String uaaTokenTarget = "https://" + uaaEndPoint + "/oauth/token";
        WebTarget webTarget =  webAPIResolver.getWebTarget(uaaTokenTarget);
        return getToken(webTarget,userName,password);
    }

    /**
     * This method gets the token for the user by calling OAUTH/TOKEN endpoint of PCF UAA
     * @param target - OAUTH Target - https://uaa.xyz.com/oauth/token
     * @param username - Username
     * @param password - Password
     * @return Token string
     */
    public String getToken(WebTarget target, String username, String password) {
        Invocation.Builder builder = target.request();
        Form form = new Form();
        form.param("grant_type", "password");
        form.param("username", username);
        form.param("password", password);
        form.param("client_id", "naz1");
        form.param("client_secret", "cndpapp");
        form.param("token_format", "opaque");
        form.param("response_type", "token");
        Response response = builder.accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
        if (response.getStatusInfo() != Response.Status.OK) {
            log.info("Get Token response: " + response.getStatusInfo());
            return "";
        }
        String token = response.readEntity(OAuthAccessToken.class).getToken();
        return token;
    }

    /**
     * This method validates the token and constructs the Authtoken object from the token details obtained
     *  from the validation
     * @param token - Token for username and password
     * @return AuthUser object
     */
    public AuthUser validateToken(String token) {
        if (uaaEndPoint.isEmpty()) {
            throw new WebApplicationException(" UAA endpoint not configured");
        }
        String uaaTokenTarget = "https://" + uaaEndPoint + "/check_token";
        WebTarget webTarget =  webAPIResolver.getWebTarget(uaaTokenTarget);
        // Authorization header
        String clientAuthorizationUAAResource = Base64.encode("naz1:cndpapp".getBytes());
        Invocation.Builder builder = webTarget.request();
        Form form = new Form();
        form.param("token", token);

        Response response = builder.accept(MediaType.APPLICATION_JSON_TYPE)
                .header("Authorization", "Basic " + clientAuthorizationUAAResource)
                .post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE));

        if (response.getStatusInfo() != Response.Status.OK) {
            log.info("Check TOKEN response: " + response.getStatusInfo());
            response.close();
            return null;
        }

        //Construct AuthUser object from TokenDetails - Need to be refactored
        TokenDetails tokenDetails = response.readEntity(TokenDetails.class);
        UUID userId = UUID.fromString(tokenDetails.getUserId());
        if (tokenDetails != null) {
            AuthUser authUser = new AuthUser(userId, tokenDetails.getName(), "",
                    tokenDetails.getName(), tokenDetails.getName(), tokenDetails.getEmail());
            return authUser;
        } else {
            log.info(" TokenDetails is NULL");
            return null;
        }
    }

    @Override
    public void init(Context context) {

        webAPIResolver = context.getWebAPIResolver();
        uaaEndPoint = System.getenv("UAA_HOST");
    }

    @Override
    public void shutDown() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}
}
