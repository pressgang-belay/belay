package org.jboss.pressgangccms.oauth.server.data.dao;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.common.base.Function;
import org.jboss.pressgangccms.oauth.server.data.domain.UserInfo;
import org.jboss.pressgangccms.oauth.server.data.model.auth.Scope;
import org.jboss.pressgangccms.oauth.server.data.model.auth.User;
import org.jboss.pressgangccms.oauth.server.data.model.auth.User_;

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
import static org.jboss.pressgangccms.oauth.server.data.domain.UserInfo.UserInfoBuilder.userInfoBuilder;

/**
 * User DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
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

    public Optional<UserInfo> getUserInfoFromIdentifier(String identifier) {
        Optional<User> userFound = getUserFromIdentifier(identifier);
        if (! userFound.isPresent()) {
            log.fine("Could not return UserInfo with identifier " + identifier);
            return Optional.absent();
        } else {
            User user = userFound.get();
            UserInfo.UserInfoBuilder builder = userInfoBuilder(user.getUserIdentifier())
                    .setFirstName(user.getFirstName())
                    .setLastName(user.getLastName())
                    .setEmail(user.getEmail())
                    .setCountry(user.getCountry())
                    .setLanguage(user.getLanguage())
                    .setOpenIdProviderUrl(user.getOpenIdProvider().getProviderUrl())
                    .setPrimaryUser(user.getUserGroup().getPrimaryUser().equals(user));

            // Transform user scopes to a set of scope names
            Set<String> userScopes = copyOf(transform(user.getUserScopes(),
                    new Function<Scope, String>() {
                        @Override
                        public String apply(Scope scope) {
                            return scope.getScopeName();
                        }
                    }));
            builder.setUserScopes(userScopes);

            // Transform group users to a set of user identifiers
            Set<String> userGroupIdentifiers = copyOf(transform(user.getUserGroup().getGroupUsers(),
                    new Function<User, String>() {
                        @Override
                        public String apply(User groupUser) {
                            return groupUser.getUserIdentifier();
                        }
                    }));
            builder.setUserGroupIdentifiers(userGroupIdentifiers);
            log.fine("Returning UserInfo with identifier " + identifier);
            return Optional.of(builder.build());
        }
    }
}
