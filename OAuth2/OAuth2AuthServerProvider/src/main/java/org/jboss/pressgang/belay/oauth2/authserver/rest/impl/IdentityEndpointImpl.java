package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ParameterStyle;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.IdentityEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuerService;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.shared.data.model.IdentityInfo;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.newHashSet;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthTokenResponseBuilder;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_REDIRECT_URI;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_TOKEN;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.UNSUPPORTED_RESPONSE_TYPE;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;

/**
 * Provides identity services for client applications, such as associating another identity with the currently
 * authenticated user and obtaining identity information. These services must be protected by the OAuth filter.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public abstract class IdentityEndpointImpl implements IdentityEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuerService tokenIssuerService;

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
     * @throws WebApplicationException if OAuth redirect URI is not provided
     */
    @Override
    public Response associateUser(@Context HttpServletRequest request,
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
            log.info("First identifier is: " + userPrincipal.getName() + ". Redirecting to login");
            // Store initial request details in session
            request.getSession().setAttribute(FIRST_IDENTIFIER, userPrincipal.getName());
            request.getSession().setAttribute(NEW_IDENTITY_PRIMARY, newIsPrimary == null ? false : newIsPrimary);
            request.getSession().setAttribute(STORED_OAUTH_REDIRECT_URI, oAuthRedirectUri);
            request.getSession().setAttribute(STORED_OAUTH_CLIENT_ID, oAuthRequest.getClientId());
            request.getSession().setAttribute(OAUTH_TOKEN, accessToken);
            if (oAuthRequest.getScopes() != null) {
                request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oAuthRequest.getScopes());
            }
            return Response.temporaryRedirect(URI.create(createNewRedirectUri(oAuthRequest.getParam(OPENID_PROVIDER)))).build();
        } catch (OAuthProblemException e) {
            return OAuthEndpointUtil.handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, oAuthRedirectUri, SERVER_ERROR);
        }
    }

    @Override
    public Response completeAssociation(@Context HttpServletRequest request) throws OAuthProblemException,
            OAuthSystemException {
        log.info("Processing identity association request completion");

        String secondId = OAuthEndpointUtil.getStringAttributeFromSessionAndRemove(request, log, OPENID_IDENTIFIER, "Second identifier");
        log.info("Identifier to associate: " + secondId);
        String oAuthRedirectUri = OAuthEndpointUtil.getStringAttributeFromSessionAndRemove(request, log, STORED_OAUTH_REDIRECT_URI,
                "Stored OAuth redirect URI");
        TokenGrant requestTokenGrant = getTokenGrantFromAccessToken(request, oAuthRedirectUri);
        String firstId = OAuthEndpointUtil.getStringAttributeFromSessionAndRemove(request, log, FIRST_IDENTIFIER,
                "First identifier");
        Boolean secondIdentityIsPrimary = OAuthEndpointUtil.getBooleanAttributeFromSessionAndRemove(request, log, NEW_IDENTITY_PRIMARY,
                "Second identity is primary flag");
        String clientId = OAuthEndpointUtil.getStringAttributeFromSessionAndRemove(request, log, STORED_OAUTH_CLIENT_ID,
                "OAuth client id");
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        Set<String> scopesRequested = OAuthEndpointUtil.getStringSetAttributeFromSessionAndRemove(request, log,
                OAuth.OAUTH_SCOPE, "Scopes requested");

        if (firstId == null || secondId == null || secondIdentityIsPrimary == null || oAuthRedirectUri == null
                || clientId == null){
            log.severe("Identity association session attribute null or invalid");
            throw new OAuthSystemException("Null session attribute");
        }
        if (!clientFound.isPresent()) {
            throw OAuthEndpointUtil.createOAuthProblemException(INVALID_CLIENT, oAuthRedirectUri);
        }

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
        // Grant token to primary identity of resulting user
        // Use scopes from previous TokenGrant if first identity was the primary, or the requested scopes if not
        Identity primaryIdentity = finalUser.getPrimaryIdentity();
        Set<Scope> grantScopes = getGrantScopes(requestTokenGrant, secondIdentityIsPrimary, scopesRequested,
                secondIdentityFound.get(), oAuthRedirectUri);
        Response response = createTokenGrantResponseForIdentity(oAuthRedirectUri, clientFound.get(), grantScopes,
                primaryIdentity, (requestTokenGrant.getRefreshToken() != null));

        if (response.getStatus() == HttpServletResponse.SC_FOUND) {
            // Make original TokenGrant non-current
            OAuthEndpointUtil.makeGrantNonCurrent(authService, requestTokenGrant);
        }
        log.info("Sending token response to " + oAuthRedirectUri);
        return response;
    }

    /**
     * This endpoint provides information about identities associated with the authorised user. If an identifier is
     * provided and the identity represented by that identifier is associated with the authorised user, information
     * will be provided about that identity. Otherwise, information will be returned on the primary identity associated
     * with the authorised user.
     *
     * @param request    The servlet request
     * @param identifier Identifier of the identity to return information about
     * @return Identity information in JSON format
     */
    @Override
    public IdentityInfo getPrimaryUserInfo(@Context HttpServletRequest request,
                                           @QueryParam(IDENTIFIER) String identifier) {
        Principal userPrincipal = request.getUserPrincipal();
        String identifierToQuery;

        if (identifier == null) {
            identifierToQuery = userPrincipal.getName();
        } else {
            checkIdentityAssociatedWithAuthorisedUser(request, identifier);
            identifierToQuery = identifier;
        }
        Optional<IdentityInfo> identityInfoFound = authService.getUserInfo(identifierToQuery);
        if (!identityInfoFound.isPresent()) {
            log.warning("Identity query failed; could not find identity info for " + identifierToQuery);
            throw OAuthEndpointUtil.createWebApplicationException(IDENTITY_QUERY_ERROR, HttpServletResponse.SC_NOT_FOUND);
        }
        log.info("Returning identity info for " + identifierToQuery);
        return identityInfoFound.get();
    }

    /**
     * This endpoint allows an identity of the currently authorised user to be set as the user's primary identity
     * If the request is successful, a new token response will be generated for the resulting primary identity, with
     * the standard access token, refresh token and access token expiry time parameters. A new token response will be
     * generated even if the identity was already the user's primary identity. The standard OAuth parameters must be
     * supplied as part of the request: redirect_uri, client_id and response_type. If an OAuth parameter is missing or
     * invalid, or the identifier is invalid or not associated with the authorised user, an error response will be
     * returned.
     *
     * @param request    The servlet request
     * @param identifier The identifier of the identity to set as primary
     * @return A token response for the resulting primary identity
     */
    @Override
    public Response makeIdentityPrimary(@Context HttpServletRequest request,
                                        @QueryParam(IDENTIFIER) String identifier) {
        log.info("Processing request to make " + identifier + " primary identity");
        try {
            OAuthAuthzRequest oAuthRequest = new OAuthAuthzRequest(request);
            if (!oAuthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE).equals(ResponseType.TOKEN.toString())) {
                log.severe("Response type requested is unsupported: " + oAuthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE));
                throw OAuthEndpointUtil.createOAuthProblemException(UNSUPPORTED_RESPONSE_TYPE, oAuthRequest.getRedirectURI());
            }
            if (identifier == null || identifier.length() == 0) {
                log.warning("Invalid identifier supplied");
                throw OAuthEndpointUtil.createOAuthProblemException(INVALID_IDENTIFIER, oAuthRequest.getRedirectURI());
            }
            TokenGrant tokenGrant = getTokenGrantFromAccessToken(request, oAuthRequest.getRedirectURI());
            return doMakeIdentityPrimary(request, identifier, oAuthRequest, tokenGrant);
        } catch (OAuthProblemException e) {
            return OAuthEndpointUtil.handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, null, SERVER_ERROR);
        }
    }

    private Response doMakeIdentityPrimary(HttpServletRequest request, String identifier,
                                           OAuthAuthzRequest oAuthRequest, TokenGrant currentGrant)
            throws OAuthProblemException {
        checkIdentityAssociatedWithAuthorisedUser(request, identifier);
        Identity newPrimaryIdentity = authService.getIdentity(identifier).get();
        User user = newPrimaryIdentity.getUser();
        user.setPrimaryIdentity(newPrimaryIdentity);
        authService.updateUser(user);
        Optional<ClientApplication> clientFound = authService.getClient(oAuthRequest.getClientId());
        if (!clientFound.isPresent()) {
            log.warning("Client not found");
            throw OAuthEndpointUtil.createOAuthProblemException(INVALID_CLIENT, oAuthRequest.getRedirectURI());
        }
        Set<Scope> grantScopes = null;
        if (oAuthRequest.getScopes() != null && (!oAuthRequest.getScopes().isEmpty())) {
            grantScopes = OAuthEndpointUtil.checkScopes(authService, oAuthRequest.getScopes(), newPrimaryIdentity.getIdentityScopes(),
                    oAuthRequest.getRedirectURI());
        }
        // Generate new token grant response
        Response response = createTokenGrantResponseForIdentity(oAuthRequest.getRedirectURI(), clientFound.get(),
                grantScopes, newPrimaryIdentity, (currentGrant.getRefreshToken() != null));
        if (response.getStatus() == HttpServletResponse.SC_FOUND) {
            // Make original TokenGrant non-current
            OAuthEndpointUtil.makeGrantNonCurrent(authService, currentGrant);
        }
        log.info("Sending token response to " + oAuthRequest.getRedirectURI());
        return response;
    }

    private Response createTokenGrantResponseForIdentity(String oAuthRedirectUri, ClientApplication client,
                                                         Set<Scope> grantScopes, Identity identity,
                                                         boolean issueRefreshToken) {
        TokenGrant newTokenGrant;
        try {
            newTokenGrant = createTokenGrantWithDefaults(tokenIssuerService, authService,
                    identity, client, issueRefreshToken);
            if (grantScopes != null) {
                newTokenGrant.setGrantScopes(grantScopes);
            }
            authService.addGrant(newTokenGrant);
            OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                    = addTokenGrantResponseParams(newTokenGrant, SC_FOUND);
            OAuthResponse response = oAuthTokenResponseBuilder.location(oAuthRedirectUri).buildQueryMessage();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthSystemException e) {
            log.severe("Could not create new token grant: " + e.getMessage());
            return handleOAuthSystemException(log, e, oAuthRedirectUri, SERVER_ERROR);
        }
    }

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

    private Set<Scope> getGrantScopes(TokenGrant requestTokenGrant, Boolean secondIdentityIsPrimary,
                                      Set<String> scopesRequested, Identity secondIdentity,
                                      String redirectUri) throws OAuthProblemException {
        if (secondIdentityIsPrimary) {
            if (scopesRequested != null) {
                return OAuthEndpointUtil.checkScopes(authService, scopesRequested, secondIdentity.getIdentityScopes(), redirectUri);
            } else {
                log.warning("Identity has default scope after association request as no scopes requested");
                return null;
            }
        } else {
            return newHashSet(requestTokenGrant.getGrantScopes());
        }
    }

    private User mergeUsers(Boolean secondIdentityIsPrimary, Identity secondIdentity, User firstUser, User secondUser) {
        User finalUser;
        if (secondIdentityIsPrimary) {
            secondUser.setPrimaryIdentity(secondIdentity);
            for (Identity identity : firstUser.getUserIdentities()) {
                identity.setUser(secondUser);
                authService.updateIdentity(identity);
            }
            authService.updateUser(secondUser);
            authService.deleteUser(firstUser);
            finalUser = secondUser;
        } else {
            for (Identity identity : secondUser.getUserIdentities()) {
                identity.setUser(firstUser);
                authService.updateIdentity(identity);
            }
            authService.deleteUser(secondUser);
            finalUser = firstUser;
        }
        return finalUser;
    }

    private String createNewRedirectUri(String provider) {
        return new StringBuilder(LOGIN_ENDPOINT).append(QUERY_STRING_MARKER)
                .append(OPENID_PROVIDER).append(KEY_VALUE_SEPARATOR)
                .append(provider).append(PARAMETER_SEPARATOR)
                .append(OAuth.OAUTH_CLIENT_ID).append(KEY_VALUE_SEPARATOR)
                .append(OAUTH_PROVIDER_ID).append(PARAMETER_SEPARATOR)
                .append(OAUTH_REDIRECT_URI).append(KEY_VALUE_SEPARATOR)
                .append(COMPLETE_ASSOCIATION_ENDPOINT).append(PARAMETER_SEPARATOR)
                .append(OAuth.OAUTH_RESPONSE_TYPE).append(KEY_VALUE_SEPARATOR)
                .append(ResponseType.TOKEN)
                .toString();
    }

    private void checkIdentityAssociatedWithAuthorisedUser(HttpServletRequest request, String identifier) {
        Optional<Identity> primaryIdentity = authService.getIdentity(request.getUserPrincipal().getName());
        if ((!primaryIdentity.isPresent() || (!authService.isIdentityAssociatedWithUser(identifier,
                primaryIdentity.get().getUser())))) {
            log.warning("Could not process request related to identity " + identifier
                    + " ; user unauthorised or system error");
            throw OAuthEndpointUtil.createWebApplicationException(UNAUTHORISED_QUERY_ERROR + " " + identifier,
                    HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
