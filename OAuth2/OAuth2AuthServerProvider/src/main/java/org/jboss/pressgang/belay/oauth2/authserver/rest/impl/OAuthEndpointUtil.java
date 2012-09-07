package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.ClientApplication;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.Identity;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.Scope;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_HEADER_NAME;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_SCOPE;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.oAuthTokenExpiry;

/**
 * Encapsulates logic shared across auth web services.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
class OAuthEndpointUtil {

    static TokenGrant createTokenGrantWithDefaults(TokenIssuerService tokenIssuerService, AuthService authService,
                                                   Identity identity, ClientApplication client, boolean issueRefreshToken)
                                                        throws OAuthSystemException {
        TokenGrant tokenGrant = new TokenGrant();
        tokenGrant.setGrantIdentity(identity);
        tokenGrant.setGrantClient(client);
        tokenGrant.setGrantScopes(newHashSet(authService.getDefaultScope()));
        tokenGrant.setAccessToken(tokenIssuerService.accessToken());
        if (issueRefreshToken) {
            tokenGrant.setRefreshToken(tokenIssuerService.refreshToken());
        }
        tokenGrant.setAccessTokenExpiry(oAuthTokenExpiry);
        tokenGrant.setGrantTimeStamp(new Date());
        tokenGrant.setGrantCurrent(true);
        return tokenGrant;
    }

    /**
     * Checks user scopes contains all requested scopes. Throws OAuthProblemException if invalid scope requested.
     *
     * @param authService     AuthService to use
     * @param requestedScopes Set of scopes requested for token grant
     * @param userScopes      Set of user's scopes
     * @return
     * @throws OAuthProblemException if scope requested not found or user does not have that scope
     */
    static Set<Scope> checkScopes(AuthService authService, Set<String> requestedScopes, Set<Scope> userScopes,
                                  String redirectUri)
            throws OAuthProblemException {
        Set<Scope> grantScopes = newHashSet(authService.getDefaultScope());
        for (String scopeName : requestedScopes) {
            Optional<Scope> scopeFound = authService.getScopeByName(scopeName);
            if ((!scopeFound.isPresent()) || (!userScopes.contains(scopeFound.get()))) {
                throw createOAuthProblemException(INVALID_SCOPE, redirectUri);
            } else {
                grantScopes.add(scopeFound.get());
            }
        }
        return grantScopes;
    }

    static void makeGrantsNonCurrent(AuthService authService, Set<TokenGrant> grants) {
        for (TokenGrant grant : grants) {
            makeGrantNonCurrent(authService, grant);
        }
    }

    static void makeGrantNonCurrent(AuthService authService, TokenGrant grant) {
        // Make current grant non-current, as long as its expiry time is not set to 0 (used for non-expiring tokens)
        if (grant.getGrantCurrent() && (! grant.getAccessTokenExpiry().equals("0"))) {
            grant.setGrantCurrent(false);
            authService.updateGrant(grant);
        }
    }

    static OAuthASResponse.OAuthTokenResponseBuilder addTokenGrantResponseParams(TokenGrant tokenGrant, int status) {
        OAuthASResponse.OAuthTokenResponseBuilder builder = OAuthASResponse
                .tokenResponse(status);
        builder.setAccessToken(tokenGrant.getAccessToken());
        builder.setRefreshToken(tokenGrant.getRefreshToken());
        builder.setExpiresIn(tokenGrant.getAccessTokenExpiry());
        return builder;
    }

    static String getStringAttributeFromSessionAndRemove(HttpServletRequest request, Logger log, String attributeKey,
                                                         String attributeName) {
        String attribute = (String) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        request.getSession().removeAttribute(attributeKey);
        return attribute;
    }

    @SuppressWarnings("unchecked")
    static Set<String> getStringSetAttributeFromSessionAndRemove(HttpServletRequest request, Logger log,
                                                                 String attributeKey, String attributeName) {
        Set<String> attribute = (Set<String>) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        request.getSession().removeAttribute(attributeKey);
        return attribute;
    }

    static Boolean getBooleanAttributeFromSessionAndRemove(HttpServletRequest request, Logger log,
                                                                 String attributeKey, String attributeName) {
        Boolean attribute = (Boolean) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        request.getSession().removeAttribute(attributeKey);
        return attribute;
    }

    static OAuthProblemException createOAuthProblemException(String error, String redirectUri) {
        OAuthProblemException e = OAuthProblemException.error(error);
        if (redirectUri != null) {
            e.setRedirectUri(redirectUri);
        }
        return e;
    }

    static WebApplicationException createWebApplicationException(String error, Integer status) {
        Response.ResponseBuilder responseBuilder = (status != null)
                ? Response.status(status)
                : Response.status(HttpServletResponse.SC_NOT_FOUND);
        return new WebApplicationException(responseBuilder.entity(error).build());
    }

    static Response handleOAuthSystemException(Logger log, OAuthSystemException e, String redirectUri, String error) {
        log.severe("OAuthSystemException thrown: " + e.getMessage());
        Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);
        responseBuilder.entity((error != null) ? error : e.getMessage());
        if (! OAuthUtils.isEmpty(redirectUri)) {
            return responseBuilder.location(URI.create(redirectUri)).build();
        } else {
            throw new WebApplicationException(responseBuilder.build());
        }
    }

    static Response handleOAuthProblemException(Logger log, OAuthProblemException e) {
        log.warning("OAuthProblemException thrown: " + e.getMessage() + " " + e.getDescription());
        final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);
        String redirectUri = e.getRedirectUri();
        if (OAuthUtils.isEmpty(redirectUri)) {
            throw createWebApplicationException(e.getError(), HttpServletResponse.SC_NOT_FOUND);
        }
        return responseBuilder.entity(e.getError()).location(URI.create(redirectUri)).build();
    }

    static String trimAccessToken(String accessToken) {
        if (accessToken.toLowerCase().startsWith(OAUTH_HEADER_NAME)) {
            // Remove leading header
            accessToken = accessToken.substring(OAUTH_HEADER_NAME.length()).trim();
        }
        return accessToken;
    }

    static String buildBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }
}
