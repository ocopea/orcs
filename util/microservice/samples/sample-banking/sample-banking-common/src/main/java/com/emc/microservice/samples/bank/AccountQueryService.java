// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.ocopea.util.database.IntegerNativeQueryConverter;
import com.emc.ocopea.util.database.NativeQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created with true love by liebea on 10/13/2014.
 */
public class AccountQueryService {
    private static final Logger log = LoggerFactory.getLogger(AccountQueryService.class);
    private final NativeQueryService nativeQueryService;
    private static final String SQL_GET_ACCOUNT_BALANCE = "select balance from account where id=?";

    public AccountQueryService(NativeQueryService nativeQueryService) {
        this.nativeQueryService = nativeQueryService;
    }

    /**
     * Getting account balance
     *
     * @param accountId account id
     *
     * @return balance in Shpandrak Dollars
     */
    public int getAccountBalance(UUID accountId) {
        Integer accountBalance = nativeQueryService.getSingleValue(
                SQL_GET_ACCOUNT_BALANCE, new IntegerNativeQueryConverter(),
                Arrays.<Object>asList(accountId));

        if (accountBalance == null) {
            throw new IllegalArgumentException("Failed getting balance for account id " + accountId);
        }
        return accountBalance;
    }

    public List<BankAccount> list() {
        return nativeQueryService.getList("select * from account", this::convertRow);
    }

    public BankAccount get(UUID accountId) {
        return nativeQueryService.getSingleValue(
                "select * from account where id=?",
                this::convertRow,
                Collections.singletonList(accountId));
    }

    private BankAccount convertRow(ResultSet resultSet, int i) throws SQLException {
        return new BankAccount(
                UUID.fromString(resultSet.getString("id")),
                resultSet.getString("name"),
                resultSet.getLong("balance"));
    }
}
