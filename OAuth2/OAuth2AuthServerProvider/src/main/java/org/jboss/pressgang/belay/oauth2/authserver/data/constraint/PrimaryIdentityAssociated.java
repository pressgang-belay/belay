package org.jboss.pressgang.belay.oauth2.authserver.data.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@Target({TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PrimaryIdentityAssociatedValidator.class)
public @interface PrimaryIdentityAssociated {

    String message() default "{org.jboss.pressgang.belay.oauth2.authserver.data.constraint.PrimaryIdentityAssociated}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
