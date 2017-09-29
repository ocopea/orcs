// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/* $Id: DeserializationHelper.java 87868 2014-07-07 03:20:28Z nivenb $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Abstract helper class to support deserialization of objects that are not available in the Apollo class loader
 *
 * @author nivenb
 */
public abstract class DeserializationHelper {

    /**
     * Implementations should merely call and return objectInputStream.readObject()
     */
    public abstract Object readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException;

}
