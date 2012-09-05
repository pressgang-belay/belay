package org.jboss.pressgangccms.oauth2.authserver.data.dao;


import org.jboss.pressgangccms.oauth2.authserver.data.model.Identity;
import org.jboss.pressgangccms.oauth2.authserver.data.model.User;
import org.jboss.pressgangccms.oauth2.authserver.util.AuthServer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
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
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private Event<User> userEventSrc;

    public boolean isIdentityAssociatedWithUser(Identity identity, User user) {
        return user.getUserIdentities().contains(identity);
    }

    public User createNewUser() {
        log.info("Creating new user");
        User user = new User();
        em.persist(user);
        userEventSrc.fire(user);
        return user;
    }

    public void updateUser(User user) {
        log.info("Updating user");
        em.merge(user);
        userEventSrc.fire(user);
    }

    public void deleteUser(User user) {
        log.info("Deleting user");
        em.remove(em.merge(user));
    }
}
