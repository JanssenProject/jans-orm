package org.xdi.service.security;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * The annotation the is generated by the
 * {@link org.xdi.service.security.SecurityExtension}, which contains all
 * security constraints to be checked.
 *
 * @author Yuriy Movchan Date: 05/22/2017
 */
@InterceptorBinding
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface InterceptSecure {

	@Nonbinding
	Secure[] value();

}
