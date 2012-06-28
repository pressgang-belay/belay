package com.redhat.prototype.rest.auth;

import com.redhat.prototype.Common;
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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static com.redhat.prototype.Common.ONE_HOUR;

@Path("/auth/token")
@RequestScoped
public class TokenService {

    @Inject
    private Logger log;

//    @POST
//    @Consumes("application/x-www-form-urlencoded")
//    @Produces("application/json")
    @GET
    public Response authorize(@Context HttpServletRequest request) throws OAuthSystemException {

        OAuthTokenRequest oauthRequest = null;

        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

        try {
            oauthRequest = new OAuthTokenRequest(request);

            // Check if clientid is valid
            if (!Common.CLIENT_ID.equals(oauthRequest.getParam(OAuth.OAUTH_CLIENT_ID))) {
                log.severe("Client ID could not be found");
                OAuthResponse response =
                        OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                                .setError(OAuthError.TokenResponse.INVALID_CLIENT).setErrorDescription("client_id not found")
                                .buildJSONMessage();
                return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
            }

            // Do checking for different grant types
            // TODO Not allowing password grants or auth code grants - should perhaps put this code back in and throw error
//            if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
//                    .equals(GrantType.AUTHORIZATION_CODE.toString())) {
//                if (!Common.AUTHORIZATION_CODE.equals(oauthRequest.getParam(OAuth.OAUTH_CODE))) {
//                    //TODO implement check of valid auth code
//                    // Should check auth code matches client id + user id from auth code request too
//                    log.severe("Invalid auth code");
//                    OAuthResponse response = OAuthASResponse
//                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
//                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
//                            .setErrorDescription("invalid authorization code")
//                            .buildJSONMessage();
//                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
//                }
//            }

//            else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
//                    .equals(GrantType.PASSWORD.toString())) {
//                if (!Common.PASSWORD.equals(oauthRequest.getPassword())
//                        || !Common.USERNAME.equals(oauthRequest.getUsername())) {
//                    OAuthResponse response = OAuthASResponse
//                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
//                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
//                            .setErrorDescription("invalid username or password")
//                            .buildJSONMessage();
//                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
//                }
            else if (oauthRequest.getParam(OAuth.OAUTH_GRANT_TYPE)
                    .equals(GrantType.REFRESH_TOKEN.toString())) {
                if (refreshTokenInvalid(oauthRequest.getParam(OAuth.OAUTH_REFRESH_TOKEN))) {
                    log.severe("Invalid refresh token");
                    OAuthResponse response = OAuthASResponse
                            .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                            .setError(OAuthError.TokenResponse.INVALID_GRANT)
                            .setErrorDescription("Refresh token not allowed")
                            .buildJSONMessage();
                    return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
                } else {
                    //TODO what about expiring for refresh tokens?
                    //TODO store access token and refresh token against client and user ids
                    String accessToken = oauthIssuerImpl.accessToken();
                    log.info("Issuing access token: " + accessToken);
                    String refreshToken = oauthIssuerImpl.refreshToken();
                    log.info("Issuing refresh token: " + refreshToken);
                    OAuthResponse response = OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK)
                            .setAccessToken(oauthIssuerImpl.accessToken())
                            .setExpiresIn(ONE_HOUR)
                            .setRefreshToken(oauthIssuerImpl.refreshToken())
                            .buildQueryMessage();
                    return Response.status(response.getResponseStatus()).build();
                }
            }

            OAuthResponse response = OAuthASResponse
                    .errorResponse(HttpServletResponse.SC_BAD_REQUEST)
                    .setError(OAuthError.TokenResponse.INVALID_GRANT)
                    .setErrorDescription("Cannot process request")
                    .buildJSONMessage();
            return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
        } catch (OAuthProblemException e) {
            OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_BAD_REQUEST).error(e)
                    .buildJSONMessage();
            return Response.status(res.getResponseStatus()).entity(res.getBody()).build();
        }
    }

    //TODO implement
    private boolean refreshTokenInvalid(String refreshToken) {
        return false;
    }

//    @GET
//    @Consumes("application/x-www-form-urlencoded")
//    @Produces("application/json")
//    public Response authorizeGet(@Context HttpServletRequest request) throws OAuthSystemException {
//        OAuthIssuer oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
//
//        OAuthResponse response = OAuthASResponse
//                .tokenResponse(HttpServletResponse.SC_OK)
//                .setAccessToken(oauthIssuerImpl.accessToken())
//                .setExpiresIn("3600")
//                .buildJSONMessage();
//
//        return Response.status(response.getResponseStatus()).entity(response.getBody()).build();
//    }
}
