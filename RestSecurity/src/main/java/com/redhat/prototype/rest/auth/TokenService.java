package com.redhat.prototype.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.redhat.prototype.data.model.auth.TokenGrant;
import com.redhat.prototype.service.AuthService;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuer;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.request.OAuthTokenRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.logging.Logger;

import static com.redhat.prototype.util.Common.*;

@Path("/auth/token")
@RequestScoped
public class TokenService {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {

        log.info("Processing token refresh request");

        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);

            // Check that client_id is registered
            if (! isClientIdKnown(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
                log.severe("client_id could not be found");
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
                    TokenGrant tokenGrant = createTokenGrant(oauthIssuerImpl, oauthRequest);
                    log.info("Issuing access token: " + tokenGrant.getAccessToken());
                    log.info("Issuing refresh token: " + tokenGrant.getRefreshToken());
                    OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                            .setAccessToken(tokenGrant.getAccessToken())
                            .setExpiresIn(tokenGrant.getAccessTokenExpiry())
                            .setRefreshToken(tokenGrant.getRefreshToken())
                            .buildJSONMessage();
                    log.info("Entity body: " + response.getBody());
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                } else {
                    log.severe("Invalid refresh token: " + oauthRequest.getParam(OAuth.OAUTH_REFRESH_TOKEN));
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
            log.info("OAuthProblemException: " + e.getMessage());
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    private boolean isRefreshTokenValid(String refreshToken, String clientId, String clientSecret) {
        Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByRefreshToken(refreshToken);
        //TODO add check to ensure TokenGrant is current if necessary
        return tokenGrantFound.isPresent()
                && tokenGrantFound.get().getGrantClient().getClientIdentifier().equals(clientId)
                && tokenGrantFound.get().getGrantClient().getClientSecret().equals(clientSecret);
    }

    private TokenGrant createTokenGrant(OAuthIssuer oauthIssuerImpl, OAuthTokenRequest oauthRequest)
            throws OAuthSystemException {
        TokenGrant tokenGrant = authService.getTokenGrantByRefreshToken(oauthRequest
                                    .getParam(OAuth.OAUTH_REFRESH_TOKEN)).get();
        tokenGrant.setAccessToken(oauthIssuerImpl.accessToken());
        tokenGrant.setRefreshToken(oauthIssuerImpl.refreshToken());
        tokenGrant.setAccessTokenExpiry(ONE_HOUR);
        tokenGrant.setGrantTimeStamp(new Date());
        //TODO make sure old TokenGrant info now properly invalidated if necessary
        authService.updateGrant(tokenGrant);
        return tokenGrant;
    }

    private boolean isClientIdKnown(String clientIdentifier) {
        return authService.getClient(clientIdentifier).isPresent();
    }
}
