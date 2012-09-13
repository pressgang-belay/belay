package org.jboss.pressgang.belay.oauth2.authserver.service;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.data.dao.TokenGrantDao;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Class wraps an OAuthIssuer implementation. Its job is to ensure no tokens are generated that
 * happen to match those of a current TokenGrant, to avoid any token collisions where one
 * user is thought to be another. It does not provide an authorisation code service.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Stateless
public class DefaultTokenIssuer implements TokenIssuer {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private TokenGrantDao tokenGrantDao;

    private static OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());

    @Override
    public String accessToken() throws OAuthSystemException {
        String accessToken = oAuthIssuer.accessToken();
        Optional<TokenGrant> grant = tokenGrantDao.getTokenGrantFromAccessToken(accessToken);
        if (grant.isPresent() && grant.get().getGrantCurrent()) {
            log.fine("Access token already in use so generating another one");
            return this.accessToken();
        }
        log.fine("Supplying access token: " + accessToken);
        return accessToken;
    }

    @Override
    public String refreshToken() throws OAuthSystemException {
        String refreshToken = oAuthIssuer.refreshToken();
        Optional<TokenGrant> grant = tokenGrantDao.getTokenGrantFromRefreshToken(refreshToken);
        if (grant.isPresent() && grant.get().getGrantCurrent()) {
            log.fine("Refresh token already in use so generating another one");
            return this.refreshToken();
        }
        log.fine("Supplying refresh token: " + refreshToken);
        return refreshToken;
    }

    @Override
    public String authorizationCode() throws OAuthSystemException {
        return oAuthIssuer.authorizationCode();
    }
}
