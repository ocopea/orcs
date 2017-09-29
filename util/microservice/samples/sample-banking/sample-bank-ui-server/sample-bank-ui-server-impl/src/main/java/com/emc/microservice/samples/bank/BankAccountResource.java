// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.MicroService;
import com.emc.microservice.MicroServiceApplication;
import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import com.emc.ocopea.util.database.BasicNativeQueryService;

import javax.sql.DataSource;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Application;
import java.util.List;
import java.util.UUID;

/**
 * Created with true love by liebea on 10/23/2014.
 */
public class BankAccountResource implements BankAccountAPI {
    private CreateAccountService createAccountService;
    private AccountQueryService accountQueryService;
    private CreateAccountReportService createAccountReportService;

    @NoJavadoc
    @javax.ws.rs.core.Context
    public void setApplication(Application application) {
        MicroServiceApplication microServiceApplication = (MicroServiceApplication) application;
        DataSource datasource = microServiceApplication
                .getMicroServiceContext()
                .getDatasourceManager()
                .getManagedResourceByName(BankUIServerMicroService.BANK_DB_NAME)
                .getDataSource();
        BasicNativeQueryService nativeQueryService = new BasicNativeQueryService(datasource);
        this.createAccountService = new CreateAccountService(nativeQueryService);
        this.accountQueryService = new AccountQueryService(nativeQueryService);
        BlobStoreAPI blobStoreAPI = microServiceApplication.getMicroServiceContext()
                .getBlobStoreManager().getManagedResourceByName(MicroService.DEFAULT_BLOBSTORE_NAME).getBlobStoreAPI();
        createAccountReportService = new CreateAccountReportService(blobStoreAPI, accountQueryService);
    }

    @Override
    public BankAccount createAccount(BankAccount bankAccount) {
        return this.createAccountService.createAccount(bankAccount.getName());
    }

    @Override
    public List<BankAccount> getAll() {
        return accountQueryService.list();
    }

    @Override
    public BankAccount get(@PathParam("id") String id) {
        return accountQueryService.get(UUID.fromString(id));
    }

    @Override
    public AccountReportId createBankStatusReport() throws Exception {
        return createAccountReportService.createReport();
    }
}
