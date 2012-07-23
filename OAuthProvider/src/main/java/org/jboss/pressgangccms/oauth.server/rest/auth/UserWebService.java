package org.jboss.pressgangccms.oauth.server.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ParameterStyle;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.jboss.pressgangccms.oauth.server.data.domain.UserInfo;
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
import static org.jboss.pressgangccms.oauth.server.rest.auth.OAuthUtil.*;
import static org.jboss.pressgangccms.oauth.server.util.Common.*;

/**
 * Provides user services for client applications, such as associating another user
 * registration with the currently authenticated user and obtaining user information.
 * These services must be protected by the OAuth filter.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Path("/auth/user")
@RequestScoped
public class UserWebService {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuerService tokenIssuerService;

    /**
     * This endpoint allows client applications to associate a second user registration with the currently
     * authenticated user. The user will need to login to second account to authenticate. Set newIsPrimary
     * to true if the second user registration should become the primary user of the associated registrations.
     * In this case, user scopes should be supplied or the new TokenGrant returns will have the default scope only.
     * If this is false, whichever user is currently the primary from the user or group of associated users
     * currently authenticated will be the primary of the expanded association group.
     *
     * @param request      The servlet request
     * @param newIsPrimary True if the new user being associated should be the primary user, default false
     * @param provider     OpenID provider for the second registration
     * @param redirectUri  OAuth redirect URI
     * @param accessToken  OAuth access token, if this request is being made using the OAuth query-string style
     * @return             OAuth response containing access token, refresh token and expiry parameters, or an error
     * @throws             WebApplicationException if OAuth redirect URI is not provided
     */
    @GET
    @Path("/associate")
    public Response associateUser(@Context HttpServletRequest request,
                                  @QueryParam(NEW_ASSOC_PRIMARY) Boolean newIsPrimary,
                                  @QueryParam(OPENID_PROVIDER) String provider,
                                  @QueryParam(OAUTH_REDIRECT_URI) String redirectUri,
                                  @QueryParam(OAUTH_TOKEN) String accessToken) throws URISyntaxException {
        log.info("Processing user association request");
        String secondUserId = getStringAttributeFromSessionAndRemove(request, log, OPENID_IDENTIFIER,
                "Second user identifier");
        boolean isFirstRequest = secondUserId == null;
        Principal userPrincipal = request.getUserPrincipal();
        String oAuthRedirectUri = null;
        OAuthIdRequest oAuthRequest = null;
        TokenGrant requestTokenGrant;

        // Error checking //TODO refactor ugliness
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
            requestTokenGrant = getTokenGrantFromAccessToken(request, accessToken);
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
            log.info("First user is: " + userPrincipal.getName() + ". Redirecting to login");

            // Store initial request details in session
            request.getSession().setAttribute(FIRST_USER_ID, userPrincipal.getName());
            request.getSession().setAttribute(NEW_ASSOC_PRIMARY, newIsPrimary == null ? false : newIsPrimary);
            request.getSession().setAttribute(STORED_OAUTH_REDIRECT_URI, redirectUri);
            request.getSession().setAttribute(STORED_OAUTH_CLIENT_ID, oAuthRequest.getClientId());
            request.getSession().setAttribute(OAUTH_TOKEN, accessToken);
            if (oAuthRequest.getScopes() != null) {
                request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oAuthRequest.getScopes());
            }

            return Response.temporaryRedirect(URI.create(createNewRedirectUri(provider))).build();
        } else {
            log.info("Identifier to associate: " + secondUserId);
            String firstUserId = getStringAttributeFromSessionAndRemove(request, log, FIRST_USER_ID,
                    "First user identifier");
            Boolean secondUserIsPrimary = getBooleanAttributeFromSessionAndRemove(request, log, NEW_ASSOC_PRIMARY,
                    "Second user is primary flag");
            String clientId = getStringAttributeFromSessionAndRemove(request, log, STORED_OAUTH_CLIENT_ID,
                    "OAuth client id");
            Optional<ClientApplication> clientFound = authService.getClient(clientId);
            Set<String> scopesRequested = getStringSetAttributeFromSessionAndRemove(request, log,
                    OAuth.OAUTH_SCOPE, "Scopes requested");

            if (firstUserId == null || secondUserIsPrimary == null || clientId == null || (!clientFound.isPresent())) {
                log.severe("User association session attribute null or invalid");
                return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR)
                        .location(URI.create(oAuthRedirectUri)).build();
            }

            // Get users for identifiers
            Optional<User> firstUserFound = authService.getUser(firstUserId);
            Optional<User> secondUserFound = authService.getUser(secondUserId);

            if ((!firstUserFound.isPresent()) || (!secondUserFound.isPresent())) {
                return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR)
                        .location(URI.create(oAuthRedirectUri)).build();
            }
            User firstUser = firstUserFound.get();
            User secondUser = secondUserFound.get();

            // Find user groups
            UserGroup firstUserGroup = firstUser.getUserGroup();
            UserGroup secondUserGroup = secondUser.getUserGroup();
            UserGroup finalGroup;

            // Check users not already associated
            if (firstUserGroup.equals(secondUserGroup)) {
                return Response.status(HttpServletResponse.SC_NOT_FOUND).entity(USERS_ASSOCIATED_ERROR)
                        .location(URI.create(oAuthRedirectUri)).build();
            }

            // Merge user groups
            if (secondUserIsPrimary) {
                secondUserGroup.setPrimaryUser(secondUser);
                for (User user : firstUserGroup.getGroupUsers()) {
                    user.setUserGroup(secondUserGroup);
                    authService.updateUser(user);
                }
                authService.updateUserGroup(secondUserGroup);
                authService.deleteUserGroup(firstUserGroup);
                finalGroup = secondUserGroup;
            } else {
                for (User user : secondUserGroup.getGroupUsers()) {
                    user.setUserGroup(firstUserGroup);
                    authService.updateUser(user);
                }
                authService.deleteUserGroup(secondUserGroup);
                finalGroup = firstUserGroup;
            }

            // Grant token to primary user of resulting group
            User primaryUser = finalGroup.getPrimaryUser();

            // Create new TokenGrant for primary user, using scopes from previous TokenGrant if first user group was
            // the primary, or the requested scopes if not
            TokenGrant newTokenGrant;
            try {
                newTokenGrant = createTokenGrantWithDefaults(tokenIssuerService, authService,
                        primaryUser, clientFound.get());
                Set<Scope> grantScopes = null;
                if (secondUserIsPrimary) {
                    if (scopesRequested != null) {
                        grantScopes = checkScopes(authService, scopesRequested, secondUser.getUserScopes());
                    } else {
                        log.warning("User has default scope after association request as no scopes requested");
                    }
                } else {
                    grantScopes = newHashSet(requestTokenGrant.getGrantScopes());
                }
                newTokenGrant.setGrantScopes(grantScopes);
                authService.addGrant(newTokenGrant);

                // Make original TokenGrant non-current
                requestTokenGrant.setGrantCurrent(false);
                authService.updateGrant(requestTokenGrant);

                OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                        = addTokenGrantResponseParams(newTokenGrant, HttpServletResponse.SC_FOUND);
                OAuthResponse response = oAuthTokenResponseBuilder.location(oAuthRedirectUri).buildQueryMessage();
                log.info("Sending token response to " + oAuthRedirectUri);
                return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
            } catch (OAuthSystemException e) {
                log.severe("Could not create new token grant: " + e.getMessage());
                return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(SYSTEM_ERROR)
                        .location(URI.create(oAuthRedirectUri)).build();
            } catch (OAuthProblemException e) {
                log.warning("OAuthProblemException thrown: " + e.getError());
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(e.getError()).location(new URI(oAuthRedirectUri)).build();
            }
        }
    }

    private TokenGrant getTokenGrantFromAccessToken(HttpServletRequest request, String accessToken)
            throws OAuthSystemException, OAuthProblemException {
        if (accessToken == null) {
            OAuthAccessResourceRequest oAuthRequest = new
                    OAuthAccessResourceRequest(request, ParameterStyle.HEADER);
            accessToken = trimAccessToken(oAuthRequest.getAccessToken());
        }
        Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByAccessToken(accessToken);
        if (!tokenGrantFound.isPresent()) {
            throw OAuthProblemException.error(SYSTEM_ERROR);
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
                .append(ASSOCIATE_USER_ENDPOINT).append(PARAMETER_SEPARATOR)
                .append(OAuth.OAUTH_RESPONSE_TYPE).append(KEY_VALUE_SEPARATOR)
                .append(ResponseType.TOKEN)
                .toString();
    }

    // Second endpoint to change primary account
    // Supply identifier to make primary

    /**
     * This endpoint provides information about authenticated users. If a userIdentifier is provided and the
     * user represented by that identifier is part of the authorised user group, information will be provided
     * on that user. Otherwise, information will be returned on the primary user from the authorised user group.
     *
     * @param request           The servlet request
     * @param userIdentifier    Identifier of the user to return information about
     * @return                  User information in JSON format
     */
    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public UserInfo getPrimaryUserInfo(@Context HttpServletRequest request,
                                       @QueryParam(USER_IDENTIFIER) String userIdentifier) {
        Principal userPrincipal = request.getUserPrincipal();
        String identifierToQuery;

        if (userIdentifier == null) {
            identifierToQuery = userPrincipal.getName();
        } else {
            Optional<User> primaryUser = authService.getUser(request.getUserPrincipal().getName());
            if ((! primaryUser.isPresent() || (! authService.isUserInGroup(userIdentifier, primaryUser.get().getUserGroup())))) {
                log.warning("Could not process query on user " + userIdentifier + "; user unauthorised or system error");
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(UNAUTHORISED_QUERY_ERROR + " " + userIdentifier).build());
            }
            identifierToQuery = userIdentifier;
        }
        Optional<UserInfo> userInfoFound = authService.getUserInfo(identifierToQuery);
        if (! userInfoFound.isPresent()) {
            log.warning("User query failed; could not find user info for " + identifierToQuery);
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity(USER_QUERY_ERROR).build());
        }
        log.info("Returning user info for " + identifierToQuery);
        return userInfoFound.get();
    }
}
