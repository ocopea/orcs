// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.calcualtor;

import com.emc.microservice.samples.calculator.CalculatorMicroService;
import com.emc.microservice.samples.calculator.CalculatorNumber;
import com.emc.microservice.samples.calculator.MathEquation;
import com.emc.microservice.testing.MicroServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class TestCalculator {
    private MicroServiceTestHelper calculatorTestHelper;

    @Before
    public void init() {
        calculatorTestHelper = new MicroServiceTestHelper(new CalculatorMicroService());
        calculatorTestHelper.startServiceInTestMode();
    }

    @After
    public void tearDown() {
        calculatorTestHelper.stopTestMode();
    }

    @Test
    public void testSum() throws IOException {

        // Input message JSON String
        MathEquation inputMessage = new MathEquation("+", "3", "2");

        // Executing service and testing result
        CalculatorNumber calculatorNumber = calculatorTestHelper.executeServiceAndReturnResult(MathEquation.class,
                inputMessage,
                Collections.<String, String>emptyMap(),
                CalculatorNumber.class);

        // Validating service result
        Assert.assertEquals("Invalid result", 5L, calculatorNumber.getNumber());
    }

    @Test
    public void testSubstract() throws IOException {

        // Input message JSON String
        MathEquation inputMessage = new MathEquation("-", "10", "2");

        // Executing service and testing result
        CalculatorNumber calculatorNumber = calculatorTestHelper.executeServiceAndReturnResult(MathEquation.class,
                inputMessage,
                Collections.<String, String>emptyMap(),
                CalculatorNumber.class);

        // Validating service result
        Assert.assertEquals("Invalid result", 8L, calculatorNumber.getNumber());
    }

    @Test
    public void testMultiply() throws IOException {

        // Input message JSON String
        MathEquation inputMessage = new MathEquation("*", "10", "10");

        // Executing service and testing result
        CalculatorNumber calculatorNumber = calculatorTestHelper.executeServiceAndReturnResult(MathEquation.class,
                inputMessage,
                Collections.<String, String>emptyMap(),
                CalculatorNumber.class);

        // Validating service result
        Assert.assertEquals("Invalid result", 100L, calculatorNumber.getNumber());
    }

    @Test
    public void testDivision() throws IOException {

        // Input message JSON String
        MathEquation inputMessage = new MathEquation("/", "10", "5");

        // Executing service and testing result
        CalculatorNumber calculatorNumber = calculatorTestHelper.executeServiceAndReturnResult(MathEquation.class,
                inputMessage,
                Collections.<String, String>emptyMap(),
                CalculatorNumber.class);

        // Validating service result
        Assert.assertEquals("Invalid result", 2L, calculatorNumber.getNumber());
    }

    @Test
    public void testModulus() throws IOException {

        // Input message JSON String
        MathEquation inputMessage = new MathEquation("%", "11", "5");

        // Executing service and testing result
        CalculatorNumber calculatorNumber = calculatorTestHelper.executeServiceAndReturnResult(MathEquation.class,
                inputMessage,
                Collections.<String, String>emptyMap(),
                CalculatorNumber.class);

        // Validating service result
        Assert.assertEquals("Invalid result", 1L, calculatorNumber.getNumber());
    }
}
