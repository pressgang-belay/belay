package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.auth.ClientApplication;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@ApplicationScoped
public class ClientApplicationRepository {

    @Inject
    private EntityManager em;

    public Optional<ClientApplication> getClientApplicationFromClientIdentifier(String clientIdentifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientApplication> criteria = cb.createQuery(ClientApplication.class);
        Root<ClientApplication> clientApplication = criteria.from(ClientApplication.class);
        criteria.select(clientApplication).where(cb.equal(clientApplication.get("clientIdentifier"), clientIdentifier));
        TypedQuery<ClientApplication> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            return Optional.of(query.getSingleResult());
        } else {
            return Optional.absent();
        }
    }
}
