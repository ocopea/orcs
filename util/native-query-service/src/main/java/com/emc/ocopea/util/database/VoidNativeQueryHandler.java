// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: liebea
 * Date: 8/30/12
 * Time: 2:45 PM
 */
public class VoidNativeQueryHandler implements NativeQueryHandler<Void> {

    protected NativeQueryRowBatchConverter<Void> converter;

    public VoidNativeQueryHandler(NativeQueryRowBatchConverter<Void> converter) {
        this.converter = converter;
    }

    @Override
    public Void handleNativeQueryResult(ResultSet rset) throws SQLException, NativeQueryException {
        while (rset.next()) {
            converter.convertRow(rset, NativeQueryRowBatchConverter.ALL_FIELDS);
        }
        converter.finalizeBatch(null);

        return null;
    }
}
