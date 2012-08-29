package org.jboss.pressgangccms.oauth2.authserver.rest;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.as.request.OAuthTokenRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.jboss.pressgangccms.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgangccms.oauth2.authserver.service.AuthService;
import org.jboss.pressgangccms.oauth2.authserver.service.TokenIssuerService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthTokenResponseBuilder;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_REFRESH_TOKEN;
import static org.apache.amber.oauth2.common.error.OAuthError.ResourceResponse.INVALID_TOKEN;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE;
import static org.jboss.pressgangccms.oauth2.authserver.rest.OAuthWebServiceUtil.*;
import static org.jboss.pressgangccms.oauth2.authserver.util.Constants.*;

/**
 * Serves as an OAuth token endpoint. Accepts refresh token grants only. For use by confidential clients.
 *
 *@author kamiller@redhat.com (Katie Miller)
 */
@Path("/auth/token")
@RequestScoped
public class TokenWebService {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuerService tokenIssuerService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authorise(@Context HttpServletRequest request) throws OAuthSystemException {
        log.info("Processing token refresh request");

        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

            // Check that client_id is registered
            if (! isClientIdKnown(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
                log.warning("client_id could not be found");
                return buildResponse(buildOAuthJsonErrorResponse(INVALID_CLIENT, INVALID_CLIENT_APPLICATION));
            }
            // Process refresh token request
            else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.REFRESH_TOKEN.toString())) {
                if (isRefreshTokenValid(oauthRequest.getParam(OAUTH_REFRESH_TOKEN),
                        oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID),
                        oauthRequest.getParam(OAuth.OAUTH_CLIENT_SECRET))) {
                    TokenGrant tokenGrant = createTokenGrant(oauthRequest);
                    OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                            = addTokenGrantResponseParams(tokenGrant, HttpServletResponse.SC_OK);
                    return buildResponse(oAuthTokenResponseBuilder.buildJSONMessage());
                } else {
                    log.warning("Invalid refresh token: " + oauthRequest.getParam(OAUTH_REFRESH_TOKEN));
                    return buildResponse(buildOAuthJsonErrorResponse(INVALID_TOKEN, INVALID_REFRESH_TOKEN));
                }
            }
            // Anything else is invalid
            return buildResponse(buildOAuthJsonErrorResponse(UNSUPPORTED_GRANT_TYPE, INVALID_GRANT_TYPE));
        } catch (OAuthProblemException e) {
            log.warning("OAuthProblemException: " + e.getMessage());
            return buildResponse(buildOAuthJsonErrorResponse(e.getMessage(), e.getDescription()));
        }
    }

    private TokenGrant createTokenGrant(OAuthTokenRequest oauthRequest)
            throws OAuthSystemException {
        TokenGrant oldTokenGrant = authService.getTokenGrantByRefreshToken(oauthRequest
                                    .getParam(OAUTH_REFRESH_TOKEN)).get();
        // Make sure no token grants held by the identity are marked as current before issuing new one
        makeGrantsNonCurrent(authService, oldTokenGrant.getGrantIdentity().getTokenGrants());
        // Issue new grant
        TokenGrant newTokenGrant = createTokenGrantWithDefaults(tokenIssuerService, authService,
                oldTokenGrant.getGrantIdentity(), oldTokenGrant.getGrantClient(), true);
        newTokenGrant.setGrantScopes(newHashSet(oldTokenGrant.getGrantScopes()));
        authService.addGrant(newTokenGrant);
        return newTokenGrant;
    }

    private boolean isRefreshTokenValid(String refreshToken, String clientId, String clientSecret)
            throws OAuthSystemException {
        Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByRefreshToken(refreshToken);
        return tokenGrantFound.isPresent()
                && tokenGrantFound.get().getGrantCurrent()
                && tokenGrantFound.get().getGrantClient().getClientIdentifier().equals(clientId)
                && tokenGrantFound.get().getGrantClient().getClientSecret().equals(clientSecret);
    }

    private boolean isClientIdKnown(String clientIdentifier) {
        return authService.getClient(clientIdentifier).isPresent();
    }

    private OAuthResponse buildOAuthJsonErrorResponse(String error, String description) throws OAuthSystemException {
        return OAuthASResponse.errorResponse(HttpServletResponse.SC_NOT_FOUND)
                .setError(error)
                .setErrorDescription(description)
                .buildJSONMessage();
    }

    private Response buildResponse(OAuthResponse response) {
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }
}
