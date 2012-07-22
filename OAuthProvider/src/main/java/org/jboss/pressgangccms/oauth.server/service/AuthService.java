package org.jboss.pressgangccms.oauth.server.service;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgangccms.oauth.server.data.dao.*;
import org.jboss.pressgangccms.oauth.server.data.model.auth.*;

import javax.ejb.Stateful;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.logging.Logger;

import static org.jboss.pressgangccms.oauth.server.util.Common.BEARER;

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
    private EndpointRepository endpointRepository;

    @Inject
    private Event<TokenGrant> tokenGrantEventSrc;

    @Inject
    private Event<User> userEventSrc;

    @Inject
    private Event<UserGroup> userGroupEventSrc;

    public Optional<TokenGrant> getTokenGrantByAccessToken(String accessToken) throws OAuthSystemException {
        return tokenGrantRepository.getTokenGrantFromAccessToken(accessToken);
    }

    public Optional<TokenGrant> getTokenGrantByRefreshToken(String refreshToken) throws OAuthSystemException {
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

    public Optional<Endpoint> getEndpointForRequest(HttpServletRequest request) {
        return endpointRepository.findEndpointMatchingRequest(request);
    }

    //TODO move these methods to repositories
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

    public UserGroup createEmptyUserGroup() {
        log.info("Creating new user group");
        UserGroup userGroup = new UserGroup();
        em.persist(userGroup);
        userGroupEventSrc.fire(userGroup);
        return userGroup;
    }

    public void updateUserGroup(UserGroup userGroup) {
        log.info("Updating user group");
        em.merge(userGroup);
        userGroupEventSrc.fire(userGroup);
    }

    public void deleteUserGroup(UserGroup userGroup) {
        log.info("Deleting user group");
        em.remove(em.merge(userGroup));
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

    public static String trimAccessToken(String accessToken) {
        if (accessToken.toLowerCase().startsWith(BEARER)) {
            // Remove leading header
            accessToken = accessToken.substring(BEARER.length()).trim();
        }
        return accessToken;
    }
}
