package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.junit.Test;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OpenIdProviderTest extends BaseUnitTest {

    @Test
    public void shouldOverrideEqualsCorrectly() throws Exception {
        EqualsVerifier.forClass(OpenIdProvider.class).verify();
    }
}
