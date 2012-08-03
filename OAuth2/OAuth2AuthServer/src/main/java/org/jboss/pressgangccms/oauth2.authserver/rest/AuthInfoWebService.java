package org.jboss.pressgangccms.oauth2.authserver.rest;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgangccms.oauth2.authserver.service.AuthService;
import org.jboss.pressgangccms.oauth2.shared.data.model.TokenGrantInfo;
import org.jboss.pressgangccms.oauth2.shared.rest.TokenGrantInfoEndpoint;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import java.util.logging.Logger;

/**
 * Provides authorisation information for resource servers.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Path("/auth/info")
public class AuthInfoWebService implements TokenGrantInfoEndpoint {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @Override
    public TokenGrantInfo getTokenGrantInfoForAccessToken(@HeaderParam("oauth_token") String token) {
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
}
