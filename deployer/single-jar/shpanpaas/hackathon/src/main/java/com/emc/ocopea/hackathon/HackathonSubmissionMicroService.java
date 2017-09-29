// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.bootstrap.SchemaBootstrapRunner;
import com.emc.microservice.datasource.ManagedDatasource;
import com.emc.ocopea.hackathon.html.SubmissionHtmlResource;
import com.emc.ocopea.hackathon.idea.SubmitIdeaResource;
import com.emc.ocopea.util.database.BasicNativeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by liebea on 5/21/15.
 * Drink responsibly
 */
public class HackathonSubmissionMicroService extends MicroService {
    private static final String SERVICE_NAME = "Hackathon Submission Service";
    private static final String SERVICE_BASE_URI = "hackathon";
    private static final String SERVICE_DESCRIPTION = "Submit all sort of crazy ideas";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(HackathonSubmissionMicroService.class);

    public HackathonSubmissionMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withDatasource("hackathon-db", "Hackathon Database")

                        .withBlobStore("hack-docs")

                        .withRestResource(SubmitIdeaResource.class, "Submit hackathon ideas")

                        .withRestResource(SubmissionHtmlResource.class, "Submission HTML app")

                        .withParameter("hackathon-pro-mode", "Pro mode", false, false)

        );
    }

    @Override
    protected void initializeService(Context context) {
        super.initializeService(context);

        logger.info("Pro mode: " + context.getParametersBag().getBoolean("hackathon-pro-mode"));

        ManagedDatasource managedResourceByName =
                context.getDatasourceManager().getManagedResourceByName("hackathon-db");

        try {
            final boolean[] mySqlExists = new boolean[1];
            new BasicNativeQueryService(managedResourceByName.getDataSource())
                    .readDatabaseMetadata(databaseMetaData -> {
                        try {
                            mySqlExists[0] = databaseMetaData.getDatabaseProductName().equalsIgnoreCase("MySQL");
                        } catch (SQLException e) {
                            throw new IllegalStateException("failed", e);
                        }
                    });

            if (!mySqlExists[0]) {
                SchemaBootstrapRunner.runBootstrap(
                        managedResourceByName.getDataSource(),
                        new HackathonSubmissionSchemaBootstrap(),
                        managedResourceByName.getConfiguration().getDatabaseSchema(),
                        null);
            }
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("Failed initializing service schema", e);
        }
    }
}
