package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.auth.Scope;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@ApplicationScoped
public class ScopeRepository {

    @Inject
    private EntityManager em;

    private static final String DEFAULT_SCOPE_NAME = "default";

    public Scope getDefaultScope() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Scope> criteria = cb.createQuery(Scope.class);
        Root<Scope> scope = criteria.from(Scope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), DEFAULT_SCOPE_NAME));
        return em.createQuery(criteria).getSingleResult();
    }

    public Optional<Scope> getScopeFromName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Scope> criteria = cb.createQuery(Scope.class);
        Root<Scope> scope = criteria.from(Scope.class);
        criteria.select(scope).where(cb.equal(scope.get("scopeName"), name));
        TypedQuery<Scope> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return Optional.of(query.getSingleResult());
        } else {
            return Optional.absent();
        }
    }
}
