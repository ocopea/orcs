// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.dependency.SendAndForgetServiceDependencyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 */
public class BankUIServerMicroService extends MicroService {
    private static final String SERVICE_NAME = "Bank UI";
    private static final String SERVICE_IDENTIFIER = "bank-ui";
    private static final String SERVICE_DESCRIPTION = "Bank UI Server";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(BankUIServerMicroService.class);
    public static final String BANK_DB_NAME = "BankDB";

    public BankUIServerMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        // Using the bank datasource
                        .withDatasource(BANK_DB_NAME, "Bank Database")

                        // Web resource
                        .withRestResource(BankAccountResource.class, "Bank Account Management Resource")
                        .withRestResource(DepositToAccountResource.class, "Deposit to account Resource")

                        // This service is dependant on calculator, doing async callbacks with calculator
                        .withServiceDependency(new SendAndForgetServiceDependencyDescriptor(
                                "deposit-to-account",
                                true,
                                DepositToAccountRequest.class)
                        )

                        .withDefaultBlobStore()
        );
    }
}
