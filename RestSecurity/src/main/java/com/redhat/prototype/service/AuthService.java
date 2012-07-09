package com.redhat.prototype.service;

import com.redhat.prototype.data.AuthRepository;
import com.redhat.prototype.model.auth.*;

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
    private Event<User> userEventSrc;

    @Inject
    private Event<TokenGrant> tokenGrantEventSrc;

    @Inject
    private AuthRepository authRepository;

    public TokenGrant getTokenGrantByAccessToken(String accessToken) {
        return authRepository.getTokenGrantFromAccessToken(accessToken);
    }

    public TokenGrant getTokenGrantByRefreshToken(String refreshToken) {
        return authRepository.getTokenGrantFromRefreshToken(refreshToken);
    }

    public User getUser(String identifier) {
        return authRepository.getUserFromIdentifier(identifier);
    }

    public Scope getDefaultScope() {
        return authRepository.getDefaultScope();
    }

    public Scope getScopeByName(String name) {
        return authRepository.getScopeFromName(name);
    }

    public ClientApplication getClient(String clientIdentifier) {
        return authRepository.getClientApplicationFromClientIdentifier(clientIdentifier);
    }

    public OpenIdProvider getOpenIdProvider(String providerUrl) {
        return authRepository.getOpenIdProviderFromUrl(providerUrl);
    }

    public void registerUser(User user) {
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
