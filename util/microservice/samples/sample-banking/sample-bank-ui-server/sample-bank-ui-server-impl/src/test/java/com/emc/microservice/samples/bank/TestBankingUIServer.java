// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.Context;
import com.emc.microservice.MicroService;
import com.emc.microservice.messaging.Message;
import com.emc.microservice.messaging.MessageListener;
import com.emc.microservice.testing.MicroServiceTestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with true love by liebea on 10/13/2014.
 */
public class TestBankingUIServer {

    private MicroServiceTestHelper bankUITestHelper;

    @Before
    public void init() throws SQLException, IOException {
        bankUITestHelper = new MicroServiceTestHelper(new BankUIServerMicroService());

        // Creating the bank db schema
        bankUITestHelper.createOrUpgrdaeSchema(new BankDBSchemaBootstrap());

        // Starting the service in test mode
        bankUITestHelper.startServiceInTestMode();

    }

    @After
    public void tearDown() {
        bankUITestHelper.stopTestMode();
    }

    @Test
    public void testCreateAccount() throws IOException {
        BankAccountResource accountResource = bankUITestHelper.getServiceResource(BankAccountResource.class);
        BankAccount account = accountResource.createAccount(new BankAccount(null, "Geneva Account", 0));
        Assert.assertNotNull(account);

        BankAccount accountAgain = accountResource.get(account.getId().toString());
        Assert.assertEquals("Should be the same account", account.getName(), accountAgain.getName());

    }

    @Test
    public void testAccountsReport() throws Exception {
        BankAccountResource accountResource = bankUITestHelper.getServiceResource(BankAccountResource.class);
        accountResource.createAccount(new BankAccount(null, "Boris Account", 0));
        accountResource.createAccount(new BankAccount(null, "Donkey Account", 0));

        AccountReportId key = accountResource.createBankStatusReport();

        // Reading the report directly from blobstore to see it is there
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bankUITestHelper
                .getContext()
                .getBlobStoreManager()
                .getManagedResourceByName(MicroService.DEFAULT_BLOBSTORE_NAME)
                .
                        getBlobStoreAPI()
                .readBlob(key.getNameSpace(), key.getKey(), out);
        String report = out.toString();
        Assert.assertNotNull(report);
        System.out.println(report);

    }

    @Test
    public void testDepositToAccount() throws IOException {
        BankAccountResource accountResource = bankUITestHelper.getServiceResource(BankAccountResource.class);
        final BankAccount account = accountResource.createAccount(new BankAccount(null, "Zurich Account", 0));
        Assert.assertNotNull(account);

        final AtomicBoolean messageInvoked = new AtomicBoolean(false);
        bankUITestHelper.mockDependentService("deposit-to-account", new MessageListener() {
            @Override
            public void onMessage(Message message, Context context) {
                DepositToAccountRequest depositToAccountRequest = message.readObject(DepositToAccountRequest.class);
                Assert.assertEquals("Incorrect account id", depositToAccountRequest.getAccountId(), account.getId());
                Assert.assertEquals("Incorrect amount to deposit", 10, depositToAccountRequest.getAmountToDeposit());
                messageInvoked.set(true);

            }

            @Override
            public void onErrorMessage(Message message, Context context) {

            }
        });

        DepositToAccountResource depositToAccountResource =
                bankUITestHelper.getServiceResource(DepositToAccountResource.class);
        Response rs = depositToAccountResource.deposit(new DepositToAccountRequest(account.getId(), 10));
        rs.close();

        // Verifying the message was sent to deposit microservice
        Assert.assertTrue("Deposit message was not sent to deposit micro-service", messageInvoked.get());

    }
}
