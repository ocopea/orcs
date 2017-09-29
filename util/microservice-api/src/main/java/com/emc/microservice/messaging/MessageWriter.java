// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import java.io.OutputStream;

/**
 * Created with love by liebea on 6/22/2014.
 */
public interface MessageWriter {
    void writeMessage(OutputStream outputStream);
}
