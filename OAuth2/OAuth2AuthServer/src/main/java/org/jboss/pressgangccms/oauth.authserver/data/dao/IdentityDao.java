package org.jboss.pressgangccms.oauth.authserver.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.common.base.Function;
import org.jboss.pressgangccms.oauth.authserver.data.domain.IdentityInfo;
import org.jboss.pressgangccms.oauth.authserver.data.model.Identity;
import org.jboss.pressgangccms.oauth.authserver.data.model.Identity_;
import org.jboss.pressgangccms.oauth.authserver.data.model.Scope;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static org.jboss.pressgangccms.oauth.authserver.data.domain.IdentityInfo.IdentityInfoBuilder.identityInfoBuilder;

/**
 * Identity DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class IdentityDao {

    @Inject
    private EntityManager em;

    @Inject
    private Logger log;

    public Optional<Identity> getIdentityFromIdentifier(String identifier) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Identity> criteria = cb.createQuery(Identity.class);
        Root<Identity> identity = criteria.from(Identity.class);
        criteria.select(identity).where(cb.equal(identity.get(Identity_.identifier), identifier));
        TypedQuery<Identity> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning Identity with identifier " + identifier);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find Identity with identifier " + identifier);
            return Optional.absent();
        }
    }

    public Optional<IdentityInfo> getUserInfoFromIdentifier(String identifier) {
        Optional<Identity> identityFound = getIdentityFromIdentifier(identifier);
        if (! identityFound.isPresent()) {
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

            // Transform identity scopes to a set of scope names
            Set<String> identityScopes = copyOf(transform(identity.getIdentityScopes(),
                    new Function<Scope, String>() {
                        @Override
                        public String apply(Scope scope) {
                            return scope.getScopeName();
                        }
                    }));
            builder.setIdentityScopes(identityScopes);

            // Transform group users to a set of identity identifiers
            Set<String> userIdentifiers = copyOf(transform(identity.getUser().getUserIdentities(),
                    new Function<Identity, String>() {
                        @Override
                        public String apply(Identity groupUser) {
                            return groupUser.getIdentifier();
                        }
                    }));
            builder.setUserIdentifiers(userIdentifiers);
            log.fine("Returning IdentityInfo with identifier " + identifier);
            return Optional.of(builder.build());
        }
    }
}
