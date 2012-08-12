package org.jboss.pressgangccms.oauth2.resourceserver.filter;

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.apache.amber.oauth2.rsfilter.OAuthClient;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;
import org.jboss.pressgangccms.oauth2.resourceserver.data.model.OAuth2RSEndpoint;
import org.jboss.pressgangccms.oauth2.resourceserver.service.OAuth2RSAuthService;
import org.jboss.pressgangccms.oauth2.resourceserver.util.Resources;
import org.jboss.pressgangccms.oauth2.shared.data.model.AccessTokenExpiryInfo;
import org.jboss.pressgangccms.oauth2.shared.data.model.TokenGrantInfo;
import org.joda.time.DateTime;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static org.apache.amber.oauth2.common.OAuth.OAUTH_HEADER_NAME;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;

/**
 * Contains the core logic for decisions made by OAuthFilter.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuth2RSDecision implements OAuthDecision {

    private OAuthClient oAuthClient;
    private Principal principal;
    private boolean isAuthorized;
    private OAuth2RSAuthService authService;
    private Logger log = Logger.getLogger(OAuth2RSDecision.class.getName());

    private static final String AUTH_SERVICE_JNDI_ADDRESS = "java:module/OAuth2RSAuthService";

    public OAuth2RSDecision(String realm, String token, HttpServletRequest request, HttpServletResponse response)
            throws OAuthProblemException {
        token = trimAccessToken(token);
        log.info("Processing decision on access token " + token);
        Optional<TokenGrantInfo> tokenGrantInfoFound;
        try {
            authService = (OAuth2RSAuthService) new InitialContext().lookup(AUTH_SERVICE_JNDI_ADDRESS);
            tokenGrantInfoFound = authService.getTokenGrantInfoForAccessToken(token);
        } catch (NamingException e) {
            log.severe("JNDI error with OAuth2RSAuthService: " + e.getMessage());
            throw OAuthProblemException.error(SERVER_ERROR);
        }
        if (tokenGrantInfoFound.isPresent()) {
            log.info("Found match for token " + token);
            TokenGrantInfo tokenGrantInfo = tokenGrantInfoFound.get();
            this.oAuthClient = new OAuth2RSClient(tokenGrantInfo.getGrantClientIdentifier());
            this.principal = new OAuth2RSPrincipal(tokenGrantInfo.getGrantIdentityIdentifier());
            setAuthorisation(tokenGrantInfo, request, response);
        } else {
            log.info("Invalid token " + token);
            this.isAuthorized = false;
            this.oAuthClient = getDefaultClient();
            this.principal = getDefaultPrincipal(request);
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
    }

    private void setAuthorisation(TokenGrantInfo tokenGrantInfo, HttpServletRequest request, HttpServletResponse response)
            throws OAuthProblemException {
        isAuthorized = false;
        checkTokenCurrentAndNotExpired(tokenGrantInfo);
        OAuth2RSEndpoint requestEndpoint = findEndpointForRequest(request);
        if (grantScopeMatchesRequest(tokenGrantInfo, requestEndpoint)) {
            log.info("Verified token " + tokenGrantInfo.getAccessToken());
            isAuthorized = true;
            // TODO add check that non-expiring tokens are being used with particular, approved clients
            // If client has no refresh token and token is within the threshold time of expiring, push out expiry time
            if ((!tokenGrantInfo.getHasRefreshToken())
                    && tokenCloseToExpiring(tokenGrantInfo)
                    && response != null) {
                log.info("Requesting token expiry time be extended");
                Optional<AccessTokenExpiryInfo> newExpiryInfo = authService.extendAccessTokenExpiry(tokenGrantInfo.getAccessToken());
                if (newExpiryInfo.isPresent()) {
                    Map<String, Object> entries = Maps.newHashMap();
                    String accessTimeRemaining = newExpiryInfo.get().getAccessTokenTimeRemaining();
                    entries.put(OAuth.OAUTH_EXPIRES_IN, accessTimeRemaining);
                    log.info("Token will now expire in " + accessTimeRemaining + " seconds");
                    response.setHeader(OAuth.HeaderType.AUTHORIZATION, OAuthUtils.encodeOAuthHeader(entries));
                }
            }
            return;
        }
        log.info("Could not find grant scope matching request");
        throw OAuthProblemException.error(OAuthError.ResourceResponse.INSUFFICIENT_SCOPE);
    }

    private boolean tokenCloseToExpiring(TokenGrantInfo tokenGrantInfo) throws OAuthProblemException {
        Optional<DateTime> expiryDate = getTokenExpiryDate(tokenGrantInfo);
        return (expiryDate.isPresent()
                && expiryDate.get().minusSeconds(Resources.getTokenExpiryExtensionThreshold()).isBeforeNow());
    }

    private void checkTokenCurrentAndNotExpired(TokenGrantInfo tokenGrantInfo) throws OAuthProblemException {
        Optional<DateTime> expiryDate = getTokenExpiryDate(tokenGrantInfo);
        if ((expiryDate.isPresent() && expiryDate.get().isBeforeNow()) || (!tokenGrantInfo.getGrantCurrent())) {
            log.warning("Attempt to use expired or superseded token " + tokenGrantInfo.getAccessToken());
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
    }

    private Optional<DateTime> getTokenExpiryDate(TokenGrantInfo tokenGrantInfo) throws OAuthProblemException {
        int expirySeconds;
        try {
            expirySeconds = Integer.parseInt(tokenGrantInfo.getAccessTokenExpiry());
        } catch (NumberFormatException e) {
            log.warning("NumberFormatException during token check: " + e);
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_TOKEN);
        }
        // If expiry seconds is 0, token does not expire
        if (expirySeconds == 0) {
            return Optional.absent();
        } else {
            return Optional.of(new DateTime(tokenGrantInfo.getGrantTimeStamp()).plusSeconds(Math.abs(expirySeconds)));
        }
    }

    private OAuth2RSEndpoint findEndpointForRequest(HttpServletRequest request) throws OAuthProblemException {
        Optional<OAuth2RSEndpoint> requestEndpointFound = authService.getEndpointForRequest(request);
        if (!requestEndpointFound.isPresent()) {
            log.severe("Could not find endpoint matching " + request.getMethod() + " request for: "
                    + request.getRequestURL());
            throw OAuthProblemException.error(OAuthError.ResourceResponse.INVALID_REQUEST);
        }
        return requestEndpointFound.get();
    }

    private boolean grantScopeMatchesRequest(TokenGrantInfo tokenGrantInfo, OAuth2RSEndpoint requestEndpoint)
            throws OAuthProblemException {
        Set<String> grantScopes = tokenGrantInfo.getGrantScopeNames();
        if (grantScopes.isEmpty()) {
            log.severe("No scopes associated with token grant");
            throw OAuthProblemException.error(SERVER_ERROR);
        }
        for (String scopeName : grantScopes) {
            Set<OAuth2RSEndpoint> scopeEndpoints = authService.getEndpointsForScopeName(scopeName);
            if (scopeEndpoints == null) {
                log.severe("No endpoints associated with scope");
                throw OAuthProblemException.error(SERVER_ERROR);
            }
            for (OAuth2RSEndpoint scopeEndpoint : scopeEndpoints) {
                if (requestEndpoint.equals(scopeEndpoint)) {
                    log.info("Endpoint " + requestEndpoint.getEndpointUrlPattern() + " matches grant scope "
                            + scopeName);
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

    private String trimAccessToken(String accessToken) {
        if (accessToken.toLowerCase().startsWith(OAUTH_HEADER_NAME)) {
            // Remove leading header
            accessToken = accessToken.substring(OAUTH_HEADER_NAME.length()).trim();
        }
        return accessToken;
    }
}
