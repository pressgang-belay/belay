package org.jboss.pressgang.belay.oauth2.authserver.data.dao;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.Identity;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.shared.data.model.IdentityInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.logging.Logger;

import static org.jboss.pressgang.belay.oauth2.shared.data.model.IdentityInfo.IdentityInfoBuilder.identityInfoBuilder;

/**
 * Identity DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class IdentityDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private Event<Identity> identityEventSrc;

    public Optional<Identity> getIdentityFromIdentifier(String identifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Identity> criteria = cb.createQuery(Identity.class);
        Root<Identity> identity = criteria.from(Identity.class);
        criteria.select(identity).where(cb.equal(identity.get("identifier"), identifier));
        TypedQuery<Identity> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning Identity with identifier " + identifier);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find Identity with identifier " + identifier);
            return Optional.absent();
        }
    }

    public Optional<IdentityInfo> getIdentityInfoFromIdentifier(String identifier) {
        Optional<Identity> identityFound = getIdentityFromIdentifier(identifier);
        if (!identityFound.isPresent()) {
            log.fine("Could not return IdentityInfo with identifier " + identifier);
            return Optional.absent();
        } else {
            Identity identity = identityFound.get();
            IdentityInfo.IdentityInfoBuilder builder = identityInfoBuilder(identity.getIdentifier())
                    .setFirstName(identity.getFirstName())
                    .setLastName(identity.getLastName())
                    .setFullName(identity.getFullName())
                    .setEmail(identity.getEmail())
                    .setCountry(identity.getCountry())
                    .setLanguage(identity.getLanguage())
                    .setOpenIdProviderUrl(identity.getOpenIdProvider().getProviderUrl())
                    .setPrimaryIdentity(identity.getUser().getPrimaryIdentity().equals(identity));
            log.fine("Returning IdentityInfo with identifier " + identifier);
            return Optional.of(builder.build());
        }
    }

    public void addIdentity(Identity identity) {
        log.info("Registering " + identity.getIdentifier());
        em.persist(identity);
        identityEventSrc.fire(identity);
    }

    public void updateIdentity(Identity identity) {
        log.info("Updating " + identity.getIdentifier());
        em.merge(identity);
        identityEventSrc.fire(identity);
    }
}
