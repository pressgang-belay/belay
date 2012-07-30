package org.jboss.pressgangccms.oauth.authserver.openid;

import com.google.code.openid.RelyingPartyFilter;

import java.util.EventListener;

/**
 * Assists in overriding a Guice binding in the openid-filter library.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GuiceOverrideServletContextListener implements EventListener {
    public GuiceOverrideServletContextListener() {
        RelyingPartyFilter.setConsumerFactory(new GuiceOverrideConsumerFactory());
    }
}
