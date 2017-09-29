// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.hub.application;

import javax.ws.rs.ClientErrorException;

/**
 * Created by liebea on 3/30/16.
 * Drink responsibly
 */
public class AppAlreadyDeployedException extends ClientErrorException {

    public AppAlreadyDeployedException(String appTemplateName, String site, String appName, Throwable throwable) {
        super("App " + appName + "(" + appTemplateName + ") already deployed on site " + site, 409, throwable);
    }
}
