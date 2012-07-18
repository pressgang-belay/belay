package org.jboss.pressgangccms.oauth.server.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import org.apache.amber.oauth2.as.request.OAuthTokenRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.jboss.pressgangccms.oauth.server.data.model.auth.TokenGrant;
import org.jboss.pressgangccms.oauth.server.service.AuthService;
import org.jboss.pressgangccms.oauth.server.service.TokenIssuerService;

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
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static org.jboss.pressgangccms.oauth.server.util.Common.*;

/**
 * Serves as an OAuth token endpoint. Accepts refresh token grants only.
 *
 *@author kamiller@redhat.com (Katie Miller)
 */
@Path("/auth/token")
@RequestScoped
public class TokenService {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuerService tokenIssuerService;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {

        log.info("Processing token refresh request");

        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

            // Check that client_id is registered
            if (! isClientIdKnown(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
                log.warning("client_id could not be found");
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT)
                                .setErrorDescription(INVALID_CLIENT)
                                .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }
            // Process refresh token request
            else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.REFRESH_TOKEN.toString())) {
                if (isRefreshTokenValid(oauthRequest.getParam(OAuth.OAUTH_REFRESH_TOKEN),
                        oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID),
                        oauthRequest.getParam(OAuth.OAUTH_CLIENT_SECRET))) {
                    TokenGrant tokenGrant = createTokenGrant(oauthRequest);
                    log.info("Issuing access token: " + tokenGrant.getAccessToken());
                    log.info("Issuing refresh token: " + tokenGrant.getRefreshToken());
                    OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                            .setAccessToken(tokenGrant.getAccessToken())
                            .setExpiresIn(tokenGrant.getAccessTokenExpiry())
                            .setRefreshToken(tokenGrant.getRefreshToken())
                            .buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                } else {
                    log.warning("Invalid refresh token: " + oauthRequest.getParam(OAuth.OAUTH_REFRESH_TOKEN));
                    OAuthResponse response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription(INVALID_REFRESH_TOKEN)
                            .buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                }
            }

            // Anything else is invalid
            OAuthResponse response = OAuthASResponse
                    .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_GRANT)
                    .setErrorDescription(INVALID_GRANT_TYPE)
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        } catch (OAuthProblemException e) {
            log.warning("OAuthProblemException: " + e.getMessage());
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    private boolean isRefreshTokenValid(String refreshToken, String clientId, String clientSecret) throws OAuthSystemException {
        Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByRefreshToken(refreshToken);
        return tokenGrantFound.isPresent()
                && tokenGrantFound.get().getGrantCurrent()
                && tokenGrantFound.get().getGrantClient().getClientIdentifier().equals(clientId)
                && tokenGrantFound.get().getGrantClient().getClientSecret().equals(clientSecret);
    }

    private TokenGrant createTokenGrant(OAuthTokenRequest oauthRequest)
            throws OAuthSystemException {
        TokenGrant oldTokenGrant = authService.getTokenGrantByRefreshToken(oauthRequest
                                    .getParam(OAuth.OAUTH_REFRESH_TOKEN)).get();
        // Make sure no token grants held by the user are marked as current before issuing new one
        Set<TokenGrant> userTokenGrants = oldTokenGrant.getGrantUser().getTokenGrants();
        for (TokenGrant userTokenGrant : userTokenGrants) {
            if (userTokenGrant.getGrantCurrent()) {
                userTokenGrant.setGrantCurrent(false);
                authService.updateGrant(userTokenGrant);
            }
        }
        // Issue new grant
        TokenGrant newTokenGrant = new TokenGrant();
        newTokenGrant.setGrantUser(oldTokenGrant.getGrantUser());
        newTokenGrant.setGrantClient(oldTokenGrant.getGrantClient());
        newTokenGrant.setGrantScopes(newHashSet(oldTokenGrant.getGrantScopes()));
        newTokenGrant.setAccessToken(tokenIssuerService.accessToken());
        newTokenGrant.setRefreshToken(tokenIssuerService.refreshToken());
        newTokenGrant.setAccessTokenExpiry(ONE_HOUR);
        newTokenGrant.setGrantTimeStamp(new Date());
        newTokenGrant.setGrantCurrent(true);
        authService.addGrant(newTokenGrant);
        return newTokenGrant;
    }

    private boolean isClientIdKnown(String clientIdentifier) {
        return authService.getClient(clientIdentifier).isPresent();
    }
}
