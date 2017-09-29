// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.webapp;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.microservice.dependency.SyncCallServiceDependencyDescriptor;
import com.emc.ocopea.hub.auth.AuthFilter;
import com.emc.ocopea.hub.auth.AuthUser;
import com.emc.ocopea.hub.auth.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by liebea on 7/28/15.
 * Drink responsibly
 */
public class HubWebAppMicroService extends MicroService {
    private static final Logger logger = LoggerFactory.getLogger(HubWebAppMicroService.class);
    private static final String SERVICE_NAME = "Hub WebApp";
    private static final int SERVICE_VERSION = 1;
    private static final String SERVICE_IDENTIFIER = "hub-web";
    private static final String SERVICE_DESCRIPTION = "Hub WebApp";

    public HubWebAppMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withRestResource(
                                HubWebAppHtmlResource.class,
                                "HTML/Web Resources")
                        .withRestResource(
                                HubWebAppImageStoreResource.class,
                                "HTML/Web Images")
                        .withRestResource(
                                UserHubWebAppResource.class,
                                "User Resources")

                        .withRestResource(
                                HubWebAppResource.class,
                                "WebApp services")

                        .withRestResource(
                                TestDevHubWebAppResource.class,
                                "Test Dev Rest Resource")

                        .withServiceDependency(new SyncCallServiceDependencyDescriptor("hub", true))

                        .withBlobStore("image-store")

                        .withSingleton(HubLoggingWebSocketsManager.class)

                        .withWebSocket(HubLogsWebSocket.class)

                        .withSingleton(AppTemplateCache.class)

                        .withSingleton(UserService.class)

                        .withRestProvider(AuthFilter.class, "Authentication filter")

                        .withRestProvider(StaticFilesCacheFilter.class, "Static files' cache filter")

                        .withJacksonSerialization(UICommandAddJiraIntegration.class)

                        .withJacksonSerialization(UICommandAddPivotalTrackerIntegration.class)
        );
    }

    @Override
    protected void initializeService(Context context) {
        final BlobStoreAPI is = context.getBlobStoreManager().getManagedResourceByName("image-store").getBlobStoreAPI();

        createProtocolIcon(is, "mongodb", "mongodb-logo.png");
        createProtocolIcon(is, "mysql", "mysql-logo.png");
        createProtocolIcon(is, "s3", "s3-logo.png");
        createProtocolIcon(is, "rabbitmq", "rabbitmq-logo.png");
        createProtocolIcon(is, "docker-volume", "docker-volume-logo.png");
        createProtocolIcon(is, "postgres", "postgres-logo.png");

        createUser(context, "shpandrak", "1234", "Amit", "Lieberman", "amit.lieberman@emc.com", "minime.png", is);
        createUser(context, "bethp", "1234", "Elizabeth", "Phalen", "elizabeth.phalen@emc.com", "beth.jpeg", is);
        createUser(context, "stanly", "1234", "Stephen", "Manley", "stephen.manley@dell.com", "stanly.jpg", is);
        createUser(context, "jimc", "1234", "Jim", "Clancy", "jim.clancy@emc.com", "jimclancy.jpeg", is);
        createUser(context, "galb", "1234", "Gal", "Ben Haim", "galb.netcraft@co.il", "galb.png", is);
        createUser(context, "frumi", "1234", "Frumi", "", "roni.frumkes@emc.com", "frumi.png", is);
        createUser(context, "yariv", "1234", "Yariv", "Yaari", "yariv.yaari@emc.com", "yariv.jpeg", is);
        createUser(context, "ford", "1234", "Henry", "Ford", "henry@ford.com", "ford.jpg", is);
        createUser(context, "maloni", "1234", "Matan", "Rubin", "matan.rubin@dell.com", "matan.jpg", is);
        createUser(context, "shilpa", "1234", "Shilpa", "Parulekar", "shilpa.parulekar@dell.com", "shilpi.jpg", is);
        createUser(context, "cindy", "1234", "Cynthia", "Burns", "cynthia.burns@emc.com", "burns.jpg", is);
        createUser(context, "admin", "1234", "Ocopea", "Admin", "admin@ocopea.com", "ocopea-admin.png", is);
        createUser(context, "mike", "1234", "Michael", "Dell", "michael@dell.com", "michael-dell.jpg", is);

        createImageTypeIcon(is, "java", "java-logo.png");
        createImageTypeIcon(is, "nodejs", "nodejs-logo.png");
        createImageTypeIcon(is, "apache-webserver", "apache-http-server-logo.png");
    }

    private void createProtocolIcon(BlobStoreAPI imageStore, String protocolName, String imageFileName) {
        createBlobIfNotExist(imageStore, "dsb-protocol", protocolName, imageFileName);
    }

    private void createImageTypeIcon(BlobStoreAPI imageStore, String imageType, String imageFileName) {
        createBlobIfNotExist(imageStore, "image-type", imageType, imageFileName);
    }

    private void createUser(
            Context context,
            String userName,
            String password,
            String firstName,
            String lastName,
            String email,
            String imageFileName,
            BlobStoreAPI imageStore) {
        final UserService userService = context.getSingletonManager()
                .getManagedResourceByName(UserService.class.getSimpleName()).getInstance();
        final AuthUser user = userService.createUser(userName, password, firstName, lastName, email);
        createBlobIfNotExist(imageStore, "user-images", user.getId().toString(), imageFileName);
    }

    private void createBlobIfNotExist(BlobStoreAPI imageStore, String namespace, String key, String imageFileName) {
        if (!imageStore.isExists(namespace, key)) {
            imageStore.create(namespace, key, Collections.emptyMap(), readResourceAsStream(imageFileName));
        }
    }

    private InputStream readResourceAsStream(String resourceFileName) {
        return Objects.requireNonNull(
                this.getClass().getClassLoader().getResourceAsStream(resourceFileName),
                "failed loading static resource " + resourceFileName);
    }
}
