// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Handmade code created by ohanaa Date: 3/1/15 Time: 3:09 PM
 */
@Path("/deposit-to-account")
public interface DepositToAccountAPI {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deposit(DepositToAccountRequest depositToAccountRequest);
}
