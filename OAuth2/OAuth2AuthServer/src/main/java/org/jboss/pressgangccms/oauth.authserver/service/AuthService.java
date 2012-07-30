package org.jboss.pressgangccms.oauth.authserver.service;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgangccms.oauth.authserver.data.dao.*;
import org.jboss.pressgangccms.oauth.authserver.data.domain.IdentityInfo;
import org.jboss.pressgangccms.oauth.authserver.data.model.*;

import javax.ejb.Stateful;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;

/**
 * Service class wraps calls to data repositories and persistence logic.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Stateful
public class AuthService {

    @Inject
    private Logger log;

    @PersistenceContext(unitName = "primary", type = PersistenceContextType.EXTENDED)
    private EntityManager em;

    @Inject
    private ClientApplicationDao clientApplicationDao;

    @Inject
    private OpenIdProviderDao openIdProviderDao;

    @Inject
    private ScopeDao scopeDao;

    @Inject
    private TokenGrantDao tokenGrantDao;

    @Inject
    private IdentityDao identityDao;

    @Inject
    private UserDao userDao;

    @Inject
    private EndpointDao endpointDao;

    @Inject
    private Event<TokenGrant> tokenGrantEventSrc;

    @Inject
    private Event<Identity> userEventSrc;

    @Inject
    private Event<User> userGroupEventSrc;

    public Optional<TokenGrant> getTokenGrantByAccessToken(String accessToken) throws OAuthSystemException {
        return tokenGrantDao.getTokenGrantFromAccessToken(accessToken);
    }

    public Optional<TokenGrant> getTokenGrantByRefreshToken(String refreshToken) throws OAuthSystemException {
        return tokenGrantDao.getTokenGrantFromRefreshToken(refreshToken);
    }

    public Optional<Identity> getIdentity(String identifier) {
        return identityDao.getIdentityFromIdentifier(identifier);
    }

    public Optional<IdentityInfo> getUserInfo(String identifier) {
        return identityDao.getUserInfoFromIdentifier(identifier);
    }

    public boolean isIdentityAssociatedWithUser(String identifier, User user) {
        Optional<Identity> identityFound = getIdentity(identifier);
        if ((! identityFound.isPresent()) || user == null) return false;
        return userDao.isIdentityAssociatedWithUser(identityFound.get(), user);
    }

    public Scope getDefaultScope() {
        return scopeDao.getDefaultScope();
    }

    public Optional<Scope> getScopeByName(String name) {
        return scopeDao.getScopeFromName(name);
    }

    public Optional<ClientApplication> getClient(String clientIdentifier) {
        return clientApplicationDao.getClientApplicationFromClientIdentifier(clientIdentifier);
    }

    public Optional<OpenIdProvider> getOpenIdProvider(String providerUrl) {
        return openIdProviderDao.getOpenIdProviderFromUrl(providerUrl);
    }

    public Optional<Endpoint> getEndpointForRequest(HttpServletRequest request) {
        return endpointDao.findEndpointMatchingRequest(request);
    }

    //TODO move these methods to repositories
    public void addIdentity(Identity identity) {
        log.info("Registering " + identity.getIdentifier());
        em.persist(identity);
        userEventSrc.fire(identity);
    }

    public void updateIdentity(Identity identity) {
        log.info("Updating " + identity.getIdentifier());
        em.merge(identity);
        userEventSrc.fire(identity);
    }

    public User createUnassociatedUser() {
        log.info("Creating new user");
        User user = new User();
        em.persist(user);
        userGroupEventSrc.fire(user);
        return user;
    }

    public void updateUser(User user) {
        log.info("Updating user");
        em.merge(user);
        userGroupEventSrc.fire(user);
    }

    public void deleteUser(User user) {
        log.info("Deleting user");
        em.remove(em.merge(user));
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
