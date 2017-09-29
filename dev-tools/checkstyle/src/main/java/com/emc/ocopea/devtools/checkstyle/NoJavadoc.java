// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package com.emc.ocopea.devtools.checkstyle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * Nazgul style guide requires a JavaDoc for all public methods that are longer than 5 lines.
 * However, some methods are trivial and there is no valuable JavaDoc to write.
 * In these cases use @NoJavadoc to avoid build failures.
 * Note: this annotation should be used sparingly, and always with solid reasoning.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface NoJavadoc {
}
