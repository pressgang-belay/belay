package org.jboss.pressgangccms.oauth.server.data.dao;

import org.jboss.pressgangccms.oauth.server.data.model.auth.User;
import org.jboss.pressgangccms.oauth.server.data.model.auth.UserGroup;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.logging.Logger;

/**
 * UserGroup DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class UserGroupRepository {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public boolean isUserInGroup(User user, UserGroup userGroup) {
        return userGroup.getGroupUsers().contains(user);
    }
}
