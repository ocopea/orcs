// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import com.emc.dpa.dev.DevResourceProvider;
import com.emc.dpa.dev.manager.DevModeServiceManagerAPI;
import com.emc.microservice.healthcheck.MicroServiceHealthCheckRegistry;
import com.emc.microservice.samples.bank.BankAccount;
import com.emc.microservice.samples.bank.BankAccountAPI;
import com.emc.microservice.samples.bank.DepositToAccountAPI;
import com.emc.microservice.samples.bank.DepositToAccountMicroService;
import com.emc.microservice.samples.bank.DepositToAccountRequest;
import com.emc.microservice.testing.PlatformTestHelper;
import com.emc.ocopea.services.rest.MicroServiceConfigurationAPI;
import com.emc.ocopea.services.rest.MicroServiceMetricsAPI;
import com.emc.ocopea.services.rest.MicroServiceStateAPI;
import com.emc.ocopea.services.rest.ServiceConfiguration;
import com.emc.ocopea.services.rest.ServiceState;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Handmade code created by ohanaa Date: 2/25/15 Time: 6:28 PM
 */
public class MicroserviceSamplePlatformTest {
    private static PlatformTestHelper platformTestHelper;
    private static final int HEALTH_CHECK_PERIOD = 15;

    @BeforeClass
    public static void init() throws Exception {
        List<String> services = Arrays.asList("deposit-to-account", "bank-ui", "calculator", "dev-mode-manager");
        services.forEach(s -> System.setProperty(
                s + "_" + MicroServiceHealthCheckRegistry.HEALTH_CHECK_PERIOD_PARAMETER_NAME,
                String.valueOf(HEALTH_CHECK_PERIOD)));
        DevResourceProvider devResourceProvider = SampleDevRunner.runSample();

        platformTestHelper = new PlatformTestHelper(new HashSet<>(services), devResourceProvider);
        platformTestHelper.start(0);
    }

    @AfterClass
    public static void clean() {
        System.clearProperty(MicroServiceHealthCheckRegistry.HEALTH_CHECK_PERIOD_PARAMETER_NAME);
    }

    @Test
    public void testDeposit() throws InterruptedException {
        BankAccountAPI bankAccount = platformTestHelper.createResource("bank-ui", BankAccountAPI.class);
        BankAccount accountCreated = bankAccount.createAccount(new BankAccount(null, "account1", 0));
        Assert.assertNotNull(accountCreated);

        DepositToAccountAPI deposit = platformTestHelper.createResource("bank-ui", DepositToAccountAPI.class);
        Response resp = deposit.deposit(new DepositToAccountRequest(accountCreated.getId(), 200));
        try {
            Assert.assertEquals("Unsuccessful deposit", Response.Status.OK.getStatusCode(), resp.getStatus());
        } finally {
            resp.close();
        }

        Thread.sleep(2000);

        BankAccount updatedAccount = bankAccount.get(accountCreated.getId().toString());
        Assert.assertEquals("Incorrect account balance", 200, updatedAccount.getBalance());
    }

    @Test
    public void testDepositConfiguration() {
        MicroServiceConfigurationAPI config =
                platformTestHelper.createResource("deposit-to-account", MicroServiceConfigurationAPI.class);
        ServiceConfiguration depositConfiguration = config.getServiceConfiguration();

        Assert.assertNotNull(depositConfiguration);
        Assert.assertEquals("Incorrect number of queues", 2, depositConfiguration.getInputQueues().size());
        Assert.assertEquals("Incorrect number of datasources", 1, depositConfiguration.getDatasources().size());
        Assert.assertEquals(
                "Incorrect number of dependencies",
                1,
                depositConfiguration.getServiceDependencies().size());
    }

    @Test
    public void testDepositMetrics() throws IOException {
        MicroServiceMetricsAPI metrics =
                platformTestHelper.createResource("deposit-to-account", MicroServiceMetricsAPI.class);
        Response depositMetrics = metrics.getMetricsOutput();

        try {
            Assert.assertNotNull(depositMetrics);
        } finally {
            depositMetrics.close();
        }
    }

    @Test
    public void testDepositState() {
        checkDepositStatus(ServiceState.ServiceStateEnum.RUNNING);
    }

    private void checkDepositStatus(ServiceState.ServiceStateEnum status) {
        MicroServiceStateAPI state =
                platformTestHelper.createResource("deposit-to-account", MicroServiceStateAPI.class);
        ServiceState depositState = state.getServiceState();

        Assert.assertNotNull(depositState);
        Assert.assertEquals("Unexpected Deposit status", status, depositState.getState());
    }

    @Test
    public void testDepositStateMetricsAndConfigurationWithPausedDB() throws InterruptedException, IOException {
        checkDepositStatus(ServiceState.ServiceStateEnum.RUNNING);
        pauseBankDB();
        Thread.sleep((HEALTH_CHECK_PERIOD + 1) * 1000);

        checkDepositStatus(ServiceState.ServiceStateEnum.PAUSED);
        testDepositMetrics();
        testDepositConfiguration();
        verifyDepositIsUnavailable();

        // resume DB to allow other tests to run with running and valid deposit service
        resumeBankDB();
    }

    private void verifyDepositIsUnavailable() {
        DepositToAccountAPI deposit = platformTestHelper.createResource("bank-ui", DepositToAccountAPI.class);
        Response resp = deposit.deposit(new DepositToAccountRequest(UUID.randomUUID(), 200));
        try {
            Assert.assertEquals(
                    "Deposit should be unavailable",
                    Response.Status.SERVICE_UNAVAILABLE.getStatusCode(),
                    resp.getStatus());
        } finally {
            resp.close();
        }
    }

    @Test
    public void testDepositIsPausedAfterError() throws InterruptedException {
        checkDepositStatus(ServiceState.ServiceStateEnum.RUNNING);
        pauseBankDB();

        DepositToAccountAPI deposit = platformTestHelper.createResource("bank-ui", DepositToAccountAPI.class);
        Response rs = deposit.deposit(new DepositToAccountRequest(UUID.randomUUID(), 200));
        rs.close();
        Thread.sleep(1000);

        // now health check should run and mark deposit as paused
        checkDepositStatus(ServiceState.ServiceStateEnum.PAUSED);

        // resume DB to allow other tests to run with running and valid deposit service
        resumeBankDB();
    }

    private void pauseBankDB() {
        DevModeServiceManagerAPI devManager =
                platformTestHelper.createResource("dev-mode-manager", DevModeServiceManagerAPI.class);
        devManager.pauseDB(DepositToAccountMicroService.BANK_DB_NAME);
    }

    private void resumeBankDB() throws InterruptedException {
        DevModeServiceManagerAPI devManager =
                platformTestHelper.createResource("dev-mode-manager", DevModeServiceManagerAPI.class);
        devManager.resumeDB(DepositToAccountMicroService.BANK_DB_NAME);
        Thread.sleep((HEALTH_CHECK_PERIOD + 1) * 1000);

        checkDepositStatus(ServiceState.ServiceStateEnum.RUNNING);
    }
}
