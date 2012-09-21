package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.AuthInfoEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.shared.data.model.AccessTokenExpiryInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.TokenGrantInfo;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import java.util.logging.Logger;

import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.oAuthTokenExpiry;

/**
 * Provides authorization information for resource servers, which are confidential clients, so the endpoint must
 * be protected by Basic or some other authentication.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AuthInfoEndpointImpl implements AuthInfoEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    /**
     * Provides information for resource servers based on an OAuth2 access token.
     *
     * @param token The access token to query with
     * @return TokenGrantInfo JSON, or null if no info is found
     */
    @Override
    public TokenGrantInfo getTokenGrantInfoForAccessToken(@HeaderParam(OAuth.OAUTH_TOKEN) String token) {
        if (token == null || token.length() == 0) {
            return null;
        }
        try {
            Optional<TokenGrantInfo> tokenGrantInfoFound = authService.getTokenGrantInfoByAccessToken(token);
            if (tokenGrantInfoFound.isPresent()) {
                log.info("Returning TokenGrantInfo for access token");
                return tokenGrantInfoFound.get();
            }
        } catch (OAuthSystemException e) {
            log.warning("OAuthSystemException thrown while obtaining TokenGrantInfo: " + e);
        }
        log.info("Could not get TokenGrantInfo for access token");
        return null;
    }

    /**
     * Endpoint allowing resource servers to request that the expiry time on a token grant be extended. This
     * should only be done for public clients, which don't have access to refresh tokens.
     *
     * @param token The access token from the grant to extend
     * @return AccessTokenExpiryInfo giving information about the new expiry
     */
    @Override
    public AccessTokenExpiryInfo extendAccessTokenExpiry(@HeaderParam(OAuth.OAUTH_TOKEN) String token) {
        if (token == null || token.length() == 0) {
            return null;
        }
        try {
            Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByAccessToken(token);
            if (tokenGrantFound.isPresent() && tokenGrantFound.get().getGrantCurrent()) {
                TokenGrant tokenGrant = tokenGrantFound.get();
                int expirySeconds = Integer.parseInt(tokenGrant.getAccessTokenExpiry());
                int extensionSeconds = Integer.parseInt(oAuthTokenExpiry);
                Period newExpiryPeriod = new Period(new DateTime(tokenGrant.getGrantTimeStamp()),
                        DateTime.now().plusSeconds(extensionSeconds), PeriodType.seconds());
                int extension = newExpiryPeriod.getSeconds() - expirySeconds;
                log.info("Access token expiry has been extended by " + extension + " seconds");
                String newExpiryString = Integer.toString(newExpiryPeriod.getSeconds());
                tokenGrant.setAccessTokenExpiry(newExpiryString);
                authService.updateTokenGrant(tokenGrant);
                return new AccessTokenExpiryInfo(newExpiryString, oAuthTokenExpiry);
            }
        } catch (OAuthSystemException e) {
            log.warning("OAuthSystemException thrown while extending access token expiry: " + e);
        } catch (NumberFormatException e) {
            log.warning("NumberFormatException while extending access token expiry:" + e);
        }
        log.info("Could not extend access token expiry");
        return null;
    }


}
