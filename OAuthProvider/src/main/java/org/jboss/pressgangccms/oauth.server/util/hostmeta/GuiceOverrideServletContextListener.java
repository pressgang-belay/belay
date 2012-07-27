package org.jboss.pressgangccms.oauth.server.util.hostmeta;

import com.google.code.openid.RelyingPartyFilter;

import java.util.EventListener;

public class GuiceOverrideServletContextListener implements EventListener {
    public GuiceOverrideServletContextListener() {
        RelyingPartyFilter.setConsumerFactory(new GuiceOverrideConsumerFactory());
    }
}
