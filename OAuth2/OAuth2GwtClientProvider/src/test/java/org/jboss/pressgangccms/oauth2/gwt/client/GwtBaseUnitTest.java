package org.jboss.pressgangccms.oauth2.gwt.client;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Base unit test for GWT tests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GwtBaseUnitTest extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "org.jboss.pressgangccms.oauth2.gwt.OAuth2GwtClientProvider";
    }
}