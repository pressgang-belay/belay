package org.jboss.pressgangccms.oauth.server.service;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgangccms.oauth.server.data.dao.TokenGrantRepository;
import org.jboss.pressgangccms.oauth.server.data.model.auth.TokenGrant;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.logging.Logger;

/**
 * Service class wraps an OAuthIssuer. Its job is to ensure no tokens are generated that
 * happen to match those of a current TokenGrant, to avoid any token collisions where one
 * user is thought to be another. It does not provide an authorisation code service.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Stateless
public class TokenIssuerService {

    @Inject
    private Logger log;

    @Inject
    private TokenGrantRepository tokenGrantRepository;

    private static OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());

    public String accessToken() throws OAuthSystemException {
        String accessToken = oAuthIssuer.accessToken();
        Optional<TokenGrant> grant = tokenGrantRepository.getTokenGrantFromAccessToken(accessToken);
        if (grant.isPresent() && grant.get().getGrantCurrent()) {
            log.fine("Access token already in use so generating another one");
            return this.accessToken();
        }
        log.fine("Supplying access token: " + accessToken);
        return accessToken;
    }

    public String refreshToken() throws OAuthSystemException {
        String refreshToken = oAuthIssuer.refreshToken();
        Optional<TokenGrant> grant = tokenGrantRepository.getTokenGrantFromRefreshToken(refreshToken);
        if (grant.isPresent() && grant.get().getGrantCurrent()) {
            log.fine("Refresh token already in use so generating another one");
            return this.refreshToken();
        }
        log.fine("Supplying refresh token: " + refreshToken);
        return refreshToken;
    }
}
