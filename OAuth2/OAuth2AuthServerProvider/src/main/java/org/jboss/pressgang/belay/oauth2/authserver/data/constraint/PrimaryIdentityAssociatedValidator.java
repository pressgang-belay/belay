package org.jboss.pressgang.belay.oauth2.authserver.data.constraint;

import org.jboss.pressgang.belay.oauth2.authserver.data.model.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class PrimaryIdentityAssociatedValidator implements ConstraintValidator<PrimaryIdentityAssociated, User> {

    @Override
    public void initialize(PrimaryIdentityAssociated constraintAnnotation) {
    }

    @Override
    public boolean isValid(User user, ConstraintValidatorContext context) {
        if (user == null
                || noPrimaryIdentityOrAssociatedIdentities(user)
                || primaryIdentityAmongAssociatedIdentities(user)) {
            return true;
        }
        return false;
    }

    private boolean primaryIdentityAmongAssociatedIdentities(User user) {
        return user.getPrimaryIdentity() != null && user.getUserIdentities().contains(user.getPrimaryIdentity());
    }

    private boolean noPrimaryIdentityOrAssociatedIdentities(User user) {
        return user.getPrimaryIdentity() == null && (!identitiesAreAssociated(user));
    }

    private boolean identitiesAreAssociated(User user) {
        return user.getUserIdentities() != null && (!user.getUserIdentities().isEmpty());
    }
}
