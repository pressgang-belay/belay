package org.jboss.pressgangccms.oauth2.authserver.data.dao;


import org.jboss.pressgangccms.oauth2.authserver.data.model.Identity;
import org.jboss.pressgangccms.oauth2.authserver.data.model.User;

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
public class UserDao {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public boolean isIdentityAssociatedWithUser(Identity identity, User user) {
        return user.getUserIdentities().contains(identity);
    }
}
