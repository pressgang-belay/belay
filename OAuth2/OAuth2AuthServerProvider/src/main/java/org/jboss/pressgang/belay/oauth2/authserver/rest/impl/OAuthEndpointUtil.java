package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.inject.internal.ImmutableSet;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Sets.filter;
import static java.net.URLDecoder.decode;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_SCOPE;
import static org.apache.commons.lang.StringUtils.join;
import static org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest.OAuthIdRequestParams;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * Encapsulates logic shared across endpoints.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
class OAuthEndpointUtil {

    static TokenGrant createTokenGrantWithDefaults(TokenIssuer tokenIssuer, AuthService authService,
                                                   User user, ClientApplication client, boolean issueRefreshToken)
            throws OAuthSystemException {
        TokenGrant tokenGrant = new TokenGrant();
        tokenGrant.setGrantUser(user);
        tokenGrant.setGrantClient(client);
        tokenGrant.setGrantScopes(newHashSet(authService.getDefaultScope()));
        tokenGrant.setAccessToken(tokenIssuer.accessToken());
        if (issueRefreshToken) {
            tokenGrant.setRefreshToken(tokenIssuer.refreshToken());
        }
        tokenGrant.setAccessTokenExpires(true);
        tokenGrant.setAccessTokenExpiry(oAuthTokenExpiry);
        tokenGrant.setGrantTimeStamp(new Date());
        tokenGrant.setGrantCurrent(true);
        return tokenGrant;
    }

    static CodeGrant createCodeGrantWithDefaults(TokenIssuer tokenIssuer, AuthService authService,
                                                 User user, ClientApplication client)
            throws OAuthSystemException {
        CodeGrant codeGrant = new CodeGrant();
        codeGrant.setGrantUser(user);
        codeGrant.setGrantClient(client);
        codeGrant.setGrantScopes(newHashSet(authService.getDefaultScope()));
        codeGrant.setAuthCode(tokenIssuer.authorizationCode());
        codeGrant.setCodeExpiry(oAuthCodeExpiry);
        codeGrant.setGrantTimeStamp(new Date());
        codeGrant.setGrantCurrent(true);
        return codeGrant;
    }

    /**
     * Checks user scopes contains all requested scopes. Throws OAuthProblemException if invalid scope requested.
     *
     * @param authService     AuthService to use
     * @param requestedScopes Set of scopes requested for token grant
     * @param userScopes      Set of user's scopes
     * @param redirectUri     The URI to redirect to if there is an error
     * @param state           The state parameter value to return if there is an error
     * @return
     * @throws OAuthProblemException if scope requested not found or user does not have that scope
     */
    static Set<Scope> checkScopes(AuthService authService, Set<String> requestedScopes, Set<Scope> userScopes,
                                  String redirectUri, String state)
            throws OAuthProblemException {
        Set<Scope> grantScopes = newHashSet(authService.getDefaultScope());
        if (requestedScopes != null) {
            for (String scopeName : requestedScopes) {
                Optional<Scope> scopeFound = authService.getScopeByName(scopeName);
                if ((!scopeFound.isPresent()) || (!userScopes.contains(scopeFound.get()))) {
                    throw createOAuthProblemException(INVALID_SCOPE, redirectUri, state);
                } else {
                    grantScopes.add(scopeFound.get());
                }
            }
        }
        return grantScopes;
    }

    static void makeExpiringTokenGrantsNonCurrent(AuthService authService, Set<TokenGrant> grants) {
        for (TokenGrant grant : grants) {
            makeExpiringTokenGrantNonCurrent(authService, grant);
        }
    }

    // Will make a grant non-current even if it does not expire
    static void invalidateTokenGrant(AuthService authService, TokenGrant grant) {
        grant.setGrantCurrent(false);
        authService.updateTokenGrant(grant);
    }

    static void makeExpiringTokenGrantNonCurrent(AuthService authService, TokenGrant grant) {
        if (grant.getGrantCurrent() && grant.getAccessTokenExpires()) {
            invalidateTokenGrant(authService, grant);
        }
    }

    static void makeCodeGrantsNonCurrent(AuthService authService, Set<CodeGrant> grants) {
        for (CodeGrant grant : grants) {
            makeCodeGrantNonCurrent(authService, grant);
        }
    }

    static void makeCodeGrantNonCurrent(AuthService authService, CodeGrant grant) {
        if (grant.getGrantCurrent()) {
            grant.setGrantCurrent(false);
            authService.updateCodeGrant(grant);
        }
    }

    static OAuthASResponse.OAuthTokenResponseBuilder addTokenGrantResponseParams(TokenGrant tokenGrant, int status, String state) {
        OAuthASResponse.OAuthTokenResponseBuilder builder = OAuthASResponse
                .tokenResponse(status);
        builder.setAccessToken(tokenGrant.getAccessToken());
        builder.setRefreshToken(tokenGrant.getRefreshToken());
        builder.setExpiresIn(tokenGrant.getAccessTokenExpiry());
        if (!OAuthUtils.isEmpty(state)) {
            builder.setParam(OAuth.OAUTH_STATE, state);
        }
        return builder;
    }

