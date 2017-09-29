// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import com.emc.microservice.dependency.AsyncCallbackServiceDependencyDescriptor;
import com.emc.microservice.samples.calculator.CalculatorNumber;
import com.emc.microservice.samples.calculator.MathEquation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 */
public class DepositToAccountMicroService extends MicroService {
    private static final String SERVICE_NAME = "Deposit To Account";
    private static final String SERVICE_IDENTIFIER = "deposit-to-account";
    private static final String SERVICE_DESCRIPTION = "Deposits data to bank account updating account balance";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(DepositToAccountMicroService.class);
    public static final String BANK_DB_NAME = "BankDB";

    public DepositToAccountMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        // Using the bank datasource
                        .withDatasource(BANK_DB_NAME, "Bank Database")

                        // Input is json DepositToAccountRequest via messaging
                        .withMainInput(
                                DepositToAccountRequest.class,
                                "Deposit request message",
                                DepositToAccountMessageListener.class,
                                new String[]{"accountId"})

                        // This service is dependant on calculator, doing async callbacks with calculator
                        .withAsyncServiceCallbackDependency(new AsyncCallbackServiceDependencyDescriptor(
                                "calculator",
                                true,
                                MathEquation.class,
                                CalculatorNumber.class,
                                DepositCalculatorResultCallback.class, 3, 60)
                        )

                        .withJacksonSerialization(DepositToAccountRequest.class)
        );
    }
}
