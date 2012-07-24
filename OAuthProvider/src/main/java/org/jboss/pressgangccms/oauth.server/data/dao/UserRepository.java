package org.jboss.pressgangccms.oauth.server.data.dao;

import org.jboss.pressgangccms.oauth.server.data.model.auth.Identity;
import org.jboss.pressgangccms.oauth.server.data.model.auth.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

/**
 * User DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class UserRepository {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public boolean isIdentityAssociatedWithUser(Identity identity, User user) {
        return user.getUserIdentities().contains(identity);
    }
}
