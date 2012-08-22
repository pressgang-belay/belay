package org.jboss.pressgangccms.oauth2.gwt.client;

import org.jboss.pressgangccms.util.test.unit.gwt.BaseGwtUnitTest;

/**
 * Base unit test for GWT tests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class BaseGwtTest extends BaseGwtUnitTest {
    @Override
    public String getModuleName() {
        return "org.jboss.pressgangccms.oauth2.gwt.OAuth2GwtClientProvider";
    }
}
