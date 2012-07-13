package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.auth.ClientApplication;
import com.redhat.prototype.data.model.auth.ClientApplication_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

@ApplicationScoped
public class ClientApplicationRepository {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public Optional<ClientApplication> getClientApplicationFromClientIdentifier(String clientIdentifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientApplication> criteria = cb.createQuery(ClientApplication.class);
        Root<ClientApplication> clientApplication = criteria.from(ClientApplication.class);
        criteria.select(clientApplication).where(cb.equal(clientApplication.get(ClientApplication_.clientIdentifier),
                clientIdentifier));
        TypedQuery<ClientApplication> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning ClientApplication with identifier " + clientIdentifier);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find ClientApplication with identifier " + clientIdentifier);
            return Optional.absent();
        }
    }
}
