// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hackathon;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.ocopea.hackathon.committee.CommitteeResource;
import com.emc.ocopea.hackathon.html.CommitteeHtmlResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 2/27/17.
 * Drink responsibly
 */
public class HackathonCommitteeMicroService extends MicroService {
    private static final String SERVICE_NAME = "Hackathon Committee Service";
    private static final String SERVICE_BASE_URI = "committee";
    private static final String SERVICE_DESCRIPTION = "Approve Hackathon ideas";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(HackathonCommitteeMicroService.class);

    public HackathonCommitteeMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_BASE_URI,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        .withDatasource("hackathon-db", "Hackathon Database")

                        .withRestResource(CommitteeResource.class, "Committee resource")

                        .withRestResource(CommitteeHtmlResource.class, "Committee HTML app")
        );
    }
}


