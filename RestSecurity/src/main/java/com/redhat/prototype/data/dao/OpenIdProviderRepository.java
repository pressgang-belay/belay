package com.redhat.prototype.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.auth.OpenIdProvider;
import com.redhat.prototype.data.model.auth.OpenIdProvider_;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

@ApplicationScoped
public class OpenIdProviderRepository {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public Optional<OpenIdProvider> getOpenIdProviderFromUrl(String providerUrl) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OpenIdProvider> criteria = cb.createQuery(OpenIdProvider.class);
        Root<OpenIdProvider> openIdProvider = criteria.from(OpenIdProvider.class);
        criteria.select(openIdProvider).where(cb.equal(openIdProvider.get(OpenIdProvider_.providerUrl), providerUrl));
        TypedQuery<OpenIdProvider> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning OpenIdProvider with URL " + providerUrl);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find OpenIdProvider with URL " + providerUrl);
            return Optional.absent();
        }
    }
}
