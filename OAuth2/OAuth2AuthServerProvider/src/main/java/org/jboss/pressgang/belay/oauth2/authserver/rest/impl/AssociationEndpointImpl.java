package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.ClientApplication;
import org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.AssociationEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.logging.Logger;

import static org.apache.amber.oauth2.common.OAuth.OAUTH_REDIRECT_URI;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_TOKEN;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * This endpoint starts the process of associating two end-user identities together. It can be used for confidential
 * clients or public clients; if for confidential clients, it should be protected by Basic or some other authentication.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AssociationEndpointImpl implements AssociationEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

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
     * Required OAuth2 parameters, which may be supplied query or header style, are:
     * response_type: OAuth2 response type, token for public clients and code for confidential clients
     * client_id: OAuth2 client identifier, supplied by OAuth2 Auth Server
     * redirect_uri: URI to redirect to when auth complete; must be registered with the Auth Server
     * provider: the URL or domain of the OpenID provider with which to authenticate the end-user
     *
     * @param newIsPrimary True if the new identity being associated should be the primary identity, default false
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
            boolean isAuthorized = request.getAuthType() != null;
            oAuthRedirectUri = oAuthRequest.getRedirectURI();
            accessToken = getTokenGrantFromAccessToken(log, authService, request, oAuthRedirectUri).getAccessToken();
            log.info("First identifier is: " + userPrincipal.getName());
            ClientApplication client = checkClient(authService, oAuthRedirectUri, oAuthRequest.getClientId());
            checkAuthorization(isAuthorized, isClientPublic(client), oAuthRequest.getRedirectURI());
            // Record initial request details
            request.getSession().setAttribute(FIRST_IDENTIFIER, userPrincipal.getName());
            request.getSession().setAttribute(NEW_IDENTITY_PRIMARY, newIsPrimary == null ? false : newIsPrimary);
            request.getSession().setAttribute(STORED_OAUTH_REDIRECT_URI, oAuthRedirectUri);
            request.getSession().setAttribute(STORED_OAUTH_CLIENT_ID, oAuthRequest.getClientId());
            request.getSession().setAttribute(OAUTH_TOKEN, accessToken);
            if (oAuthRequest.getScopes() != null) {
                request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oAuthRequest.getScopes());
            }
            log.info("Redirecting to authorize second identifier");
            return Response.temporaryRedirect(URI.create(createNewRedirectUri(oAuthRequest.getParam(OPENID_PROVIDER)))).build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, oAuthRedirectUri, SERVER_ERROR);
        }
    }

    private boolean isClientPublic(ClientApplication client) {
        return client.getClientSecret() == null || client.getClientSecret().isEmpty();
    }

    private String createNewRedirectUri(String provider) {
        return new StringBuilder(publicAuthEndpoint).append(QUERY_STRING_MARKER)
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
}
