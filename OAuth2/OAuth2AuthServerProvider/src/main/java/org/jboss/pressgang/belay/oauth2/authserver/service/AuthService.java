package org.jboss.pressgang.belay.oauth2.authserver.service;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.data.dao.*;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.shared.data.model.IdentityInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.TokenGrantInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.UserInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Service class wraps calls to DAOs.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Stateless
public class AuthService {

    @Inject
    @AuthServer
    private Logger log;

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
    private ClientApprovalDao clientApprovalDao;

    public Optional<TokenGrant> getTokenGrantByAccessToken(String accessToken) throws OAuthSystemException {
        return tokenGrantDao.getTokenGrantFromAccessToken(accessToken);
    }

    public Optional<TokenGrant> getTokenGrantByRefreshToken(String refreshToken) throws OAuthSystemException {
        return tokenGrantDao.getTokenGrantFromRefreshToken(refreshToken);
    }

    public Optional<TokenGrantInfo> getTokenGrantInfoByAccessToken(String accessToken) throws OAuthSystemException {
        return tokenGrantDao.getTokenGrantInfoFromAccessToken(accessToken);
    }

    public Optional<Identity> getIdentity(String identifier) {
        return identityDao.getIdentityFromIdentifier(identifier);
    }

    public Optional<IdentityInfo> getIdentityInfo(String identifier) {
        return identityDao.getIdentityInfoFromIdentifier(identifier);
    }

    public Optional<UserInfo> getUserInfo(String identifier) {
        Optional<Identity> identityFound = getIdentity(identifier);
        if (! identityFound.isPresent()) {
            log.warning("Could not find UserInfo for identifier " + identifier);
            return Optional.absent();
        }
        return userDao.getUserInfoFromUser(identityFound.get().getUser());
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

    public void addIdentity(Identity identity) {
        identityDao.addIdentity(identity);
    }

    public void updateIdentity(Identity identity) {
        identityDao.updateIdentity(identity);
    }

    public void addUser(User user) {
        userDao.addUser(user);
    }

    public void updateUser(User user) {
        userDao.updateUser(user);
    }

    public void deleteUser(User user) {
        userDao.deleteUser(user);
    }

    public void addGrant(TokenGrant tokenGrant) {
        tokenGrantDao.addTokenGrant(tokenGrant);
    }

    public void updateGrant(TokenGrant tokenGrant) {
        tokenGrantDao.updateTokenGrant(tokenGrant);
    }

    public void addClientApproval(ClientApproval clientApproval) {
        clientApprovalDao.addClientApproval(clientApproval);
    }

    public void updateClientApproval(ClientApproval clientApproval) {
        clientApprovalDao.update(clientApproval);
    }
}
