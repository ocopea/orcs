// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.database;

/**
 * @author nivenb
 */
public class FetchSpecification {

    private static final FetchSpecification useDriverDefault =
            new FetchSpecification(FetchBehaviour.USE_DRIVER_DEFAULT, -1);
    private final FetchBehaviour fetchBehaviour;

    private final int specificFetchSize;

    private FetchSpecification(FetchBehaviour fetchBehaviour, int specificFetchSize) {
        this.fetchBehaviour = fetchBehaviour;
        this.specificFetchSize = specificFetchSize;
    }

    /**
     * Obtain a FetchSpecification that specifies to use the runtime jdbc driver default. i.e. Do not set an explicit
     * fetch size
     */
    public static FetchSpecification getUseDriverDefault() {
        return useDriverDefault;
    }

    /**
     * Obtain a FetchSpecification that _suggests_ a fetch size for the runtime jdbc driver to adhere to. Please note
     * that whether the driver adheres to this, is vendor specific
     */
    public static FetchSpecification getSuggestSpecificFetchSize(int specificFetchSize) {
        return new FetchSpecification(FetchBehaviour.SUGGEST_SPECIFIC_SIZE, specificFetchSize);
    }

    FetchBehaviour getFetchBehaviour() {
        return fetchBehaviour;
    }

    int getSpecificFetchSize() {
        return specificFetchSize;
    }

    enum FetchBehaviour {
        USE_DRIVER_DEFAULT, SUGGEST_SPECIFIC_SIZE
    }
}
