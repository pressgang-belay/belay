package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.jboss.pressgang.belay.oauth2.authserver.data.model.Util.makeDifferentUser;
import static org.jboss.pressgang.belay.oauth2.authserver.data.model.Util.makeUser;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class CodeGrantTest extends BaseUnitTest {

    @Test
    public void shouldOverrideEqualsCorrectly() throws Exception {
        EqualsVerifier.forClass(CodeGrant.class)
                .withPrefabValues(User.class, makeUser(), makeDifferentUser())
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }
}
