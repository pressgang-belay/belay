package org.jboss.pressgang.belay.util.test.unit;

import net.sf.ipsedixit.integration.junit.JUnit4IpsedixitTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Base unit test class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(JUnit4IpsedixitTestRunner.class)
public class BaseUnitTest {

    @Before
    public void initialiseMocks() {
        MockitoAnnotations.initMocks(this);
    }

    // Required to avoid initialisation error
    @Test
    public void sanityCheck() {
        assertThat(true, is(true));
    }
}
