package org.jboss.pressgangccms.oauth.server.oauth.request;

import java.security.Principal;

/**
 * Implementation of Principal for use during OAuth filtering.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthPrincipal implements Principal {

    private String userIdentifier;

    public OAuthPrincipal(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    @Override
    public String getName() {
        return userIdentifier;
    }
}
