package org.jboss.pressgangccms.oauth.server.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ParameterStyle;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.jboss.pressgangccms.oauth.server.data.domain.IdentityInfo;
import org.jboss.pressgangccms.oauth.server.data.model.auth.*;
import org.jboss.pressgangccms.oauth.server.oauth.login.OAuthIdRequest;
import org.jboss.pressgangccms.oauth.server.service.AuthService;
import org.jboss.pressgangccms.oauth.server.service.TokenIssuerService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthTokenResponseBuilder;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_REDIRECT_URI;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_TOKEN;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.UNSUPPORTED_RESPONSE_TYPE;
import static org.jboss.pressgangccms.oauth.server.rest.auth.OAuthUtil.*;
import static org.jboss.pressgangccms.oauth.server.util.Common.*;

/**
 * Provides identity services for client applications, such as associating another identity with the currently
 * authenticated user and obtaining identity information. These services must be protected by the OAuth filter.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Path("/auth/identity")
@RequestScoped
public class IdentityWebService {

    @Inject
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
     *
     * Set newIsPrimary to true if the second identity should become the user's primary identity.
     * In this case, identity scopes should be requested or the new token info returned will have the
     * default scope only. If this is false, whichever identity is currently the authenticated user's
     * primary identity will remain the primary identity of the resulting user.
     *
     * @param request      The servlet request
     * @param newIsPrimary True if the new identity being associated should be the primary identity, default false
     * @param provider     OpenID provider for the second identity
     * @param redirectUri  OAuth redirect URI
     * @param accessToken  OAuth access token, if this request is being made using the OAuth query-string style
     * @return OAuth response containing access token, refresh token and expiry parameters, or an error
     * @throws WebApplicationException if OAuth redirect URI is not provided
     */
    @GET
    @Path("/associate")
    public Response associateUser(@Context HttpServletRequest request,
                                  @QueryParam(NEW_IDENTITY_PRIMARY) Boolean newIsPrimary,
                                  @QueryParam(OPENID_PROVIDER) String provider,
                                  @QueryParam(OAUTH_REDIRECT_URI) String redirectUri,
                                  @QueryParam(OAUTH_TOKEN) String accessToken) throws URISyntaxException {
        log.info("Processing identity association request");
        String secondId = getStringAttributeFromSessionAndRemove(request, log, OPENID_IDENTIFIER,
                "Second identifier");
        boolean isFirstRequest = secondId == null;
        Principal userPrincipal = request.getUserPrincipal();
        String oAuthRedirectUri = null;
        OAuthIdRequest oAuthRequest = null;
        TokenGrant requestTokenGrant;

        //TODO refactor this to two endpoints - GET and POST for second request
        // Error checking //TODO refactor exception ugliness on this and other endpoints + refactor this ugliness
        try {
            if (isFirstRequest) {
                oAuthRequest = new OAuthIdRequest(request);
                oAuthRedirectUri = oAuthRequest.getRedirectURI();
            } else {
                String sessionRedirectUri = (String) request.getSession().getAttribute(STORED_OAUTH_REDIRECT_URI);
                if (sessionRedirectUri != null) {
                    oAuthRedirectUri = sessionRedirectUri;
                } else {
                    log.severe("OAuth redirect URI session attribute null");
                    throw new WebApplicationException(
                            Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR).build());
                }
            }
            requestTokenGrant = getTokenGrantFromAccessToken(request, accessToken, oAuthRedirectUri);
        } catch (OAuthProblemException e) {
            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_NOT_FOUND);
            String requestRedirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(requestRedirectUri)) {
                throw new WebApplicationException(
                        responseBuilder.entity(OAUTH_CALLBACK_URL_REQUIRED).build());
            }
            return responseBuilder.entity(e.getError()).location(new URI(requestRedirectUri)).build();
        } catch (OAuthSystemException e) {
            log.severe("OAuthSystemException thrown: " + e.getMessage());
            if (oAuthRedirectUri != null) {
                return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(e.getMessage())
                        .location(URI.create(oAuthRedirectUri)).build();
            } else {
                throw new WebApplicationException(
                        Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR).build());
            }
        }

        if (isFirstRequest) {
            log.info("First identifier is: " + userPrincipal.getName() + ". Redirecting to login");

            // Store initial request details in session
            request.getSession().setAttribute(FIRST_IDENTIFIER, userPrincipal.getName());
            request.getSession().setAttribute(NEW_IDENTITY_PRIMARY, newIsPrimary == null ? false : newIsPrimary);
            request.getSession().setAttribute(STORED_OAUTH_REDIRECT_URI, redirectUri);
            request.getSession().setAttribute(STORED_OAUTH_CLIENT_ID, oAuthRequest.getClientId());
            request.getSession().setAttribute(OAUTH_TOKEN, accessToken);
            if (oAuthRequest.getScopes() != null) {
                request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oAuthRequest.getScopes());
            }

            return Response.temporaryRedirect(URI.create(createNewRedirectUri(provider))).build();
        } else {
            log.info("Identifier to associate: " + secondId);
            String firstId = getStringAttributeFromSessionAndRemove(request, log, FIRST_IDENTIFIER,
                    "First identifier");
            Boolean secondIdentityIsPrimary = getBooleanAttributeFromSessionAndRemove(request, log, NEW_IDENTITY_PRIMARY,
                    "Second identity is primary flag");
            String clientId = getStringAttributeFromSessionAndRemove(request, log, STORED_OAUTH_CLIENT_ID,
                    "OAuth client id");
            Optional<ClientApplication> clientFound = authService.getClient(clientId);
            Set<String> scopesRequested = getStringSetAttributeFromSessionAndRemove(request, log,
                    OAuth.OAUTH_SCOPE, "Scopes requested");

            if (firstId == null || secondIdentityIsPrimary == null || clientId == null || (!clientFound.isPresent())) {
                log.severe("Identity association session attribute null or invalid");
                return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR)
                        .location(URI.create(oAuthRedirectUri)).build();
            }

            // Get users for identifiers
            Optional<Identity> firstIdentityFound = authService.getIdentity(firstId);
            Optional<Identity> secondIdentityFound = authService.getIdentity(secondId);

            if ((!firstIdentityFound.isPresent()) || (!secondIdentityFound.isPresent())) {
                return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR)
                        .location(URI.create(oAuthRedirectUri)).build();
            }
            Identity firstIdentity = firstIdentityFound.get();
            Identity secondIdentity = secondIdentityFound.get();

            // Find users
            User firstUser = firstIdentity.getUser();
            User secondUser = secondIdentity.getUser();
            User finalUser;

            // Check users not already associated
            if (firstUser.equals(secondUser)) {
                return Response.status(HttpServletResponse.SC_NOT_FOUND).entity(IDENTITIES_ASSOCIATED_ERROR)
                        .location(URI.create(oAuthRedirectUri)).build();
            }

            // Merge users
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

            // Grant token to primary user of resulting group
            Identity primaryIdentity = finalUser.getPrimaryIdentity();

            // Use scopes from previous TokenGrant if first user group was the primary, or the requested scopes if not
            Set<Scope> grantScopes = null;

            if (secondIdentityIsPrimary) {
                if (scopesRequested != null) {
                    try {
                        grantScopes = checkScopes(authService, scopesRequested, secondIdentity.getIdentityScopes());
                    } catch (OAuthProblemException e) {
                        log.warning("OAuthProblemException thrown: " + e.getError());
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity(e.getError()).location(new URI(oAuthRedirectUri)).build();
                    }
                } else {
                    log.warning("Identity has default scope after association request as no scopes requested");
                }
            } else {
                grantScopes = newHashSet(requestTokenGrant.getGrantScopes());
            }
            Response response = createTokenGrantResponseForIdentity(oAuthRedirectUri, clientFound.get(), grantScopes,
                    primaryIdentity);

            if (response.getStatus() == HttpServletResponse.SC_FOUND) {
                // Make original TokenGrant non-current
                makeGrantNonCurrent(authService, requestTokenGrant);
            }
            log.info("Sending token response to " + oAuthRedirectUri);
            return response;
        }
    }

    private Response createTokenGrantResponseForIdentity(String oAuthRedirectUri, ClientApplication client, Set<Scope> grantScopes,
                                                         Identity identity) {
        TokenGrant newTokenGrant;
        try {
            newTokenGrant = createTokenGrantWithDefaults(tokenIssuerService, authService,
                    identity, client);
            if (grantScopes != null) {
                newTokenGrant.setGrantScopes(grantScopes);
            }
            authService.addGrant(newTokenGrant);

            OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                    = addTokenGrantResponseParams(newTokenGrant, HttpServletResponse.SC_FOUND);
            OAuthResponse response = oAuthTokenResponseBuilder.location(oAuthRedirectUri).buildQueryMessage();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthSystemException e) {
            log.severe("Could not create new token grant: " + e.getMessage());
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR)
                    .location(URI.create(oAuthRedirectUri)).build();
        }
    }

    private TokenGrant getTokenGrantFromAccessToken(HttpServletRequest request, String accessToken, String redirectUri)
            throws OAuthSystemException, OAuthProblemException {
        if (accessToken == null) {
            OAuthAccessResourceRequest oAuthRequest = new
                    OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
            accessToken = trimAccessToken(oAuthRequest.getAccessToken());
        }
        Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByAccessToken(accessToken);
        if (!tokenGrantFound.isPresent()) {
            log.severe("Token grant could not be found");
            throw createOAuthProblemException(SYSTEM_ERROR, redirectUri);
        }
        return tokenGrantFound.get();
    }

    private String createNewRedirectUri(String provider) {
        return new StringBuilder(LOGIN_ENDPOINT).append(QUERY_STRING_MARKER)
                .append(OPENID_PROVIDER).append(KEY_VALUE_SEPARATOR)
                .append(provider).append(PARAMETER_SEPARATOR)
                .append(OAuth.OAUTH_CLIENT_ID).append(KEY_VALUE_SEPARATOR)
                .append(OAUTH_PROVIDER_ID).append(PARAMETER_SEPARATOR)
                .append(OAUTH_REDIRECT_URI).append(KEY_VALUE_SEPARATOR)
                .append(ASSOCIATE_IDENTITY_ENDPOINT).append(PARAMETER_SEPARATOR)
                .append(OAuth.OAUTH_RESPONSE_TYPE).append(KEY_VALUE_SEPARATOR)
                .append(ResponseType.TOKEN)
                .toString();
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
     * @param request           The servlet request
     * @param identifier        The identifier of the identity to set as primary
     * @return                  A token response for the resulting primary identity
     */
    @GET
    @Path("/makePrimary")
    public Response redirectGet(@Context HttpServletRequest request,
                                @QueryParam(IDENTIFIER) String identifier,
                                @QueryParam(OAUTH_TOKEN) String accessToken) {
        log.info("Processing request to make " + identifier + " primary identity");
        try {
            OAuthAuthzRequest oAuthRequest = new OAuthAuthzRequest(request);
            if (!oAuthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE).equals(ResponseType.TOKEN.toString())) {
                log.severe("Response type requested is unsupported: " + oAuthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE));
                throw createOAuthProblemException(UNSUPPORTED_RESPONSE_TYPE, oAuthRequest.getRedirectURI());
            }
            if (identifier == null || identifier.length() == 0) {
                log.warning("Invalid identifier supplied");
                throw createOAuthProblemException(INVALID_IDENTIFIER, oAuthRequest.getRedirectURI());
            }
            TokenGrant tokenGrant = getTokenGrantFromAccessToken(request, accessToken, oAuthRequest.getRedirectURI());
            return makeIdentityPrimary(request, identifier, oAuthRequest, tokenGrant);
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(e);
        }
    }

    private Response handleOAuthSystemException(OAuthSystemException e) {
        log.severe("OAuthSystemException thrown: " + e.getMessage());
        return Response.serverError().build();
    }

    private Response makeIdentityPrimary(HttpServletRequest request, String identifier,
                                         OAuthAuthzRequest oAuthRequest, TokenGrant currentGrant) {
        checkIdentityAssociatedWithAuthorisedUser(request, identifier);
        Identity newPrimaryIdentity = authService.getIdentity(identifier).get();
        User user = newPrimaryIdentity.getUser();
        user.setPrimaryIdentity(newPrimaryIdentity);
        authService.updateUser(user);
        try {
            Optional<ClientApplication> clientFound = authService.getClient(oAuthRequest.getClientId());
            if (!clientFound.isPresent()) {
                log.warning("Client not found");
                throw createOAuthProblemException(SYSTEM_ERROR, oAuthRequest.getRedirectURI());
            }
            Set<Scope> grantScopes = null;
            if (oAuthRequest.getScopes() != null && (!oAuthRequest.getScopes().isEmpty())) {
                grantScopes = checkScopes(authService, oAuthRequest.getScopes(), newPrimaryIdentity.getIdentityScopes());
            }
            // Generate new token grant response
            Response response = createTokenGrantResponseForIdentity(oAuthRequest.getRedirectURI(), clientFound.get(),
                    grantScopes, newPrimaryIdentity);

            if (response.getStatus() == HttpServletResponse.SC_FOUND) {
                // Make original TokenGrant non-current
                makeGrantNonCurrent(authService, currentGrant);
            }
            log.info("Sending token response to " + oAuthRequest.getRedirectURI());
            return response;
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(e);
        }
    }

    private Response handleOAuthProblemException(OAuthProblemException e) {
        log.warning("OAuthProblemException thrown: " + e.getMessage() + " " + e.getDescription());

        final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_NOT_FOUND);
        String redirectUri = e.getRedirectUri();

        if (OAuthUtils.isEmpty(redirectUri)) {
            throw new WebApplicationException(
                    responseBuilder.entity(OAUTH_CALLBACK_URL_REQUIRED).build());
        }
        return responseBuilder.entity(e.getError()).location(URI.create(redirectUri)).build();
    }

    /**
     * This endpoint provides information about identities associated with the authorised user. If an identifier is
     * provided and the identity represented by that identifier is associated with the authorised user, information
     * will be provided about that identity. Otherwise, information will be returned on the primary identity associated
     * with the authorised user.
     *
     * @param request        The servlet request
     * @param identifier     Identifier of the identity to return information about
     * @return               Identity information in JSON format
     */
    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
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
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(IDENTITY_QUERY_ERROR).build());
        }
        log.info("Returning identity info for " + identifierToQuery);
        return identityInfoFound.get();
    }

    private void checkIdentityAssociatedWithAuthorisedUser(HttpServletRequest request, String identifier) {
        Optional<Identity> primaryIdentity = authService.getIdentity(request.getUserPrincipal().getName());
        if ((!primaryIdentity.isPresent() || (!authService.isIdentityAssociatedWithUser(identifier,
                primaryIdentity.get().getUser())))) {
            log.warning("Could not process request related to identity " + identifier + "; user unauthorised or system error");
            throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                    .entity(UNAUTHORISED_QUERY_ERROR + " " + identifier).build());
        }
    }
}
