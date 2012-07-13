package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.auth.User;
import com.redhat.prototype.data.model.auth.User_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

@ApplicationScoped
public class UserRepository {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public Optional<User> getUserFromIdentifier(String identifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        criteria.select(user).where(cb.equal(user.get(User_.userIdentifier), identifier));
        TypedQuery<User> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning User with identifier " + identifier);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find User with identifier " + identifier);
            return Optional.absent();
        }
    }
}
