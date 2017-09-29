// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.calculator;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;

import java.util.Collections;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class CalculatorInputMessageListener implements MessageListener {

    @Override
    public void onMessage(Message message, Context context) {
        // Reading calculator input message from message
        MathEquation mathEquation = message.readObject(MathEquation.class);

        // Calculating the result
        Long result = calculateResult(mathEquation);

        // Outputting the result
        sendResult(context, message, result);
    }

    @Override
    public void onErrorMessage(Message message, Context context) {
        // TODO
    }

    private void sendResult(Context context, Message message, final Long result) {
        context
                .getOutputMessageSender(message)
                .sendMessage(CalculatorNumber.class,
                        new CalculatorNumber(result),
                        Collections.emptyMap(),
                        message.getMessageContext());
    }

    private Long calculateResult(MathEquation mathEquation) {
        Long result;
        Long left = Long.valueOf(mathEquation.getLeftOperand());
        Long right = Long.valueOf(mathEquation.getRightOperand());
        switch (mathEquation.getOperator()) {
            case "+":
                result = left + right;
                break;
            case "-":
                result = left - right;
                break;
            case "*":
                result = left * right;
                break;
            case "/":
                result = left / right;
                break;
            case "%":
                result = left % right;
                break;
            default:
                throw new IllegalArgumentException("Invalid operator type " + mathEquation.getOperator());
        }
        return result;
    }

}
