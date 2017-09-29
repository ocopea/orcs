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
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class BankAccountSteps {
    private MicroServiceIntegrationTestHelper integrationTestHelper;
    private BankAccount account;
    private BankAccountResource bankAccountResource;

    @Before
    public void before() throws IOException, SQLException {
        integrationTestHelper = new MicroServiceIntegrationTestHelper(Arrays.asList(new BankUIServerMicroService(),
                new DepositToAccountMicroService(),
                new CalculatorMicroService()));

        integrationTestHelper.createOrUpgradeSchema(new BankDBSchemaBootstrap());

        integrationTestHelper.startServiceInTestMode();
    }

    @After
    public void after() {
        integrationTestHelper.stopTestMode();
    }

    @Given("^a bank account called \"([^\"]*)\" with balance of (\\d+)$")
    public void createBankAccount(String name, int balance) throws Throwable {
        bankAccountResource = integrationTestHelper.getServiceResource(BankAccountResource.class, "bank-ui");
        account = bankAccountResource.createAccount(new BankAccount(null, name, balance));
    }

    @When("^(\\d+) is deposited$")
    public void deposit(int amount) throws Throwable {
        DepositToAccountRequest depositToAccountRequest = new DepositToAccountRequest(account.getId(), amount);

        integrationTestHelper.executeService(
                "deposit-to-account",
                depositToAccountRequest,
                DepositToAccountRequest.class);
    }

    @Then("^bank balance is (\\d+)$")
    public void checkBalance(int expected) throws Throwable {
        assertEquals(
                "Should have deposited the money!!! thief!",
                expected,
                bankAccountResource.get(account.getId().toString()).getBalance());
    }

}
