// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.Context;
import com.emc.microservice.dependency.ServiceResultCallback;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.samples.calculator.CalculatorNumber;
import com.emc.microservice.singleton.ServiceLifecycle;
import com.emc.ocopea.util.database.BasicNativeQueryService;

import java.util.UUID;

/**
 * Created with true love by liebea on 10/13/2014.
 */
public class DepositCalculatorResultCallback implements ServiceResultCallback, ServiceLifecycle {
    private UpdateAccountService updateAccountService;

    @Override
    public void onTimeOut(Message message, Context context) {
        //todo: mark transaction as canceled? retry?
    }

    @Override
    public void onMessage(Message message, Context context) {
        CalculatorNumber calculatorNumber = message.readObject(CalculatorNumber.class);

        //todo: Are headers the right way to pass state? maybe..
        updateAccountService.updateBalance(
                UUID.fromString(message.getContextValue("accountId")),
                (int) calculatorNumber.getNumber());
    }

    @Override
    public void onErrorMessage(Message message, Context context) {

    }

    @Override
    public void init(Context context) {
        this.updateAccountService = new UpdateAccountService(new BasicNativeQueryService(context
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
