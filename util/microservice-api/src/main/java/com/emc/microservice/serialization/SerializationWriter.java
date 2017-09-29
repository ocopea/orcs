// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.serialization;

import java.io.OutputStream;

/**
 * Created by liebea on 11/19/14.
 * Drink responsibly
 */
public interface SerializationWriter<T> {
    void writeObject(T object, OutputStream outputStream);
}
