package org.jboss.pressgangccms.oauth2.authserver.rest;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgangccms.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgangccms.oauth2.authserver.service.AuthService;
import org.jboss.pressgangccms.oauth2.shared.data.model.AccessTokenExpiryInfo;
import org.jboss.pressgangccms.oauth2.shared.data.model.TokenGrantInfo;
import org.jboss.pressgangccms.oauth2.shared.rest.TokenExpiryExtensionEndpoint;
import org.jboss.pressgangccms.oauth2.shared.rest.TokenGrantInfoEndpoint;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import java.util.logging.Logger;

import static org.jboss.pressgangccms.oauth2.authserver.util.Common.OAUTH_TOKEN_EXPIRY;

/**
 * Provides authorisation information for resource servers, which are confidential clients.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Path("/auth/info")
public class AuthInfoWebService implements TokenGrantInfoEndpoint, TokenExpiryExtensionEndpoint {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @Override
    public TokenGrantInfo getTokenGrantInfoForAccessToken(@HeaderParam(OAuth.OAUTH_TOKEN) String token) {
        if (token == null || token.length() == 0) {
            return null;
        }
        try {
            Optional<TokenGrantInfo> tokenGrantInfoFound = authService.getTokenGrantInfoForAccessToken(token);
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
                int extensionSeconds = Integer.parseInt(OAUTH_TOKEN_EXPIRY);
                Period newExpiryPeriod = new Period(new DateTime(tokenGrant.getGrantTimeStamp()),
                        DateTime.now().plusSeconds(extensionSeconds), PeriodType.seconds());
                int extension = newExpiryPeriod.getSeconds() - expirySeconds;
                log.info("Access token expiry has been extended by " + extension + " seconds");
                String newExpiryString = Integer.toString(newExpiryPeriod.getSeconds());
                tokenGrant.setAccessTokenExpiry(newExpiryString);
                authService.updateGrant(tokenGrant);
                return new AccessTokenExpiryInfo(newExpiryString, OAUTH_TOKEN_EXPIRY);
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
