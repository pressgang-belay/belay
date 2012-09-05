package org.jboss.pressgangccms.oauth2.authserver.data.dao;

import com.google.common.base.Optional;
import org.jboss.pressgangccms.oauth2.authserver.data.model.OpenIdProvider;
import org.jboss.pressgangccms.oauth2.authserver.util.AuthServer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

/**
 * OpenIdProvider DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class OpenIdProviderDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    public Optional<OpenIdProvider> getOpenIdProviderFromUrl(String providerUrl) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OpenIdProvider> criteria = cb.createQuery(OpenIdProvider.class);
        Root<OpenIdProvider> openIdProvider = criteria.from(OpenIdProvider.class);
        criteria.select(openIdProvider).where(cb.equal(openIdProvider.get("providerUrl"), providerUrl));
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
