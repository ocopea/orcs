// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.output;

/**
 * Created by liebea on 10/4/2014. Enjoy it
 */
public class MessagingOutputDescriptor extends OutputDescriptor {

    public MessagingOutputDescriptor(Class format, String description) {
        super(MicroServiceOutputType.messaging, format, description);
    }

}
