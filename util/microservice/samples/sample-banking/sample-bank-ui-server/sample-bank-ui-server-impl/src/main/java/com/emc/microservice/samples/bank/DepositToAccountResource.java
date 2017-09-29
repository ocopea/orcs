// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.messaging.MessageSender;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with true love by liebea on 10/23/2014.
 */
public class DepositToAccountResource implements DepositToAccountAPI {

    private MessageSender depositMessageSender;

    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        MicroServiceApplication microServiceApplication = (MicroServiceApplication) application;
        depositMessageSender = microServiceApplication.getMicroServiceContext().getDependencyManager()
                .getManagedResourceByName("deposit-to-account").getMessageSender();
    }

    @Override
    public Response deposit(DepositToAccountRequest depositToAccountRequest) {
        Map<String, String> messageHeaders = new HashMap<>();
        messageHeaders.put("accountId", depositToAccountRequest.getAccountId().toString());
        depositMessageSender.sendMessage(DepositToAccountRequest.class, depositToAccountRequest, messageHeaders);
        return Response.ok().build();
    }
}
