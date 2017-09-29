// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * Copyright (c) 2014-2016 EMC Corporation All Rights Reserved
 */
package com.emc.microservice.serialization;

import java.io.InputStream;
import java.io.Reader;

/**
 * Created by liebea on 11/19/14.
 * Drink responsibly
 */
public interface SerializationReader<T> {

    T readObject(InputStream inputStream);

    T readObject(Reader reader);
}