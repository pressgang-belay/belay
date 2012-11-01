package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.as.request.OAuthTokenRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.GrantType;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.CodeGrant;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.TokenEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.joda.time.DateTime;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.newHashSet;
import static org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthTokenResponseBuilder;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_CODE;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_REFRESH_TOKEN;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_GRANT;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.UNSUPPORTED_GRANT_TYPE;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.filterTokenGrantsByClient;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.makeExpiringTokenGrantsNonCurrent;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;

/**
 * OAuth token endpoint. Accepts refresh token or auth code grants and provides OAuth2 token grants. For use by confidential
 * clients only. Endpoint must be protected by Basic or some other authentication method.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class TokenEndpointImpl implements TokenEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuer tokenIssuer;

    /**
     * Endpoint to obtain a token grant, using either an authorization code or refresh token as the grant.
     * Required OAuth2 parameters are:
     * client_id: OAuth2 client identifier, supplied by OAuth2 Auth Server
     * client_secret: OAuth2 client secret, supplied by the OAuth2 Auth Server
     * grant_type: OAuth2 grant type, refresh_token or code
     * <p/>
     * If the grant type is refresh_token, another required parameter is:
     * refresh_token: The refresh token, obtained with a previous token grant
     * <p/>
     * If the grant type is authorization code, other required parameters are:
     * code: The authorization code obtained through the auth endpoint
     * redirect_uri: The client's registered redirect URI
     *
     * @return Token grant response including access_token, expires_in and refresh_token parameters
     * @throws OAuthSystemException
     */
    @Override
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {
        log.info("Processing token refresh request");

        try {
            OAuthTokenRequest oAuthRequest = new OAuthTokenRequest(request);

            // Check that client_id is registered
            if (!isClientIdKnown(oAuthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
                log.warning("client_id could not be found");
                return buildResponse(buildOAuthJsonErrorResponse(INVALID_CLIENT, INVALID_CLIENT_APPLICATION));
            }
            // Process refresh token grant request
            if (oAuthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.REFRESH_TOKEN.toString())) {
                if (isRefreshTokenValid(oAuthRequest.getParam(OAUTH_REFRESH_TOKEN),
                        oAuthRequest.getParam(OAuth.OAUTH_CLIENT_ID),
                        oAuthRequest.getParam(OAuth.OAUTH_CLIENT_SECRET))) {
                    TokenGrant tokenGrant = createTokenGrantFromOldGrant(oAuthRequest, oAuthRequest.getClientId());
                    OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                            = OAuthEndpointUtil.addTokenGrantResponseParams(tokenGrant, HttpServletResponse.SC_OK, null);
                    return buildResponse(oAuthTokenResponseBuilder.buildJSONMessage());
                } else {
                    log.warning("Invalid refresh token: " + oAuthRequest.getParam(OAUTH_REFRESH_TOKEN));
                    return buildResponse(buildOAuthJsonErrorResponse(INVALID_GRANT, INVALID_REFRESH_TOKEN));
                }
            }
            // Process authorization code grant request
            if (oAuthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.AUTHORIZATION_CODE.toString())) {
                if (isAuthCodeValid(oAuthRequest.getParam(OAUTH_CODE),
                        oAuthRequest.getParam(OAuth.OAUTH_CLIENT_ID),
                        oAuthRequest.getParam(OAuth.OAUTH_CLIENT_SECRET))) {
                    TokenGrant tokenGrant = createTokenGrantFromCodeGrant(oAuthRequest, oAuthRequest.getClientId());
                    OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                            = OAuthEndpointUtil.addTokenGrantResponseParams(tokenGrant, HttpServletResponse.SC_OK, null);
                    return buildResponse(oAuthTokenResponseBuilder.buildJSONMessage());
                } else {
                    log.warning("Invalid auth code: " + oAuthRequest.getParam(OAUTH_CODE));
                    return buildResponse(buildOAuthJsonErrorResponse(INVALID_GRANT, INVALID_AUTH_CODE));
                }
            }
            // Anything else is invalid
            return buildResponse(buildOAuthJsonErrorResponse(UNSUPPORTED_GRANT_TYPE, INVALID_GRANT_TYPE));
        } catch (OAuthProblemException e) {
            log.warning("OAuthProblemException: " + e.getMessage());
            return buildResponse(buildOAuthJsonErrorResponse(e.getMessage(), e.getDescription()));
        }
    }

    private TokenGrant createTokenGrantFromOldGrant(OAuthTokenRequest oAuthRequest, final String clientId)
            throws OAuthSystemException {
        TokenGrant oldTokenGrant = authService.getTokenGrantByRefreshToken(oAuthRequest
                .getParam(OAUTH_REFRESH_TOKEN)).get();
        // Make sure no token grants held by the user for the client app are marked as current before issuing new one
        makeExpiringTokenGrantsNonCurrent(authService, filterTokenGrantsByClient(oldTokenGrant.getGrantUser().getTokenGrants(), clientId));
        // Issue new grant
        TokenGrant newTokenGrant = OAuthEndpointUtil.createTokenGrantWithDefaults(tokenIssuer, authService,
                oldTokenGrant.getGrantUser(), oldTokenGrant.getGrantClient(), true);
        newTokenGrant.setGrantScopes(newHashSet(oldTokenGrant.getGrantScopes()));
        authService.addTokenGrant(newTokenGrant);
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

    private TokenGrant createTokenGrantFromCodeGrant(OAuthTokenRequest oAuthRequest, final String clientId)
            throws OAuthSystemException {
        CodeGrant codeGrant = authService.getCodeGrantByAuthCode(oAuthRequest.getParam(OAUTH_CODE)).get();
        // Make sure no token grants held by the user for the client app are marked as current before issuing new one
        makeExpiringTokenGrantsNonCurrent(authService, filterTokenGrantsByClient(codeGrant.getGrantUser().getTokenGrants(), clientId));
        // Issue new grant
        TokenGrant newTokenGrant = OAuthEndpointUtil.createTokenGrantWithDefaults(tokenIssuer, authService,
                codeGrant.getGrantUser(), codeGrant.getGrantClient(), true);
        newTokenGrant.setGrantScopes(newHashSet(codeGrant.getGrantScopes()));
        authService.addTokenGrant(newTokenGrant);
        return newTokenGrant;
    }

    private boolean isAuthCodeValid(String authCode, String clientId, String clientSecret)
            throws OAuthSystemException, OAuthProblemException {
        Optional<CodeGrant> codeGrantFound = authService.getCodeGrantByAuthCode(authCode);
        return codeGrantFound.isPresent()
                && codeGrantFound.get().getGrantCurrent()
                && codeGrantFound.get().getGrantClient().getClientIdentifier().equals(clientId)
                && codeGrantFound.get().getGrantClient().getClientSecret().equals(clientSecret)
                && getAuthCodeExpiry(codeGrantFound.get()).isAfterNow();
    }

    private DateTime getAuthCodeExpiry(CodeGrant codeGrant) throws OAuthProblemException {
        int expirySeconds;
        try {
            expirySeconds = Integer.parseInt(codeGrant.getCodeExpiry());
        } catch (NumberFormatException e) {
            log.warning("NumberFormatException during auth code check: " + e);
            throw OAuthProblemException.error(INVALID_GRANT);
        }
        return new DateTime(codeGrant.getGrantTimeStamp()).plusSeconds(Math.abs(expirySeconds));
    }

    private boolean isClientIdKnown(String clientIdentifier) {
        return authService.getClient(clientIdentifier).isPresent();
    }

    private OAuthResponse buildOAuthJsonErrorResponse(String error, String description) throws OAuthSystemException {
        return OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                .setError(error)
                .setErrorDescription(description)
                .buildJSONMessage();
    }

    private Response buildResponse(OAuthResponse response) {
        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
    }
}
