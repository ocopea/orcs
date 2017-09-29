// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.ocopea.util.database.NativeQueryService;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created with true love by liebea on 10/13/2014.
 */
public class CreateAccountService {
    private final NativeQueryService nativeQueryService;

    public CreateAccountService(NativeQueryService nativeQueryService) {
        this.nativeQueryService = nativeQueryService;
    }

    /**
     * Creates a new bank account
     *
     * @param name account name
     *
     * @return UUID representing this bank account
     */
    public BankAccount createAccount(String name) {
        UUID newAccountId = UUID.randomUUID();
        int initialBalance = 0;
        nativeQueryService.executeUpdate(
                "insert into account (id, name, balance) values (?, ?, ?)",
                Arrays.<Object>asList(newAccountId, name, initialBalance));
        return new BankAccount(newAccountId, name, initialBalance);
    }
}
