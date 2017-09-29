// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.restapi;

import com.emc.microservice.Context;

import java.util.Set;

/**
 * Created by liebea on 10/5/2014. Enjoy it
 * Interface for implementing a microservice webserver
 */
public interface MicroServiceWebServer {

    /**
     * Deploy web application described by the context
     *
     * @param context contest of the app being deployed.
     */
    void deployServiceApplication(Context context);

    /***
     * Undeploy the app from the current context
     * @param context context of the current app.
     */
    void unDeployServiceApplication(Context context);


    /***
     * lists urns of web apps deployed on this server
     */
    Set<String> listDeploymentURNs();

    /***
     * Returns the port used by this web server instance.
     */
    int getPort();
}
