package com.redhat.prototype.oauth.request;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.appengine.repackaged.org.joda.time.DateTime;
import com.redhat.prototype.util.Common;
import com.redhat.prototype.data.model.auth.TokenGrant;
import com.redhat.prototype.service.AuthService;
import com.redhat.prototype.util.Resources;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.rsfilter.OAuthClient;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.logging.Logger;

import static com.redhat.prototype.util.Common.SYSTEM_ERROR;

public class OAuthDecisionImpl implements OAuthDecision {

    private OAuthClient oAuthClient;
    private Principal principal;
    private boolean isAuthorized;
    private Logger log = Logger.getLogger(OAuthDecisionImpl.class.getName());

    private static final String AUTH_SERVICE_JNDI_ADDRESS = "java:global/RestSecurity/AuthService";

    public OAuthDecisionImpl(String realm, String token, HttpServletRequest request) throws OAuthProblemException {
        if (token.toLowerCase().startsWith(Common.BEARER)) {
            // Remove leading header
            token = token.substring(Common.BEARER.length()).trim();
        }
        log.info("Processing decision on access token " + token);
        AuthService authService;
        try {
            authService = (AuthService) new InitialContext().lookup(AUTH_SERVICE_JNDI_ADDRESS);
        } catch (NamingException e) {
            log.severe("JNDI error with AuthService: " + e.getMessage());
            throw OAuthProblemException.error(SYSTEM_ERROR);
        }
        Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByAccessToken(token);
        if (tokenGrantFound.isPresent()) {
            log.info("Found match for token " + token);
            TokenGrant tokenGrant = tokenGrantFound.get();
            this.oAuthClient = new OAuthClientImpl(tokenGrant.getGrantClient());
            this.principal = new UserPrincipal(tokenGrant.getGrantUser());
            setAuthorisation(tokenGrant, request);
        } else {
            log.info("Invalid token " + token);
            this.isAuthorized = false;
            this.oAuthClient = getDefaultClient();
            this.principal = getDefaultPrincipal(request);
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
    }

    private void setAuthorisation(TokenGrant tokenGrant, HttpServletRequest request) throws OAuthProblemException {
        //TODO check scopes here by checking against request URI/method?
        // Throw exception if token has expired
        DateTime expiryDate = new DateTime(tokenGrant.getGrantTimeStamp()).
                plusSeconds(Integer.parseInt(tokenGrant.getAccessTokenExpiry()));
        if (expiryDate.isBeforeNow()) {
            isAuthorized = false;
            log.warning("Attempt to use expired token " + tokenGrant.getAccessToken());
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
        log.info("Verified token " + tokenGrant.getAccessToken());
        isAuthorized = true;
    }

    @Override
    public boolean isAuthorized() {
        return isAuthorized;
    }

    @Override
    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public OAuthClient getOAuthClient() {
        return oAuthClient;
    }

    private OAuthClient getDefaultClient() {
        return new OAuthClient() {
            @Override
            public String getClientId() {
                return null;
            }
        };
    }

    private Principal getDefaultPrincipal(HttpServletRequest request) {
        return request.getUserPrincipal();
    }
}