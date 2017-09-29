// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.microservice.blobstore.BlobStoreAPI;
import com.emc.ocopea.devtools.checkstyle.NoJavadoc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by liebea on 11/19/14.
 * Drink responsibly
 */
public class CreateAccountReportService {
    private static final Logger log = LoggerFactory.getLogger(CreateAccountReportService.class);
    private final BlobStoreAPI blobStoreAPI;
    private final AccountQueryService accountQueryService;

    public CreateAccountReportService(BlobStoreAPI blobStoreAPI, AccountQueryService accountQueryService) {
        this.blobStoreAPI = blobStoreAPI;
        this.accountQueryService = accountQueryService;
    }

    @NoJavadoc
    public AccountReportId createReport() throws Exception {
        final List<BankAccount> accountList = accountQueryService.list();
        String reportKey = "report" + UUID.randomUUID().toString();
        Map<String, String> headers = new HashMap<>();
        headers.put("retention", "-1");
        headers.put("generated-at", Long.toString(new Date().getTime()));
        String namespace = "bank-example";
        blobStoreAPI.create(namespace, reportKey, headers, out -> {
            try {
                out.write("[".getBytes());
                String reportString;
                String prefix = "";
                for (BankAccount account : accountList) {
                    reportString = prefix + "{\"id\":\"" + account.getId() + "\",\"name\":\"" + account.getName() +
                            "\",\"balance\":\"" + account.getBalance() + "\"}";
                    out.write(reportString.getBytes());
                    prefix = ",";
                }
                out.write("]".getBytes());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        log.info("Bank Accounts report available at /objects-api/bank-example/" + reportKey);
        return new AccountReportId(namespace, reportKey);
    }

}
