// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.microservice.configuration;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by ashish on 04/08/15.
 */
public class PathUtilitiesTest {

    @Test
    public void testPathsWithValidCharacters() {
        assertTrue(PathUtilities.isValid("level1"));
        assertTrue(PathUtilities.isValid("level-1"));
        assertTrue(PathUtilities.isValid("level-1/level-2/level3"));
        assertTrue(PathUtilities.isValid("1/2/a"));
    }

    @Test
    public void testThatPathWithOnlyHyphenIsInvalid() {
        assertFalse(PathUtilities.isValid("level1/-/level3"));
        assertFalse(PathUtilities.isValid("-"));
    }

    @Test
    public void testThatPathWithSpaceIsInvalid() {
        assertFalse(PathUtilities.isValid("level 1"));
    }

    @Test
    public void testThatPathWithNoneAlphaNumericOrHyphenIsInvalid() {
        assertFalse(PathUtilities.isValid("apple_level"));
        assertFalse(PathUtilities.isValid("category%23/apple"));
    }

    @Test
    public void testThatPathStartingWithSeparatorIsValid() {
        assertTrue(PathUtilities.isValid("/apple/ball"));
    }

    @Test
    public void testRootPathIsValid() {
        assertTrue(PathUtilities.isValid("/"));
    }

}
