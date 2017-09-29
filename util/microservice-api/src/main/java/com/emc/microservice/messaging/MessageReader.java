// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.messaging;

import java.io.InputStream;

/**
 * Created by liebea on 12/29/14.
 * Drink responsibly
 */
public interface MessageReader {
    void read(InputStream in);
}
