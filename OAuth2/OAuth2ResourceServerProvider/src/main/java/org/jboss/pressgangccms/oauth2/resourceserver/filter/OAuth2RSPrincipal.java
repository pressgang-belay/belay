package org.jboss.pressgangccms.oauth2.resourceserver.filter;

import java.lang.Override;import java.lang.String;import java.security.Principal;

/**
 * Implementation of Principal for use during OAuth filtering.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuth2RSPrincipal implements Principal {

    private String userIdentifier;

    public OAuth2RSPrincipal(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    @Override
    public String getName() {
        return userIdentifier;
    }
}
