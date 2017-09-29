// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import java.util.UUID;

/**
 * Created with true love by liebea on 10/13/2014.
 */
public class DepositToAccountRequest {
    private UUID accountId;
    private int amountToDeposit;

    protected DepositToAccountRequest() {
    }

    public DepositToAccountRequest(UUID accountId, int amountToDeposit) {
        this.accountId = accountId;
        this.amountToDeposit = amountToDeposit;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public int getAmountToDeposit() {
        return amountToDeposit;
    }

    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public void setAmountToDeposit(int amountToDeposit) {
        this.amountToDeposit = amountToDeposit;
    }
}
