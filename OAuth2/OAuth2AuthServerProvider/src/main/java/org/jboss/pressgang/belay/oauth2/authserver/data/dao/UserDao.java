package org.jboss.pressgang.belay.oauth2.authserver.data.dao;


import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.Identity;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.Scope;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.User;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.shared.data.model.UserInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;

/**
 * User DAO.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@ApplicationScoped
public class UserDao {

    @Inject
    @AuthServer
    private EntityManager em;

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private Event<User> userEventSrc;

    public boolean isIdentityAssociatedWithUser(Identity identity, User user) {
        return user.getUserIdentities().contains(identity);
    }

    public void addUser(User user) {
        log.info("Adding user");
        em.persist(user);
        userEventSrc.fire(user);
    }

    public void updateUser(User user) {
        log.info("Updating user");
        em.merge(user);
        userEventSrc.fire(user);
    }

    public void deleteUser(User user) {
        log.info("Deleting user");
        em.remove(em.merge(user));
    }

    public Optional<User> getUserByUsername(String username) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        criteria.select(user).where(cb.equal(user.get("username"), username));
        TypedQuery<User> query = em.createQuery(criteria);
        if (query.getResultList().size() == 1) {
            log.fine("Returning User with username " + username);
            return Optional.of(query.getSingleResult());
        } else {
            log.fine("Could not find User with username " + username);
            return Optional.absent();
        }
    }

    public Optional<UserInfo> getUserInfoFromUser(final User user) {
        if (user == null || (user.getUsername() == null && user.getPrimaryIdentity().getIdentifier() == null)) {
            log.fine("Could not return UserInfo");
            return Optional.absent();
        } else {
            UserInfo.UserInfoBuilder builder = UserInfo.UserInfoBuilder.identityInfoBuilder(user.getUsername());
            if (user.getPrimaryIdentity() != null) {
                builder.setPrimaryIdentifier(user.getPrimaryIdentity().getIdentifier());

                Set<Identity> nonPrimaryIdentities = copyOf(filter(user.getUserIdentities(), new Predicate<Identity>() {
                    @Override
                    public boolean apply(Identity identity) {
                        return !identity.equals(user.getPrimaryIdentity());
                    }
                }));
                Set<String> userIdentifiers = copyOf(transform(user.getUserIdentities(), new Function<Identity, String>() {
                    public String apply(Identity identity) {
                        return identity.getIdentifier();
                    }
                }));
                builder.setUserIdentifiers(userIdentifiers);

                // Amalgamate the different identity values for the following attributes, and filter any nulls
                // Always list the primary identity's value, if any, first
                List<String> firstNames = newArrayList(user.getPrimaryIdentity().getFirstName());
                List<String> lastNames = newArrayList(user.getPrimaryIdentity().getLastName());
                List<String> fullNames = newArrayList(user.getPrimaryIdentity().getFullName());
                List<String> emails = newArrayList(user.getPrimaryIdentity().getEmail());
                List<String> languages = newArrayList(user.getPrimaryIdentity().getLanguage());
                List<String> countries = newArrayList(user.getPrimaryIdentity().getCountry());
                List<String> openIdProviderUrls = newArrayList(user.getPrimaryIdentity().getOpenIdProvider().getProviderUrl());

                for (Identity identity : nonPrimaryIdentities) {
                    firstNames.add(identity.getFirstName());
                    lastNames.add(identity.getLastName());
                    fullNames.add(identity.getFullName());
                    emails.add(identity.getEmail());
                    languages.add(identity.getLanguage());
                    countries.add(identity.getCountry());
                    openIdProviderUrls.add(identity.getOpenIdProvider().getProviderUrl());
                }

                builder.setFirstNames(ImmutableList.copyOf(filter(firstNames, notNull())))
                        .setLastNames(ImmutableList.copyOf(filter(lastNames, notNull())))
                        .setFullNames(ImmutableList.copyOf(filter(fullNames, notNull())))
                        .setEmails(ImmutableList.copyOf(filter(fullNames, notNull())))
                        .setLanguages(ImmutableList.copyOf(filter(languages, notNull())))
                        .setCountries(ImmutableList.copyOf(filter(countries, notNull())))
                        .setOpenIdProviderUrls(ImmutableList.copyOf(filter(openIdProviderUrls, notNull())));
            }

            Set<String> userScopes = copyOf(transform(user.getUserScopes(),
                    new Function<Scope, String>() {
                        @Override
                        public String apply(Scope scope) {
                            return scope.getScopeName();
                        }
                    }));
            builder.setUserScopes(userScopes);

            log.fine("Returning UserInfo for user " + (user.getPrimaryIdentity() != null ? user.getPrimaryIdentity().getIdentifier() : user.getUsername()));
            return Optional.of(builder.build());
        }
    }
}
