package org.jboss.pressgangccms.oauth.server.oauth.request;

import org.jboss.pressgangccms.oauth.server.data.model.auth.User;

import java.security.Principal;

/**
 * Implementation of Principal for use during OAuth filtering.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthPrincipal implements Principal {

    private User user;

    public OAuthPrincipal(User user) {
        this.user = user;
    }

    @Override
    public String getName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    public String getUserId() {
        return user.getUserIdentifier();
    }
}
