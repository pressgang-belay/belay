package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.junit.Test;

import static org.jboss.pressgang.belay.oauth2.authserver.data.model.Util.makeDifferentIdentity;
import static org.jboss.pressgang.belay.oauth2.authserver.data.model.Util.makeIdentity;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class UserTest extends BaseUnitTest {

    @Test
    public void shouldOverrideEqualsCorrectly() throws Exception {
        EqualsVerifier.forClass(User.class)
                .withPrefabValues(Identity.class, makeIdentity(), makeDifferentIdentity())
                .verify();
    }
}
