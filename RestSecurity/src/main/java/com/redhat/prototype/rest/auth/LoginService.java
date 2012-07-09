package com.redhat.prototype.rest.auth;

import com.google.code.openid.AuthorizationHeaderBuilder;
import com.redhat.prototype.model.auth.*;
import com.redhat.prototype.oauth.login.OAuthIdRequest;
import com.redhat.prototype.service.AuthService;
import org.apache.amber.oauth2.as.issuer.MD5Generator;
import org.apache.amber.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
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
import java.net.URLDecoder;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static com.redhat.prototype.Common.*;

@Path("/auth/login")
@RequestScoped
public class LoginService {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

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

            // Check that the OpenId provider is known to us
            String providerUrl = request.getParameter(OPENID_PROVIDER);
            String decodedProviderUrl = URLDecoder.decode(providerUrl, "UTF-8");
            OpenIdProvider provider = authService.getOpenIdProvider(decodedProviderUrl);
            if (provider == null) {
                OAuthProblemException e = OAuthProblemException.error(INVALID_PROVIDER);
                e.setRedirectUri(oauthRequest.getRedirectURI());
                throw e;
            } else {
                request.getSession().setAttribute(OPENID_PROVIDER, decodedProviderUrl);
            }

            // Check that the client is known to us
            String clientId = oauthRequest.getClientId();
            ClientApplication client = authService.getClient(clientId);
            if (client == null) {
                OAuthProblemException e = OAuthProblemException.error(OAuthError.TokenResponse.INVALID_CLIENT);
                e.setRedirectUri(oauthRequest.getRedirectURI());
                throw e;
            }

            // Get user to log into OpenId provider
            Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
            log.info("Sending request for login authentication");
            builder.header(AUTHENTICATE_HEADER, new AuthorizationHeaderBuilder()
                    .forIdentifier(providerUrl)
                    .includeStandardAttributes()
                    .buildHeader());
            return builder.build();
        } catch (OAuthProblemException e) {

            log.info("OAuthProblemException thrown: " + e.getMessage());

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
    public Response authorise(@Context HttpServletRequest request) throws URISyntaxException, OAuthSystemException,
            OAuthProblemException {

        log.info("Processing authorisation attempt");

        OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        String identifier = (String) request.getAttribute(OPENID_IDENTIFIER);

        if (identifier != null) {
            log.info("User has been authenticated as: " + identifier);
            String clientId = (String) request.getSession().getAttribute(OAuth.OAUTH_CLIENT_ID);
            request.getSession().removeAttribute(OAuth.OAUTH_CLIENT_ID);
            log.info("Authenticated user came from client: " + clientId);
            String redirectUri = (String) request.getSession().getAttribute(OAuth.OAUTH_REDIRECT_URI);
            request.getSession().removeAttribute(OAuth.OAUTH_REDIRECT_URI);
            log.info("Redirect URI supplied is: " + redirectUri);
            Set<String> scopes = (Set<String>) request.getSession().getAttribute(OAuth.OAUTH_SCOPE);
            request.getSession().removeAttribute(OAuth.OAUTH_SCOPE);
            log.info("User scopes requested are: " + scopes);
            String providerUrl = (String) request.getSession().getAttribute(OPENID_PROVIDER);
            request.getSession().removeAttribute(OPENID_PROVIDER);

            // Check if this is a known user or new user
            User user = authService.getUser(identifier);

            if (user == null) {
                // New user, so add them to database
                log.info("Creating new user");
                User newUser = new User();
                newUser.setUserIdentifier(identifier);
                newUser.setUserScopes(newHashSet(authService.getDefaultScope()));
                OpenIdProvider provider = authService.getOpenIdProvider(providerUrl);
                log.info("OpenId provider is: " + provider.getProviderName());
                log.info("OpenId provider has id: " + provider.getProviderId());
                newUser.setOpenIdProvider(provider);
                //TODO add other user attributes if available
                authService.registerUser(newUser);
                user = newUser;
            } else {
                // User exists, but make sure all details match and update any that don't
                log.info("Updating existing user");
                if (user.getOpenIdProvider() != authService.getOpenIdProvider(providerUrl)
                        && authService.getOpenIdProvider(providerUrl) != null) {
                    user.setOpenIdProvider(authService.getOpenIdProvider(providerUrl));
                }
                //TODO update other attributes
                authService.updateUser(user);
            }

            OAuthASResponse.OAuthTokenResponseBuilder builder = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_FOUND);

            //TODO Add check for redirect_uri - store this as client attribute?

            //TODO check if this user already has a token grant; if so, cancel old refresh token?

            TokenGrant tokenGrant = new TokenGrant();
            tokenGrant.setGrantUser(user);
            tokenGrant.setGrantClient(authService.getClient(clientId));
            // If specific grant scopes requested, check these are valid and add to token grant
            if (scopes != null) {
                Set<Scope> userScopes = user.getUserScopes();
                Set<Scope> requestedScopes = newHashSet(authService.getDefaultScope());
                for (String scopeName : scopes) {
                    Scope scope = authService.getScopeByName(scopeName);
                    if (scope == null || (! userScopes.contains(scope))) {
                        throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_SCOPE);
                    } else {
                        requestedScopes.add(scope);
                    }
                }
                tokenGrant.setGrantScopes(requestedScopes);
            } else {
                // Set default scope
                tokenGrant.setGrantScopes(newHashSet(authService.getDefaultScope()));
            }
            tokenGrant.setAccessToken(oauthIssuerImpl.accessToken());
            tokenGrant.setRefreshToken(oauthIssuerImpl.refreshToken());
            tokenGrant.setAccessTokenExpiry(ONE_HOUR);
            tokenGrant.setGrantTimeStamp(new Date());
            authService.addGrant(tokenGrant);
            log.info("Issuing access token: " + tokenGrant.getAccessToken() + " to: " + identifier);
            log.info("Issuing refresh token: " + tokenGrant.getRefreshToken());
            log.info("Access token expires in: " + tokenGrant.getAccessTokenExpiry());
            builder.setAccessToken(tokenGrant.getAccessToken());
            builder.setRefreshToken(tokenGrant.getRefreshToken());
            builder.setExpiresIn(tokenGrant.getAccessTokenExpiry());

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