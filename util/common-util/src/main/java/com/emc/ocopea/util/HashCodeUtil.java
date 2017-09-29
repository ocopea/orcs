// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/*
 * $Id: HashCodeUtil.java 88678 2014-07-28 07:41:00Z liebea $
 *
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */
package com.emc.ocopea.util;

import java.lang.reflect.Array;

/**
 * Collected methods which allow easy implementation of <code>hashCode</code>.
 * Example use case:
 * <pre>
 * public int hashCode()
 * {
 *     int result = HashCodeUtil.SEED;
 *     //collect the contributions of various fields
 *     result = HashCodeUtil.hash(result, fPrimitive);
 *     result = HashCodeUtil.hash(result, fObject);
 *     result = HashCodeUtil.hash(result, fArray);
 *     return result;
 * }
 * </pre>
 */
public final class HashCodeUtil {

    /**
     * An initial value for a <code>hashCode</code>, to which is added contributions from fields.
     * Using a non-zero value decreases collisons of <code>hashCode</code> values.
     */
    public static final int SEED = 23;
    // / PRIVATE ///
    private static final int FODD_PRIME_NUMBER = 37;

    public static int hash(int seed, boolean bool) {
        return firstTerm(seed) + (bool ? 1 : 0);
    }

    public static int hash(int seed, char c) {
        return firstTerm(seed) + c;
    }

    public static int hash(int seed, int x) {
        /*
         * Implementation Note Note that byte and short are handled by this method, through implicit conversion.
         */
        return firstTerm(seed) + x;
    }

    public static int hash(int seed, long x) {
        return firstTerm(seed) + (int) (x ^ (x >>> 32));
    }

    public static int hash(int seed, float x) {
        return hash(seed, Float.floatToIntBits(x));
    }

    public static int hash(int seed, double x) {
        return hash(seed, Double.doubleToLongBits(x));
    }

    /**
    * @param object  a possibly-null object field, and possibly an array. If it is an array, then
    *     each element may be a primitive or a possibly-null object.
    */
    public static int hash(int seed, Object object) {
        int result = seed;
        if (object == null) {
            result = hash(result, 0);
        } else if (!isArray(object)) {
            result = hash(result, object.hashCode());
        } else {
            int length = Array.getLength(object);
            for (int idx = 0; idx < length; ++idx) {
                Object item = Array.get(object, idx);
                // recursive call!
                result = hash(result, item);
            }
        }
        return result;
    }

    private static int firstTerm(int seed) {
        return FODD_PRIME_NUMBER * seed;
    }

    private static boolean isArray(Object object) {
        return object.getClass().isArray();
    }
}
