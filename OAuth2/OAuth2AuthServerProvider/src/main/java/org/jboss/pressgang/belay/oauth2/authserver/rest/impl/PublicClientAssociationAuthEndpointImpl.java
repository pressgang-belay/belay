package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableSet;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ParameterStyle;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.PublicClientAssociationAuthEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_REDIRECT_URI;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_TOKEN;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class PublicClientAssociationAuthEndpointImpl implements PublicClientAssociationAuthEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuer tokenIssuer;

    /**
     * This endpoint allows client applications to associate a second identity with the currently
     * authenticated user. The user will need to log in to the second identity to authenticate.
     * If this second identity belongs to a user that has multiple associated identities already,
     * all the identities will end up associated with the one user.
     * <p/>
     * Set newIsPrimary to true if the second identity should become the user's primary identity.
     * In this case, identity scopes should be requested or the new token info returned will have the
     * default scope only. If this is false, whichever identity is currently the authenticated user's
     * primary identity will remain the primary identity of the resulting user.
     * <p/>
     * The caller must include all the parameters required for an OAuthIdRequest login request, that is,
     * provider, redirect_uri, client_id and response_type, which must be 'token'.
     *
     * @param request      The servlet request
     * @param newIsPrimary True if the new identity being associated should be the primary identity, default false
     * @return OAuth response containing access token, refresh token and expiry parameters, or an error
     * @throws javax.ws.rs.WebApplicationException if OAuth redirect URI is not provided
     */
    @Override
    public Response associateIdentity(@Context HttpServletRequest request,
                                      @QueryParam(NEW_IDENTITY_PRIMARY) Boolean newIsPrimary) throws URISyntaxException {
        log.info("Processing identity association request");

        Principal userPrincipal = request.getUserPrincipal();
        OAuthIdRequest oAuthRequest;
        String oAuthRedirectUri = null;
        String accessToken;

        try {
            oAuthRequest = new OAuthIdRequest(request);
            oAuthRedirectUri = oAuthRequest.getRedirectURI();
            accessToken = getTokenGrantFromAccessToken(request, oAuthRedirectUri).getAccessToken();
            log.info("First identifier is: " + userPrincipal.getName());
            checkClient(oAuthRedirectUri, oAuthRequest.getClientId());
            // Record initial request details
            request.getSession().setAttribute(FIRST_IDENTIFIER, userPrincipal.getName());
            request.getSession().setAttribute(NEW_IDENTITY_PRIMARY, newIsPrimary == null ? false : newIsPrimary);
            request.getSession().setAttribute(STORED_OAUTH_REDIRECT_URI, oAuthRedirectUri);
            request.getSession().setAttribute(STORED_OAUTH_CLIENT_ID, oAuthRequest.getClientId());
            request.getSession().setAttribute(OAUTH_TOKEN, accessToken);
            if (oAuthRequest.getScopes() != null) {
                request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oAuthRequest.getScopes());
            }
            log.info("Redirecting to authorise second identifier");
            return Response.temporaryRedirect(URI.create(createNewRedirectUri(oAuthRequest.getParam(OPENID_PROVIDER)))).build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, oAuthRedirectUri, SERVER_ERROR);
        }
    }

    @Override
    public Response completeAssociation(@Context HttpServletRequest request) throws OAuthProblemException,
            OAuthSystemException {
        log.info("Processing identity association request completion");

        String secondId = OAuthEndpointUtil.getStringAttributeFromSession(request, log, OPENID_IDENTIFIER, "Second identifier");
        String oAuthRedirectUri = OAuthEndpointUtil.getStringAttributeFromSession(request, log, STORED_OAUTH_REDIRECT_URI,
                "Stored OAuth redirect URI");
        TokenGrant requestTokenGrant = getTokenGrantFromAccessToken(request, oAuthRedirectUri);
        String firstId = OAuthEndpointUtil.getStringAttributeFromSession(request, log, FIRST_IDENTIFIER,
                "First identifier");
        Boolean secondIdentityIsPrimary = OAuthEndpointUtil.getBooleanAttributeFromSession(request, log, NEW_IDENTITY_PRIMARY,
                "Second identity is primary flag");
        String clientId = OAuthEndpointUtil.getStringAttributeFromSession(request, log, STORED_OAUTH_CLIENT_ID,
                "OAuth client id");
        Set<String> scopesRequested = OAuthEndpointUtil.getStringSetAttributeFromSession(request, log,
                OAuth.OAUTH_SCOPE, "Scopes requested");
        request.getSession().invalidate();

        if (firstId == null || secondId == null || secondIdentityIsPrimary == null || oAuthRedirectUri == null
                || clientId == null){
            log.severe("Identity association session attribute null or invalid");
            throw new OAuthSystemException("Null session attribute");
        }
        Optional<ClientApplication> clientFound = checkClient(oAuthRedirectUri, clientId);

        // Get users for identifiers
        Optional<Identity> firstIdentityFound = authService.getIdentity(firstId);
        Optional<Identity> secondIdentityFound = authService.getIdentity(secondId);
        if ((!firstIdentityFound.isPresent()) || (!secondIdentityFound.isPresent())) {
            throw new OAuthSystemException("Could not find both identities to associate");
        }
        User firstUser = firstIdentityFound.get().getUser();
        User secondUser = secondIdentityFound.get().getUser();

        // Check identities not already associated
        if (firstUser.equals(secondUser)) {
            throw OAuthEndpointUtil.createOAuthProblemException(IDENTITIES_ASSOCIATED_ERROR, oAuthRedirectUri);
        }

        User finalUser = mergeUsers(secondIdentityIsPrimary, secondIdentityFound.get(), firstUser, secondUser);
        // Use scopes from previous TokenGrant if first identity was the primary, or the requested scopes if not
        Identity primaryIdentity = finalUser.getPrimaryIdentity();
        Set<Scope> grantScopes = getGrantScopes(requestTokenGrant, secondIdentityIsPrimary, scopesRequested,
                secondIdentityFound.get(), oAuthRedirectUri);
        Response response = createTokenGrantResponseForUser(oAuthRedirectUri, clientFound.get(), grantScopes,
                finalUser, (requestTokenGrant.getRefreshToken() != null));

        if (response.getStatus() == HttpServletResponse.SC_FOUND) {
            // Make original TokenGrant non-current
            makeGrantNonCurrent(authService, requestTokenGrant);
        }
        log.info("Sending token response to " + oAuthRedirectUri);
        return response;
    }

    private Optional<ClientApplication> checkClient(String oAuthRedirectUri, String clientId) throws OAuthProblemException {
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        if (!clientFound.isPresent()) {
            throw OAuthEndpointUtil.createOAuthProblemException(INVALID_CLIENT, oAuthRedirectUri);
        }
        return clientFound;
    }


    private Set<Scope> getGrantScopes(TokenGrant requestTokenGrant, Boolean secondIdentityIsPrimary,
                                      Set<String> scopesRequested, Identity secondIdentity,
                                      String redirectUri) throws OAuthProblemException {
        if (secondIdentityIsPrimary) {
            if (scopesRequested != null) {
                return checkScopes(authService, scopesRequested, secondIdentity.getUser().getUserScopes(), redirectUri);
            } else {
                log.warning("Identity has default scope after association request as no scopes requested");
                return null;
            }
        } else {
            return newHashSet(requestTokenGrant.getGrantScopes());
        }
    }

    private User mergeUsers(Boolean secondIdentityIsPrimary, Identity secondIdentity, User firstUser, User secondUser) {
        if (secondIdentityIsPrimary) {
            secondUser.setPrimaryIdentity(secondIdentity);
            return mergeUserAttributes(firstUser, secondUser);
        } else {
            return mergeUserAttributes(secondUser, firstUser);
        }
    }

    private User mergeUserAttributes(final User oldUser, final User newUser) {
        if (isNullOrEmpty(newUser.getUsername()) && (! isNullOrEmpty(oldUser.getUsername()))) {
            newUser.setUsername(oldUser.getUsername());
        }
        newUser.getUserIdentities().addAll(updateIdentities(oldUser, newUser));
        newUser.getTokenGrants().addAll(updateOldTokenGrants(oldUser, newUser));
        newUser.getUserScopes().addAll(newHashSet(oldUser.getUserScopes()));
        newUser.getClientApprovals().addAll(updateOldClientApprovals(oldUser, newUser));

        authService.updateUser(newUser);
        authService.deleteUser(oldUser);
        return newUser;
    }

    private ImmutableSet<Identity> updateIdentities(User oldUser, final User newUser) {
        return copyOf(transform(oldUser.getUserIdentities(), new Function<Identity, Identity>() {
            @Override
            public Identity apply(Identity identity) {
                identity.setUser(newUser);
                authService.updateIdentity(identity);
                return identity;
            }
        }));
    }

    private ImmutableSet<ClientApproval> updateOldClientApprovals(User oldUser, final User newUser) {
        return copyOf(transform(oldUser.getClientApprovals(), new Function<ClientApproval, ClientApproval>() {
            @Override
            public ClientApproval apply(ClientApproval clientApproval) {
                clientApproval.setApprover(newUser);
                authService.updateClientApproval(clientApproval);
                return clientApproval;
            }
        }));
    }

    private ImmutableSet<TokenGrant> updateOldTokenGrants(User oldUser, final User newUser) {
        return copyOf(transform(oldUser.getTokenGrants(), new Function<TokenGrant, TokenGrant>() {
            @Override
            public TokenGrant apply(TokenGrant tokenGrant) {
                tokenGrant.setGrantUser(newUser);
                authService.updateGrant(tokenGrant);
                return tokenGrant;
            }
        }));
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private String createNewRedirectUri(String provider) {
        return new StringBuilder(authEndpoint).append(QUERY_STRING_MARKER)
                .append(OPENID_PROVIDER).append(KEY_VALUE_SEPARATOR)
                .append(provider).append(PARAMETER_SEPARATOR)
                .append(OAuth.OAUTH_CLIENT_ID).append(KEY_VALUE_SEPARATOR)
                .append(authServerOAuthClientId).append(PARAMETER_SEPARATOR)
                .append(OAUTH_REDIRECT_URI).append(KEY_VALUE_SEPARATOR)
                .append(completeAssociationEndpoint).append(PARAMETER_SEPARATOR)
                .append(OAuth.OAUTH_RESPONSE_TYPE).append(KEY_VALUE_SEPARATOR)
                .append(ResponseType.TOKEN)
                .toString();
    }

    private Response createTokenGrantResponseForUser(String oAuthRedirectUri, ClientApplication client,
                                                     Set<Scope> requestedGrantScopes, User user,
                                                     boolean issueRefreshToken) {
        TokenGrant newTokenGrant;
        try {
            newTokenGrant = createTokenGrantWithDefaults(tokenIssuer, authService,
                    user, client, issueRefreshToken);
            if (requestedGrantScopes != null) {
                Set<Scope> grantScopes = newTokenGrant.getGrantScopes();
                for (Scope grantScope : requestedGrantScopes) {
                    grantScopes.add(grantScope);
                }
                newTokenGrant.setGrantScopes(grantScopes);
            }
            authService.addGrant(newTokenGrant);
            OAuthASResponse.OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                    = addTokenGrantResponseParams(newTokenGrant, SC_FOUND);
            OAuthResponse response = oAuthTokenResponseBuilder.location(oAuthRedirectUri).buildQueryMessage();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthSystemException e) {
            log.severe("Could not create new token grant: " + e.getMessage());
            return handleOAuthSystemException(log, e, oAuthRedirectUri, SERVER_ERROR);
        }
    }

    // Checks for access token as both query and header-style parameter, then retrieves corresponding token grant
    private TokenGrant getTokenGrantFromAccessToken(HttpServletRequest request, String redirectUri)
            throws OAuthSystemException, OAuthProblemException {
        String accessToken = request.getParameter(OAUTH_TOKEN);
        if (accessToken == null) {
            OAuthAccessResourceRequest oAuthRequest = new
                    OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
            accessToken = trimAccessToken(oAuthRequest.getAccessToken());
        }
        Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByAccessToken(accessToken);
        if (!tokenGrantFound.isPresent()) {
            log.severe("Token grant could not be found");
            throw OAuthEndpointUtil.createOAuthProblemException(SERVER_ERROR, redirectUri);
        }
        return tokenGrantFound.get();
    }
}
