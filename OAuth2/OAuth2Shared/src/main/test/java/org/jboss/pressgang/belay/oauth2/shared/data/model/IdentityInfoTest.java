package org.jboss.pressgang.belay.oauth2.shared.data.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.junit.Test;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class IdentityInfoTest extends BaseUnitTest {

    @Test
    public void shouldOverrideEqualsCorrectly() throws Exception {
        EqualsVerifier.forClass(IdentityInfo.class)
                .suppress(Warning.NULL_FIELDS, Warning.NONFINAL_FIELDS)
                .verify();
    }
}
