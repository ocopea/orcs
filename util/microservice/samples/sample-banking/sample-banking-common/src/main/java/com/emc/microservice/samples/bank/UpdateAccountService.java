// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import com.emc.ocopea.util.database.NativeQueryService;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created with true love by liebea on 10/14/2014.
 */
public class UpdateAccountService {
    private final NativeQueryService nativeQueryService;
    private static final String SQL_UPDATE_ACCOUNT_BALANCE = "update account set balance = ? where id=?";

    public UpdateAccountService(NativeQueryService nativeQueryService) {
        this.nativeQueryService = nativeQueryService;
    }

    /**
     * Getting account balance
     *
     * @param accountId account id
     * @param newBalance new balance in Shpandrak Dollars
     */
    public void updateBalance(UUID accountId, int newBalance) {
        nativeQueryService.executeUpdate(
                SQL_UPDATE_ACCOUNT_BALANCE,
                Arrays.<Object>asList(newBalance, accountId));

    }

}
