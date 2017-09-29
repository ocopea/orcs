// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.samples.calculator.CalculatorNumber;
import com.emc.microservice.testing.MicroServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Created with true love by liebea on 10/13/2014.
 */
public class TestDepositToAccountMicroService {

    private MicroServiceTestHelper depositTestHelper;
    private CreateAccountService createAccountService;
    private AccountQueryService accountQueryService;

    @Before
    public void init() throws SQLException, IOException {

        // Instantiating test helper
        depositTestHelper = new MicroServiceTestHelper(new DepositToAccountMicroService());

        // Creating the bank db schema
        depositTestHelper.createOrUpgrdaeSchema(new BankDBSchemaBootstrap());

        // Starting service in test mode
        depositTestHelper.startServiceInTestMode();

        // instantiating helper services
        createAccountService = new CreateAccountService(depositTestHelper.getNativeQueryService());
        accountQueryService = new AccountQueryService(depositTestHelper.getNativeQueryService());

    }

    @After
    public void tearDown() {
        depositTestHelper.stopTestMode();
    }

    @Test
    public void testBasic() throws IOException {

        // Mocking the calculator service to always return same result
        depositTestHelper.mockDependentServiceWithStaticOutput(
                "calculator",
                new CalculatorNumber(1000),
                CalculatorNumber.class);

        // Creating my Switzerland account, in which I store all the money I steal from the rich
        BankAccount account = createAccountService.createAccount("My Secret Switzerland account");

        // Deposit 1000 $ I received from my grandmother for my Bar Mitzva
        DepositToAccountRequest depositToAccountRequest = new DepositToAccountRequest(account.getId(), 1000);

        // Executing the service
        depositTestHelper.executeService(depositToAccountRequest, DepositToAccountRequest.class, null);

        Assert.assertEquals(
                "Should have deposited the money!!! thief!",
                1000,
                accountQueryService.getAccountBalance(account.getId()));
    }
}
