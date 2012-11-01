package org.jboss.pressgang.belay.oauth2.authserver.data.model;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author katie@codemiller.com (Katie Miller)
 */
public class ClientApplicationTest extends BaseUnitTest {

    @Test
    public void shouldOverrideEqualsCorrectly() throws Exception {
        EqualsVerifier.forClass(ClientApplication.class).verify();
    }
}
