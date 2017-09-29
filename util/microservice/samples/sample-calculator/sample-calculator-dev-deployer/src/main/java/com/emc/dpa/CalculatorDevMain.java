// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa;

import com.emc.dpa.dev.DevResourceProvider;
import com.emc.microservice.runner.MicroServiceRunner;
import com.emc.microservice.samples.calculator.CalculatorMicroService;

/**
 * Created by liebea on 10/6/2014. Enjoy it
 */
public class CalculatorDevMain {
    public static void main(String[] args) throws Exception {
        new MicroServiceRunner().run(new DevResourceProvider(), new CalculatorMicroService());
    }
}
