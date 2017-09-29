// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.dpa.samples.bank;

import com.emc.microservice.samples.bank.BankAccount;
import com.emc.microservice.samples.bank.BankAccountResource;
import com.emc.microservice.samples.bank.BankDBSchemaBootstrap;
import com.emc.microservice.samples.bank.BankUIServerMicroService;
import com.emc.microservice.samples.bank.DepositToAccountMicroService;
import com.emc.microservice.samples.bank.DepositToAccountRequest;
import com.emc.microservice.samples.calculator.CalculatorMicroService;
import com.emc.microservice.testing.MicroServiceIntegrationTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Created with true love by liebea on 10/22/2014.
 */
public class TestDepositIntegration {
    private MicroServiceIntegrationTestHelper integrationTestHelper;

    @Before
    public void init() throws SQLException, IOException {
        integrationTestHelper = new MicroServiceIntegrationTestHelper(Arrays.asList(new BankUIServerMicroService(),
                new DepositToAccountMicroService(),
                new CalculatorMicroService()));

        integrationTestHelper.createOrUpgradeSchema(new BankDBSchemaBootstrap());

        integrationTestHelper.startServiceInTestMode();

    }

    @After
    public void tearDown() {
        integrationTestHelper.stopTestMode();
    }

    @Test
    public void testBasic() throws IOException {

        BankAccountResource bankAccountResource =
                integrationTestHelper.getServiceResource(BankAccountResource.class, "bank-ui");
        BankAccount account =
                bankAccountResource.createAccount(new BankAccount(null, "My Secret Switzerland account", 0L));

        // Deposit 1000 $ I received from my grandmother for my Bar Mitzva
        DepositToAccountRequest depositToAccountRequest = new DepositToAccountRequest(account.getId(), 1000);

        integrationTestHelper.executeService(
                "deposit-to-account",
                depositToAccountRequest,
                DepositToAccountRequest.class);

        Assert.assertEquals(
                "Should have deposited the money!!! thief!",
                1000,
                bankAccountResource.get(account.getId().toString()).getBalance());
    }

}