    static OAuthASResponse.OAuthAuthorizationResponseBuilder addCodeGrantResponseParams(CodeGrant codeGrant,
                                                                                        HttpServletRequest request, int status, String state) {
        OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request, status);
        builder.setCode(codeGrant.getAuthCode());
        builder.setExpiresIn(codeGrant.getCodeExpiry());
        if (!OAuthUtils.isEmpty(state)) {
            builder.setParam(OAuth.OAUTH_STATE, state);
        }
        return builder;
    }

    static String getStringAttributeFromSession(HttpServletRequest request, Logger log, String attributeKey,
                                                String attributeName) {
        String attribute = (String) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        return attribute;
    }

    static OAuthIdRequestParams getOAuthIdRequestParamsFromSession(HttpServletRequest request, String sessionKey) {
        return (OAuthIdRequestParams) request.getSession().getAttribute(sessionKey);
    }

    @SuppressWarnings("unchecked")
    static Set<String> getStringSetAttributeFromSession(HttpServletRequest request, Logger log,
                                                        String attributeKey, String attributeName) {
        Set<String> attribute = (Set<String>) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        return attribute;
    }

    static Boolean getBooleanAttributeFromSession(HttpServletRequest request, Logger log,
                                                  String attributeKey, String attributeName) {
        Boolean attribute = (Boolean) request.getSession().getAttribute(attributeKey);
        log.info(attributeName + " is: " + attribute);
        return attribute;
    }

    static OAuthProblemException createOAuthProblemException(String error, String redirectUri, String state) {
        OAuthProblemException e = OAuthProblemException.error(error);
        e.state(state);
        if (redirectUri != null) {
            e.setRedirectUri(redirectUri);
        }
        return e;
    }

    static WebApplicationException createWebApplicationException(String error, Integer status) {
        Response.ResponseBuilder responseBuilder = (status != null)
                ? Response.status(status)
                : Response.status(HttpServletResponse.SC_BAD_REQUEST);
        return new WebApplicationException(responseBuilder.entity(error).build());
    }

    static Response handleOAuthSystemException(Logger log, OAuthSystemException e, String redirectUri, String error) {
        log.severe("OAuthSystemException thrown: " + e.getMessage() + "\n" + join(e.getStackTrace()));
        Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);
        responseBuilder.entity((error != null) ? error : e.getMessage());
        if (!OAuthUtils.isEmpty(redirectUri)) {
            return responseBuilder.location(URI.create(redirectUri)).build();
        } else {
            throw new WebApplicationException(responseBuilder.build());
        }
    }

    static Response handleOAuthProblemException(Logger log, OAuthProblemException e) {
        log.warning("OAuthProblemException thrown: " + e.getMessage() + " " + e.getDescription() + "\n" + join(e.getStackTrace()));
        String redirectUri = e.getRedirectUri();
        if (OAuthUtils.isEmpty(redirectUri)) {
            throw createWebApplicationException(e.getError(), HttpServletResponse.SC_BAD_REQUEST);
        }
        try {
            OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e)
                    .location(redirectUri)
                    .buildQueryMessage();
            return Response.seeOther(URI.create(response.getLocationUri())).build();
        } catch (OAuthSystemException ose) {
            log.warning("OAuthSystemException thrown during OAuth error response creation: " + ose.getMessage());
            return handleOAuthSystemException(log, ose, redirectUri, OAuthError.CodeResponse.SERVER_ERROR);
        }
    }

    static String buildBaseUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
    }

    static Set<TokenGrant> filterTokenGrantsByClient(Set<TokenGrant> grants, final String clientId) {
        return ImmutableSet.copyOf(filter(grants, new Predicate<TokenGrant>() {
            @Override
            public boolean apply(TokenGrant grant) {
                return grant.getGrantClient().getClientIdentifier().equals(clientId);
            }
        }));
    }

    static Set<CodeGrant> filterCodeGrantsByClient(Set<CodeGrant> grants, final String clientId) {
        return ImmutableSet.copyOf(filter(grants, new Predicate<CodeGrant>() {
            @Override
            public boolean apply(CodeGrant grant) {
                return grant.getGrantClient().getClientIdentifier().equals(clientId);
            }
        }));
    }

    static Optional<ClientApproval> getClientApprovalForClientFromSet(Logger log, Set<ClientApproval> clientApprovals, final ClientApplication client) {
        Collection<ClientApproval> matchingApprovals = Collections2.filter(clientApprovals, new Predicate<ClientApproval>() {
            @Override
            public boolean apply(ClientApproval approval) {
                return approval.getClientApplication().getClientIdentifier().equals(client.getClientIdentifier());
            }
        });
        if (matchingApprovals.isEmpty()) {
            log.fine("Could not find Client Approval for client application " + client.getClientName());
            return Optional.absent();
        }
        if (matchingApprovals.size() > 1) {
            log.warning("Found " + matchingApprovals.size() + " user Client Approvals for client application " + client.getClientName());
        }
        log.fine("Found Client Approval for client application " + client.getClientName());
        return Optional.of(matchingApprovals.iterator().next());
    }

    static boolean isClientPublic(ClientApplication client) {
        return isNullOrEmpty(client.getClientSecret());
    }

    static String urlDecodeString(String str, Logger log) {
        try {
            return decode(str, urlEncoding);
        } catch (UnsupportedEncodingException e) {
            log.warning("Decoding failed on string: " + str);
            return str;
        }
    }
}