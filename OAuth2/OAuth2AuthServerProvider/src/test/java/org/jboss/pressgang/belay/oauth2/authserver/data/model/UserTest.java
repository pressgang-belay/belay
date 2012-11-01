package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class UserTest extends BaseUnitTest {

    private static Validator validator;

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void shouldViolateConstraintIfIdentityAssociatedButNoPrimaryIdentity() {
        // Given a user with an associated identity but no primary identity
        User user = new User();
        user.setUserIdentities(newHashSet(new Identity()));

        // When the user entity is validated
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);

        // Then a violation should be found
        assertEquals(1, constraintViolations.size());
        assertEquals("{org.jboss.pressgang.belay.oauth2.authserver.data.constraint.PrimaryIdentityAssociated}",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldViolateConstraintIfNoPrimaryIdentityButNoAssociatedIdentities() {
        // Given a user with a primary identity but no associated identities
        User user = new User();
        Identity identity = new Identity();
        user.setPrimaryIdentity(identity);

        // When the user entity is validated
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);

        // Then a violation should be found
        assertEquals(1, constraintViolations.size());
        assertEquals("{org.jboss.pressgang.belay.oauth2.authserver.data.constraint.PrimaryIdentityAssociated}",
                constraintViolations.iterator().next().getMessage());
    }

    @Test
    public void shouldNotViolateConstraintIfIdentityAssociatedAndPrimaryIdentity() {
        // Given a user with an associated identity and a primary identity
        User user = new User();
        Identity identity = new Identity();
        user.setUserIdentities(newHashSet(identity));
        user.setPrimaryIdentity(identity);

        // When the user entity is validated
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);

        // Then no violations should be found
        assertThat(constraintViolations.isEmpty(), is(true));
    }

    @Test
    public void shouldNotViolateConstraintIfNoIdentitiesAssociated() {
        // Given a user with no associated identities or primary identity
        User user = new User();

        // When the user entity is validated
        Set<ConstraintViolation<User>> constraintViolations = validator.validate(user);

        // Then no violations should be found
        assertThat(constraintViolations.isEmpty(), is(true));
    }
}
