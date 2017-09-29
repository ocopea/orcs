// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.samples.bank;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Handmade code created by ohanaa Date: 3/1/15 Time: 3:43 PM
 */
@Path("/account")
public interface BankAccountAPI {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public BankAccount createAccount(BankAccount bankAccount);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<BankAccount> getAll();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public BankAccount get(@PathParam("id") String id);

    @GET
    @Path("/report/bank/status")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountReportId createBankStatusReport() throws Exception;
}
