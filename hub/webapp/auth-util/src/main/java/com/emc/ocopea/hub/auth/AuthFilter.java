// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.auth;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;
import java.net.URI;

/**
 * Basic HTTP Auth filter.
 */
@Provider
@PreMatching
public class AuthFilter implements ContainerRequestFilter {

    public static final String SECURITY_CONTEXT_SESSION_KEY = AuthFilter.class.getName() + ".security-context";
    private UserService userService;
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    
    @javax.ws.rs.core.Context
    private HttpServletRequest request;

    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();
        this.userService = context.getSingletonManager()
                .getManagedResourceByName(UserService.class.getSimpleName()).getInstance();

    }

    /**
     * Apply the filter : check input request, validate (or not) with user auth. First, the user is authenticated
     *  against the local database, if the user is not found in the local database, validation is performed against PCF
     *  UAA users.
     *
     * @param containerRequest The request from Tomcat server
     */
    @Override
    public void filter(ContainerRequestContext containerRequest) {
        final HttpSession session = request.getSession();
        HubWebAppSecurityContext hubWebAppSecurityContext =
                (HubWebAppSecurityContext) session.getAttribute(SECURITY_CONTEXT_SESSION_KEY);

        if (hubWebAppSecurityContext == null &&
                !containerRequest.getUriInfo().getPath(true)
                        .startsWith("/html/login/")) {
            //lap : loginAndPassword
            String authorizationHeader =
                    containerRequest.getHeaderString(HttpHeaders.AUTHORIZATION);

            //Authorization header is NULL when sign-out API is called - handle it now and investigate the UI code later
            if (authorizationHeader == null || authorizationHeader.startsWith("Basic ")) {
                String[] lap = getLap(containerRequest);
                if (lap != null && lap.length == 2) {
                    // Support basic Authentication with local database
                    AuthUser authenticationResult = userService.authenticate(lap[0], lap[1]);
                    if (authenticationResult != null) {
                        String scheme = containerRequest.getUriInfo().getRequestUri().getScheme();
                        hubWebAppSecurityContext = new HubWebAppSecurityContext(authenticationResult, scheme);
                        session.setAttribute(SECURITY_CONTEXT_SESSION_KEY, hubWebAppSecurityContext);
                    } else {
                        // Try the UAA path
                        String token = userService.authenticateAndGetToken(lap[0], lap[1]);
                        if (!token.isEmpty()) {
                            log.info("Acquired token for username and password");
                            AuthUser authUser = userService.validateToken(token);
                            if (authUser == null) {
                                throw new NotAuthorizedException("Not an authorized user");
                            }
                            String scheme = containerRequest.getUriInfo().getRequestUri().getScheme();
                            hubWebAppSecurityContext = new HubWebAppSecurityContext(authUser, scheme);
                            session.setAttribute(SECURITY_CONTEXT_SESSION_KEY, hubWebAppSecurityContext);
                        } else {
                            log.info("Failed to get the token");
                            throw new NotAuthorizedException("Not an authorized user");
                        }
                    }
                }
            } else if (authorizationHeader.startsWith("Bearer ")) {
                // Extract the token from the HTTP Authorization header
                String token = authorizationHeader.substring("Bearer".length()).trim();
                //Check if the token is present
                AuthUser authUser = userService.validateToken(token);
                if (authUser == null) {
                    // Make the context Null
                    session.setAttribute(SECURITY_CONTEXT_SESSION_KEY, null);
                    throw new NotAuthorizedException("Not a valid token");
                }
            }
            // login failed at some point
            if (hubWebAppSecurityContext == null) {
                throw new RedirectionException(Status.TEMPORARY_REDIRECT, URI.create("html/login/index.html"));
            }
        }
        containerRequest.setSecurityContext(hubWebAppSecurityContext);
    }

    protected String[] getLap(ContainerRequestContext containerRequest) {
        //Get the authentication passed in HTTP headers parameters
        String auth = containerRequest.getHeaderString("authorization");
        //If the user does not have the right (does not provide any HTTP Basic Auth)
        if (auth != null) {
            return BasicAuth.decode(auth);
        } else {
            return new String[0];
        }
    }
}