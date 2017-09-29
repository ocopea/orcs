// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import java.util.UUID;

/**
 * Created with true love by liebea on 10/13/2014.
 */
public class BankAccount {
    private final UUID id;
    private final String name;
    private final long balance;

    protected BankAccount() {
        this(null, null, 0);
    }

    public BankAccount(UUID id, String name, long balance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getBalance() {
        return balance;
    }
}
