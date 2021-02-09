/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark POJO class as Persistance entry
 *
 * @author Yuriy Movchan Date: 10.07.2010
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataEntry {

	/**
     * (Optional) The name of the Persistance attributes
     */
	String[] sortByName() default "";

    /**
     * (Optional) Specify that this entry contains LDAP configuration definition.
     */
    boolean configurationDefinition() default false;

    /**
     * (Optional) Specify sortBy properties to sort by default list of Entries.
     */
    String[] sortBy() default {};

}
