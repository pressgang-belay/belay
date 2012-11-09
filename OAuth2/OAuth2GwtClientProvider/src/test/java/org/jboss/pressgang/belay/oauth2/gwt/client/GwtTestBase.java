package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Base unit test for GWT tests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GwtTestBase extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "org.jboss.pressgang.belay.oauth2.gwt.OAuth2GwtClientProvider";
    }

    // Required to prevent error
    public void testSanity() {
        assert (true);
    }
}
