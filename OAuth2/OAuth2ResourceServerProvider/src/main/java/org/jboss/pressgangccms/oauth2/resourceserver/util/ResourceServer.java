package org.jboss.pressgangccms.oauth2.resourceserver.util;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Interface to differentiate ResourceServerProvider implementations
 * of EntityManager and Logger.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Qualifier
@Retention(RUNTIME)
@Target({FIELD, METHOD})
public @interface ResourceServer {
}
