package com.redhat.prototype.rest.auth;

import com.google.code.openid.AuthorizationHeaderBuilder;
import com.redhat.prototype.oauth.login.OAuthIdRequest;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.utils.OAuthUtils;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.logging.Logger;

import static com.redhat.prototype.Common.ONE_HOUR;

@Path("/auth/login")
@RequestScoped
public class LoginService {

    @Inject
    private Logger log;

    @GET
    public Response login(@Context HttpServletRequest request) throws IOException, URISyntaxException, OAuthSystemException {

        log.info("Processing login request");

        try {
            OAuthIdRequest oauthRequest = new OAuthIdRequest(request);

            // Store OAuth request params in the user's session
            request.getSession().setAttribute(OAuth.OAUTH_CLIENT_ID, oauthRequest.getClientId());
            request.getSession().setAttribute(OAuth.OAUTH_REDIRECT_URI, oauthRequest.getRedirectURI());
            if (oauthRequest.getScopes() != null) {
                request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oauthRequest.getScopes());
            }

            // Get user to log into OpenId provider
            Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
            log.info("Sending request for login authentication");
            builder.header("WWW-Authenticate", new AuthorizationHeaderBuilder()
                    .forIdentifier(request.getParameter("provider"))
                    .includeStandardAttributes()
                    .buildHeader());
            return builder.build();
        } catch (OAuthProblemException e) {

            log.severe("OAuthProblemException thrown: " + e.getMessage());

            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);
            String redirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(redirectUri)) {
                throw new WebApplicationException(
                        responseBuilder.entity("OAuth callback URL needs to be provided by client").build());
            }
            final OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e)
                    .location(redirectUri).buildQueryMessage();
            final URI location = new URI(response.getLocationUri());
            return responseBuilder.location(location).build();
        }
    }

    @POST
    public Response authorise(@Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException {

        log.info("Processing authorisation attempt");

        OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        String identifier = (String) request.getAttribute("openid.identifier");

        if (identifier != null) {
            log.info("User has been authenticated as: " + identifier);
            String clientId = (String) request.getSession().getAttribute(OAuth.OAUTH_CLIENT_ID);
            request.getSession().removeAttribute(OAuth.OAUTH_CLIENT_ID);
            log.info("Authenticated user came from client: " + clientId);
            String redirectUri = (String) request.getSession().getAttribute(OAuth.OAUTH_REDIRECT_URI);
            request.getSession().removeAttribute(OAuth.OAUTH_REDIRECT_URI);
            log.info("Redirect URI supplied is: " + redirectUri);
            HashSet<String> scopes = (HashSet<String>) request.getSession().getAttribute(OAuth.OAUTH_SCOPE);
            request.getSession().removeAttribute(OAuth.OAUTH_SCOPE);
            log.info("User scopes requested are: " + scopes);

            OAuthASResponse.OAuthTokenResponseBuilder builder = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_FOUND);

            //TODO store access token and refresh token against client and user ids
            //TODO check client id is valid
            String accessToken = oauthIssuerImpl.accessToken();
            log.info("Issuing access token: " + accessToken + " to: " + identifier);
            String refreshToken = oauthIssuerImpl.refreshToken();
            log.info("Issuing refresh token: " + refreshToken);
            builder.setRefreshToken(refreshToken);
            builder.setAccessToken(accessToken);
            builder.setExpiresIn(ONE_HOUR);
            log.info("Access token expires in: " + ONE_HOUR);

            final OAuthResponse response = builder.location(redirectUri).buildQueryMessage();
            URI url = new URI(response.getLocationUri());
            log.info("Location URI: " + url);
            return Response.status(response.getResponseStatus()).location(url).build();

        } else {
            log.info("No identifier received");
            Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
            return builder.build();
        }
    }
}