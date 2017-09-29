// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.util;
/**
 * Copyright (c) 2002-2016 EMC Corporation All Rights Reserved
 */

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by brainl on 06/11/2015.
 */
public class PairTest {

    @Test
    public void testPairOf() throws Exception {
        final Pair<Integer, String> pair = new Pair(0, "foo");
        assertTrue(pair instanceof Pair<?, ?>);
        assertEquals(new Integer(0), pair.getKey());
        assertEquals("foo", ((Pair<Integer, String>) pair).getValue());
        final Pair<Object, String> pair2 = new Pair(null, "bar");
        assertTrue(pair2 instanceof Pair<?, ?>);
        assertNull(((Pair<Object, String>) pair2).getKey());
        assertEquals("bar", ((Pair<Object, String>) pair2).getValue());
    }

    @Test
    public void testCompatibilityBetweenPairs() throws Exception {
        final Pair<Integer, String> pair1 = new Pair(0, "foo");
        final Pair<Integer, String> pair2 = new Pair(0, "foo");
        assertEquals(pair1, pair2);
        assertEquals(pair1.hashCode(), pair2.hashCode());

        assertTrue(pair1.equals(pair2));
        pair2.setValue("bar");
        assertFalse(pair1.equals(pair2));
        assertFalse(pair1.hashCode() == pair2.hashCode());
    }

    @Test
    public void testObject1() throws Exception {
        final Pair<String, String> pair1 = new Pair("A", "D");
        assertTrue(pair1.getObject1() instanceof String);
        assertTrue("A".equalsIgnoreCase(pair1.getObject1()));
    }

    @Test
    public void testObject2() throws Exception {
        final Pair<String, String> pair1 = new Pair("A", "D");
        assertTrue(pair1.getObject2() instanceof String);
        assertTrue("D".equalsIgnoreCase(pair1.getObject2()));
    }

    @Test
    public void testToString() throws Exception {
        final Pair<String, String> pair = new Pair("Key", "Value");
        assertEquals("Key:Value", pair.toString());
    }

    @Test
    public void testFormattable_simple() throws Exception {
        final Pair<String, String> pair = new Pair("Key", "Value");
        assertEquals("Key:Value", String.format("%1$s", pair));
    }

    @Test
    public void testFormattable_padded() throws Exception {
        final Pair<String, String> pair = new Pair("Key", "Value");
        assertEquals("           Key:Value", String.format("%1$20s", pair));
    }

}
