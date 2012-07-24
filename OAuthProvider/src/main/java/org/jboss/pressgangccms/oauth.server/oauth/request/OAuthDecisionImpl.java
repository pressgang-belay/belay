package org.jboss.pressgangccms.oauth.server.oauth.request;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.rsfilter.OAuthClient;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;
import org.jboss.pressgangccms.oauth.server.data.model.auth.Endpoint;
import org.jboss.pressgangccms.oauth.server.data.model.auth.Scope;
import org.jboss.pressgangccms.oauth.server.data.model.auth.TokenGrant;
import org.jboss.pressgangccms.oauth.server.service.AuthService;
import org.joda.time.DateTime;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Set;
import java.util.logging.Logger;

import static org.jboss.pressgangccms.oauth.server.rest.auth.OAuthUtil.trimAccessToken;
import static org.jboss.pressgangccms.oauth.server.util.Common.SYSTEM_ERROR;

/**
 * Contains the core logic for decision made by OAuthFilter.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthDecisionImpl implements OAuthDecision {

    private OAuthClient oAuthClient;
    private Principal principal;
    private boolean isAuthorized;
    private AuthService authService;
    private Logger log = Logger.getLogger(OAuthDecisionImpl.class.getName());

    private static final String AUTH_SERVICE_JNDI_ADDRESS = "java:global/OAuthProvider/AuthService";

    public OAuthDecisionImpl(String realm, String token, HttpServletRequest request) throws OAuthProblemException {
        token = trimAccessToken(token);
        log.info("Processing decision on access token " + token);
        Optional<TokenGrant> tokenGrantFound;
        try {
            authService = (AuthService) new InitialContext().lookup(AUTH_SERVICE_JNDI_ADDRESS);
            tokenGrantFound = authService.getTokenGrantByAccessToken(token);
        } catch (NamingException e) {
            log.severe("JNDI error with AuthService: " + e.getMessage());
            throw OAuthProblemException.error(SYSTEM_ERROR);
        } catch (OAuthSystemException e) {
            throw OAuthProblemException.error(SYSTEM_ERROR);
        }
        if (tokenGrantFound.isPresent()) {
            log.info("Found match for token " + token);
            TokenGrant tokenGrant = tokenGrantFound.get();
            this.oAuthClient = new OAuthClientImpl(tokenGrant.getGrantClient().getClientIdentifier());
            this.principal = new OAuthPrincipal(tokenGrant.getGrantIdentity().getIdentifier());
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
        isAuthorized = false;
        checkTokenCurrentAndNotExpired(tokenGrant);
        Optional<Endpoint> requestEndpoint = findEndpointForRequest(request);
        if (grantScopeMatchesRequest(tokenGrant, requestEndpoint)) {
            log.info("Verified token " + tokenGrant.getAccessToken());
            isAuthorized = true;
            return;
        }
        log.info("Could not find grant scope matching request");
        throw OAuthProblemException.error(OAuthError.ResourceResponse.INSUFFICIENT_SCOPE);
    }

    private void checkTokenCurrentAndNotExpired(TokenGrant tokenGrant) throws OAuthProblemException {
        DateTime expiryDate = new DateTime(tokenGrant.getGrantTimeStamp()).
                plusSeconds(Integer.parseInt(tokenGrant.getAccessTokenExpiry()));
        if (expiryDate.isBeforeNow() || (! tokenGrant.getGrantCurrent())) {
            log.warning("Attempt to use expired or superseded token " + tokenGrant.getAccessToken());
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
    }

    private Optional<Endpoint> findEndpointForRequest(HttpServletRequest request) throws OAuthProblemException {
        Optional<Endpoint> requestEndpoint = authService.getEndpointForRequest(request);
        if (! requestEndpoint.isPresent()) {
            log.severe("Could not find endpoint matching " + request.getMethod() + " request for: "
                    + request.getRequestURL());
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_REQUEST);
        }
        return requestEndpoint;
    }

    private boolean grantScopeMatchesRequest(TokenGrant tokenGrant, Optional<Endpoint> requestEndpoint)
            throws OAuthProblemException {
        Set<Scope> grantScopes = tokenGrant.getGrantScopes();
        if (grantScopes == null) {
            log.severe("No scopes associated with token grant; set was null");
            throw OAuthProblemException.error(SYSTEM_ERROR);
        }
        for (Scope scope : grantScopes) {
            Set<Endpoint> scopeEndpoints = scope.getScopeEndpoints();
            if (scopeEndpoints == null) {
                log.severe("No endpoints associated with scope; set was null");
                throw OAuthProblemException.error(SYSTEM_ERROR);
            }
            for (Endpoint scopeEndpoint : scopeEndpoints) {
                if (requestEndpoint.get().equals(scopeEndpoint)) {
                    log.info("Endpoint " + requestEndpoint.get().getEndpointUrlPattern() + " matches grant scope "
                            + scope.getScopeName());
                    return true;
                }
            }
        }
        return false;
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

    // Default implementation for use when access is denied
    private OAuthClient getDefaultClient() {
        return new OAuthClient() {
            @Override
            public String getClientId() {
                return null;
            }
        };
    }

    // Default implementation for use when access is denied
    private Principal getDefaultPrincipal(HttpServletRequest request) {
        return request.getUserPrincipal();
    }
}
