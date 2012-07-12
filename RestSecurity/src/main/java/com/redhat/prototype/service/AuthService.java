package com.redhat.prototype.service;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.dao.*;
import com.redhat.prototype.data.model.auth.*;

import javax.ejb.Stateful;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.logging.Logger;

@Stateful
public class AuthService {

    @Inject
    private Logger log;

    @PersistenceContext(unitName = "primary", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Inject
    private ClientApplicationRepository clientApplicationRepository;

    @Inject
    private OpenIdProviderRepository openIdProviderRepository;

    @Inject
    private ScopeRepository scopeRepository;

    @Inject
    private TokenGrantRepository tokenGrantRepository;

    @Inject
    private UserRepository userRepository;

    @Inject
    private Event<TokenGrant> tokenGrantEventSrc;

    @Inject
    private Event<User> userEventSrc;

    public Optional<TokenGrant> getTokenGrantByAccessToken(String accessToken) {
        return tokenGrantRepository.getTokenGrantFromAccessToken(accessToken);
    }

    public Optional<TokenGrant> getTokenGrantByRefreshToken(String refreshToken) {
        return tokenGrantRepository.getTokenGrantFromRefreshToken(refreshToken);
    }

    public Optional<User> getUser(String identifier) {
        return userRepository.getUserFromIdentifier(identifier);
    }

    public Scope getDefaultScope() {
        return scopeRepository.getDefaultScope();
    }

    public Optional<Scope> getScopeByName(String name) {
        return scopeRepository.getScopeFromName(name);
    }

    public Optional<ClientApplication> getClient(String clientIdentifier) {
        return clientApplicationRepository.getClientApplicationFromClientIdentifier(clientIdentifier);
    }

    public Optional<OpenIdProvider> getOpenIdProvider(String providerUrl) {
        return openIdProviderRepository.getOpenIdProviderFromUrl(providerUrl);
    }

    public void addUser(User user) {
        log.info("Registering " + user.getUserIdentifier());
        em.persist(user);
        userEventSrc.fire(user);
    }

    public void updateUser(User user) {
        log.info("Updating " + user.getUserIdentifier());
        em.merge(user);
        userEventSrc.fire(user);
    }

    public void addGrant(TokenGrant tokenGrant) {
        log.info("Adding token grant");
        em.persist(tokenGrant);
        tokenGrantEventSrc.fire(tokenGrant);
    }

    public void updateGrant(TokenGrant tokenGrant) {
        log.info("Updating token grant");
        em.merge(tokenGrant);
        tokenGrantEventSrc.fire(tokenGrant);
    }
}
