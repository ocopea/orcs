// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.samples.bank;

import com.emc.microservice.samples.bank.BankAccount;
import com.emc.microservice.samples.bank.BankAccountResource;
import com.emc.microservice.samples.bank.BankDBSchemaBootstrap;
import com.emc.microservice.samples.bank.BankUIServerMicroService;
import com.emc.microservice.samples.bank.DepositToAccountMicroService;
import com.emc.microservice.samples.bank.DepositToAccountRequest;
import com.emc.microservice.samples.bank.DepositToAccountResource;
import com.emc.microservice.samples.calculator.CalculatorMicroService;
import com.emc.microservice.testing.MicroServiceIntegrationTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created with true love by liebea on 10/22/2014.
 */
public class TestBankUIServerIntegration {
    private MicroServiceIntegrationTestHelper integrationTestHelper;

    @Before
    public void init() throws SQLException, IOException {
        integrationTestHelper = new MicroServiceIntegrationTestHelper(Arrays.asList(new BankUIServerMicroService(),
                new DepositToAccountMicroService(),
                new CalculatorMicroService()));

        // Creating the Bank DB schema
        integrationTestHelper.createOrUpgradeSchema(new BankDBSchemaBootstrap());

        // Starting the services in test mode
        integrationTestHelper.startServiceInTestMode();
    }

    @After
    public void tearDown() {
        integrationTestHelper.stopTestMode();
    }

    @Test
    public void testDepositToAccount() throws IOException {
        BankAccountResource accountResource =
                integrationTestHelper.getServiceResource(BankAccountResource.class, "bank-ui");

        BankAccount account = accountResource.createAccount(new BankAccount(null, "Zurich Account", 0));
        Assert.assertNotNull(account);

        DepositToAccountResource depositToAccountResource =
                integrationTestHelper.getServiceResource(DepositToAccountResource.class, "bank-ui");
        Response rs = depositToAccountResource.deposit(new DepositToAccountRequest(account.getId(), 10));
        rs.close();

        account = accountResource.get(account.getId().toString());
        Assert.assertEquals("Deposit done (yey)", 10L, account.getBalance());

        rs = depositToAccountResource.deposit(new DepositToAccountRequest(account.getId(), 10));
        rs.close();
        rs = depositToAccountResource.deposit(new DepositToAccountRequest(account.getId(), 10));
        rs.close();
        rs = depositToAccountResource.deposit(new DepositToAccountRequest(account.getId(), 10));
        rs.close();

        account = accountResource.get(account.getId().toString());
        Assert.assertEquals("Deposit done (yey)", 40L, account.getBalance());

    }

}
