package org.jboss.pressgang.belay.oauth2.authserver.service;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.shared.data.model.IdentityInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.TokenGrantInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.UserInfo;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface AuthService {

    public Optional<TokenGrant> getTokenGrantByAccessToken(String accessToken) throws OAuthSystemException;

    public Optional<TokenGrant> getTokenGrantByRefreshToken(String refreshToken) throws OAuthSystemException;

    public Optional<TokenGrantInfo> getTokenGrantInfoByAccessToken(String accessToken) throws OAuthSystemException;

    public void addTokenGrant(TokenGrant tokenGrant);

    public Optional<CodeGrant> getCodeGrantByAuthCode(String authCode) throws OAuthSystemException;

    public void updateTokenGrant(TokenGrant tokenGrant);

    public void addCodeGrant(CodeGrant grant);

    public void updateCodeGrant(CodeGrant grant);

    public Optional<Identity> getIdentity(String identifier);

    public Optional<IdentityInfo> getIdentityInfo(String identifier);

    public Optional<UserInfo> getUserInfo(String identifier);

    public boolean isIdentityAssociatedWithUser(String identifier, User user);

    public Scope getDefaultScope();

    public Optional<Scope> getScopeByName(String name);

    public Optional<ClientApplication> getClient(String clientIdentifier);

    public Optional<OpenIdProvider> getOpenIdProvider(String providerUrl);

    public void addIdentity(Identity identity);

    public void updateIdentity(Identity identity);

    public void addUser(User user);

    public void updateUser(User user);

    public void deleteUser(User user);

    public void addClientApproval(ClientApproval clientApproval);

    public void updateClientApproval(ClientApproval clientApproval);
}
