// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * $Id: Pair.java 67521 2012-12-14 18:11:37Z patele $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util;

import java.io.Serializable;

public class Pair<K, V> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected K key;
    protected V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public K getObject1() {
        return key;
    }

    public V getObject2() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Pair other = (Pair) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(key);
        builder.append(':');
        builder.append(value);
        return builder.toString();
    }
}
