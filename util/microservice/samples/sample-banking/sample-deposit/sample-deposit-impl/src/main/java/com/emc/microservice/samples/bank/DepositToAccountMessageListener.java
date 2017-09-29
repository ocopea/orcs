// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.Context;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.messaging.MessageSender;
import com.emc.microservice.samples.calculator.MathEquation;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.database.BasicNativeQueryService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 * This class handles all the incoming input messages sent to the Deposit service
 */
public class DepositToAccountMessageListener implements MessageListener, ServiceLifecycle {

    /***
     * Used to interact with calculator service
     */
    private MessageSender calculatorMessageSender;

    /***
     * Used to query account information
     */
    private AccountQueryService accountQueryService;

    @Override
    public void onMessage(Message message, Context context) {
        // Parsing input
        DepositToAccountRequest depositToAccountRequest = message.readObject(DepositToAccountRequest.class);

        // Reading current account balance from the database
        int currAccountBalance = readAccountBalance(depositToAccountRequest.getAccountId());

        // Calculating new balance after deposit using calculator, the result/error handling will be handled
        // by the callback See @DepositCalculatorResultCallback
        sendCalculationRequestToCalculator(
                currAccountBalance,
                depositToAccountRequest.getAmountToDeposit(),
                depositToAccountRequest.getAccountId());
    }

    @Override
    public void onErrorMessage(Message message, Context context) {

    }

    private void sendCalculationRequestToCalculator(int currAccountBalance, int amountToDeposit, UUID accountId) {
        // Input message JSON String
        final MathEquation calculatorInput =
                new MathEquation("+", String.valueOf(currAccountBalance), String.valueOf(amountToDeposit));

        // Sending the calculation request to the calculator service
        Map<String, String> contextHeaders = new HashMap<>();
        contextHeaders.put("accountId", accountId.toString());
        calculatorMessageSender.sendMessage(
                MathEquation.class,
                calculatorInput,
                Collections.emptyMap(),
                contextHeaders);
    }

    private int readAccountBalance(UUID accountId) {
        return accountQueryService.getAccountBalance(accountId);
    }

    @Override
    public void init(Context context) {
        this.calculatorMessageSender =
                context.getDependencyManager().getManagedResourceByName("calculator").getMessageSender();
        this.accountQueryService = new AccountQueryService(new BasicNativeQueryService(context
                .getDatasourceManager()
                .getManagedResourceByName(DepositToAccountMicroService.BANK_DB_NAME)
                .getDataSource()));
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void shutDown() {

    }
}
