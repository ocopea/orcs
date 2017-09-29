// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.protection;

import com.emc.ocopea.site.crb.CrbNegotiationResult;

/**
 * Created by liebea on 2/5/17.
 * Drink responsibly
 */
public interface CopyRepositoryNegotiator {

    /**
     * todo: protocol must include supported CR protocols from dsb and more info... just a mock now
     *
     * @return CRB details to use
     */
    CrbNegotiationResult findCrb();

}
