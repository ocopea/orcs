// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.calculator;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class CalculatorNumber {
    private final long number;

    private CalculatorNumber() {
        this(0L);
    }

    public CalculatorNumber(long number) {
        this.number = number;
    }

    public long getNumber() {
        return number;
    }
}
