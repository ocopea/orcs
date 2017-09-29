// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.calculator;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceInitializationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class CalculatorMicroService extends MicroService {
    private static final String SERVICE_NAME = "Calculator";
    private static final String SERVICE_IDENTIFIER = "calculator";
    private static final String SERVICE_DESCRIPTION = "Calculates basic operations";
    private static final int SERVICE_VERSION = 1;
    private static final Logger logger = LoggerFactory.getLogger(CalculatorMicroService.class);

    public CalculatorMicroService() {
        super(
                SERVICE_NAME,
                SERVICE_IDENTIFIER,
                SERVICE_DESCRIPTION,
                SERVICE_VERSION,
                logger,
                new MicroServiceInitializationHelper()

                        // Calculator's main input
                        .withMainInput(
                                MathEquation.class,
                                "Calculator input math expression",
                                CalculatorInputMessageListener.class,
                                null)

                        // Main Output
                        .withMainOutput(
                                CalculatorNumber.class,
                                "A number representing calculated result")

        );

    }
}
