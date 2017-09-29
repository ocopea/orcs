// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package org.jboss.resteasy.plugins.server.undertow;

import io.undertow.servlet.api.ServletContainer;

/**
 * Created by liebea on 2/1/17.
 * Very much like UndertowJaxrsServer, but better
 */
public class UndertowJaxrsServerBetter extends UndertowJaxrsServer {
    public ServletContainer getServletContainer() {
        return super.container;
    }
}
