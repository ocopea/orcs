// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.calculator;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class MathEquation {
    private final String operator;
    private final String rightOperand;
    private final String leftOperand;

    private MathEquation() {
        this(null, null, null);
    }

    public MathEquation(String operator, String leftOperand, String rightOperand) {
        this.operator = operator;
        this.rightOperand = rightOperand;
        this.leftOperand = leftOperand;
    }

    public String getOperator() {
        return operator;
    }

    public String getRightOperand() {
        return rightOperand;
    }

    public String getLeftOperand() {
        return leftOperand;
    }
}
