// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.Context;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.hub.auth.AuthFilter;
import com.emc.ocopea.hub.auth.AuthUser;
import com.emc.ocopea.hub.auth.UserService;
import com.emc.ocopea.util.io.StreamUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PathParam;
import javax.ws.rs.RedirectionException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by liebea on 5/10/16.
 * Drink responsibly
 */
public class UserHubWebAppResource implements HubWebAppUserWebApi {

    private BlobStoreAPI imageStore;
    private UserService userService;

    @javax.ws.rs.core.Context
    private SecurityContext securityContext;

    @javax.ws.rs.core.Context
    private HttpServletRequest request;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        Context context = ((MicroServiceApplication) application).getMicroServiceContext();

        this.imageStore = context.getBlobStoreManager().getManagedResourceByName("image-store").getBlobStoreAPI();
        this.userService = context.getSingletonManager()
                .getManagedResourceByName(UserService.class.getSimpleName()).getInstance();
    }

    @Override
    public UIUser getLoggedInUser() {
        final AuthUser user = (AuthUser) securityContext.getUserPrincipal();
        if (user == null) {
            throw new NotFoundException("No user logged in to the system");
        }
        return convert(user);
    }

    @Override
    public List<UIUser> listUsers() {
        return userService.list().stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public UIUser getUser(@PathParam("userId") String userId) {
        final AuthUser user = userService.getById(UUID.fromString(userId));
        if (user == null) {
            throw new NotFoundException("User with id " + userId + " Not found");
        }
        return convert(user);
    }

    private UIUser convert(AuthUser user) {
        return new UIUser(
                user.getId().toString(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }

    @Override
    public Response getUserImage(@PathParam("userId") String userId) {
        StreamingOutput so = output -> {
            try {
                imageStore.readBlob("user-images", userId, output);
            } catch (Exception ex) {
                final InputStream resourceAsStream =
                        this.getClass().getClassLoader().getResourceAsStream("static/images/no-user.png");
                if (resourceAsStream == null) {
                    throw new NotFoundException(ex);
                }
                StreamUtil.copy(resourceAsStream, output);
            }
        };
        return Response.ok(so).build();
    }

    @Override
    public void signIn2(UISignInInfo signInInfo) {
        final AuthUser authUser = userService.authenticate(signInInfo.getUsername(), signInInfo.getPassword());
        // Take a the UAA path
        if (authUser == null) {
            final String token =
                    userService.authenticateAndGetToken(signInInfo.getUsername(), signInInfo.getPassword());
            if (token.isEmpty()) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
        }
        throw new RedirectionException(Response.Status.SEE_OTHER, URI.create("html/nui/index.html"));
    }

    @Override
    public void signOut() {
        request.getSession().setAttribute(AuthFilter.SECURITY_CONTEXT_SESSION_KEY, null);
        throw new RedirectionException(Response.Status.SEE_OTHER, URI.create("html/nui/index.html"));
    }

    @Override
    public String signIn(UISignInInfo signInInfo) {
        final String token  = userService.authenticateAndGetToken(signInInfo.getUsername(), signInInfo.getPassword());
        if (token.isEmpty()) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        return token;
    }
}
