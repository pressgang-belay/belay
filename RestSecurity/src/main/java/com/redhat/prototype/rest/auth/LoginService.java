package com.redhat.prototype.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.code.openid.AuthorizationHeaderBuilder;
import com.redhat.prototype.data.model.auth.*;
import com.redhat.prototype.oauth.login.OAuthIdRequest;
import com.redhat.prototype.service.AuthService;
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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static com.redhat.prototype.util.Common.*;

@Path("/auth/login")
@RequestScoped
public class LoginService {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @GET
    public Response login(@Context HttpServletRequest request) throws IOException, URISyntaxException,
            OAuthSystemException {

        log.info("Processing login request");

        try {
            OAuthIdRequest oauthRequest = new OAuthIdRequest(request);
            storeOAuthRequestParams(request, oauthRequest);
            String providerUrl = checkOpenIdProvider(request, oauthRequest);
            checkOAuthClient(oauthRequest);
            Response.ResponseBuilder builder = createAuthResponse(providerUrl);
            return builder.build();
        } catch (OAuthProblemException e) {

            log.info("OAuthProblemException thrown: " + e.getMessage());

            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_FOUND);
            String redirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(redirectUri)) {
                throw new WebApplicationException(
                        responseBuilder.entity(OAUTH_CALLBACK_URL_REQUIRED).build());
            }
            return responseBuilder.entity(e.getError()).location(new URI(redirectUri)).build();
        }
    }

    @POST
    public Response authorise(@Context HttpServletRequest request) throws URISyntaxException {
        log.info("Processing authorisation attempt");

        OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());
        String identifier = (String) request.getAttribute(OPENID_IDENTIFIER);
        String clientId = getClientIdFromSession(request, identifier);
        String redirectUri = getRedirectUriFromSession(request);
        Set<String> scopes = getScopesFromSession(request);
        String providerUrl = getOpenIdProviderFromSession(request);

        try {
        if (identifier == null) {
            log.info("No user identifier received");
            throw OAuthProblemException.error(INVALID_USER_IDENTIFIER);
        }

        log.info("User has been authenticated as: " + identifier);

        // Check redirect URI matches expectation
        if (isRedirectUriInvalid(clientId, redirectUri)) {
            log.info("Invalid callback URI: " + redirectUri);
            throw OAuthProblemException.error(INVALID_CALLBACK_URI);
        }

        // Check if this is a known user or new user
        Optional<User> userFound = authService.getUser(identifier);
        User user;

        if (userFound.isPresent()) {
            user = userFound.get();
            updateUser(providerUrl, user, request);
        } else {
            user = addNewUser(identifier, providerUrl, request);
        }

        OAuthASResponse.OAuthTokenResponseBuilder builder = OAuthASResponse
                .tokenResponse(HttpServletResponse.SC_FOUND);

        //TODO check if this user already has a token grant; if so, cancel old refresh token?

        TokenGrant tokenGrant = createTokenGrant(oauthIssuerImpl, clientId, scopes, user);
        log.info("Issuing access token: " + tokenGrant.getAccessToken() + " to " + identifier);
        builder.setAccessToken(tokenGrant.getAccessToken());
        log.info("Issuing refresh token: " + tokenGrant.getRefreshToken());
        builder.setRefreshToken(tokenGrant.getRefreshToken());
        log.info("Access token expires in: " + tokenGrant.getAccessTokenExpiry());
        builder.setExpiresIn(tokenGrant.getAccessTokenExpiry());

        final OAuthResponse response = builder.location(redirectUri).buildQueryMessage();
        return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
        } catch (OAuthProblemException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getError()).location(new URI(redirectUri)).build();
        } catch (OAuthSystemException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(e.getMessage()).location(new URI(redirectUri)).build();
        }
    }

    private boolean isRedirectUriInvalid(String clientId, String redirectUri) {
        log.info("Checking redirect URI: " + redirectUri);
        try {
            Optional<ClientApplication> clientFound = authService.getClient(clientId);
            if ((clientNotFound(clientFound))
                    || clientRedirectUriDoesNotMatch(URLDecoder.decode(redirectUri, UTF_ENCODING), clientFound)) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            log.severe("Redirect URI decoding failed");
            return true;
        }
        return false;
    }

    private Response.ResponseBuilder createAuthResponse(String providerUrl) {
        Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
        log.info("Sending request for login authentication");
        builder.header(AUTHENTICATE_HEADER, new AuthorizationHeaderBuilder()
                .forIdentifier(providerUrl)
                .includeStandardAttributes()
                .buildHeader());
        return builder;
    }

    private void checkOAuthClient(OAuthIdRequest oauthRequest) throws OAuthProblemException {
        String clientId = oauthRequest.getClientId();
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        if (clientNotFound(clientFound)) {
            OAuthProblemException e = OAuthProblemException.error(INVALID_CLIENT);
            e.setRedirectUri(oauthRequest.getRedirectURI());
            throw e;
        }
    }

    private String checkOpenIdProvider(HttpServletRequest request, OAuthIdRequest oauthRequest)
            throws UnsupportedEncodingException, OAuthProblemException {
        String providerUrl = request.getParameter(OPENID_PROVIDER);
        String decodedProviderUrl = URLDecoder.decode(providerUrl, UTF_ENCODING);
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(decodedProviderUrl);
        if (providerFound.isPresent()) {
            request.getSession().setAttribute(OPENID_PROVIDER, decodedProviderUrl);
        } else {
            OAuthProblemException e = OAuthProblemException.error(INVALID_PROVIDER);
            e.setRedirectUri(oauthRequest.getRedirectURI());
            throw e;
        }
        return providerUrl;
    }

    private void storeOAuthRequestParams(HttpServletRequest request, OAuthIdRequest oauthRequest) {
        request.getSession().setAttribute(OAuth.OAUTH_CLIENT_ID, oauthRequest.getClientId());
        request.getSession().setAttribute(OAuth.OAUTH_REDIRECT_URI, oauthRequest.getRedirectURI());
        if (oauthRequest.getScopes() != null) {
            request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oauthRequest.getScopes());
        }
    }

    private String getOpenIdProviderFromSession(HttpServletRequest request) {
        String providerUrl = (String) request.getSession().getAttribute(OPENID_PROVIDER);
        request.getSession().removeAttribute(OPENID_PROVIDER);
        return providerUrl;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getScopesFromSession(HttpServletRequest request) {
        Set<String> scopes = (Set<String>) request.getSession().getAttribute(OAuth.OAUTH_SCOPE);
        log.info("User scopes requested are: " + scopes);
        request.getSession().removeAttribute(OAuth.OAUTH_SCOPE);
        return scopes;
    }

    private String getRedirectUriFromSession(HttpServletRequest request) {
        String redirectUri = (String) request.getSession().getAttribute(OAuth.OAUTH_REDIRECT_URI);
        log.info("Redirect URI supplied is: " + redirectUri);
        request.getSession().removeAttribute(OAuth.OAUTH_REDIRECT_URI);
        return redirectUri;
    }

    private String getClientIdFromSession(HttpServletRequest request, String identifier) {
        String clientId = (String) request.getSession().getAttribute(OAuth.OAUTH_CLIENT_ID);
        log.info("Authenticated user came from client: " + clientId);
        request.getSession().removeAttribute(OAuth.OAUTH_CLIENT_ID);
        return clientId;
    }

    private User addNewUser(String identifier, String providerUrl, HttpServletRequest request) {
        log.info("Creating new user");
        User newUser = new User();
        newUser.setUserIdentifier(identifier);
        newUser.setUserScopes(newHashSet(authService.getDefaultScope()));
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(providerUrl);
        newUser.setOpenIdProvider(providerFound.get());
        // Set extra attributes if available
        setExtraUserAttributes(request, newUser);
        authService.addUser(newUser);
        return newUser;
    }

    private void updateUser(String providerUrl, User user, HttpServletRequest request) {
        // Update any changed user details
        log.info("Updating existing user");
        if (user.getOpenIdProvider() != authService.getOpenIdProvider(providerUrl).get()
                && authService.getOpenIdProvider(providerUrl) != null) {
            user.setOpenIdProvider(authService.getOpenIdProvider(providerUrl).get());
        }
        setExtraUserAttributes(request, user);
        authService.updateUser(user);
    }

    private void setExtraUserAttributes(HttpServletRequest request, User user) {
        String firstName = (String)request.getAttribute(OPENID_FIRSTNAME);
        if (firstName != null) user.setFirstName(firstName);
        String lastName = (String)request.getAttribute(OPENID_LASTNAME);
        if (lastName != null) user.setLastName(lastName);
        String email = (String)request.getAttribute(OPENID_EMAIL);
        if (email != null) user.setEmail(email);
        String country = (String)request.getAttribute(OPENID_COUNTRY);
        if (country != null) user.setCountry(country);
    }

    private TokenGrant createTokenGrant(OAuthIssuerImpl oauthIssuerImpl, String clientId, Set<String> scopes, User user)
            throws OAuthProblemException, OAuthSystemException {
        TokenGrant tokenGrant = new TokenGrant();
        tokenGrant.setGrantUser(user);
        tokenGrant.setGrantClient(authService.getClient(clientId).get());
        // If specific grant scopes requested, check these are valid and add to token grant
        if (scopes != null) {
            Set<Scope> userScopes = user.getUserScopes();
            Set<Scope> requestedScopes = newHashSet(authService.getDefaultScope());
            for (String scopeName : scopes) {
                Optional<Scope> scopeFound = authService.getScopeByName(scopeName);
                if ((!scopeFound.isPresent()) || (!userScopes.contains(scopeFound.get()))) {
                    throw OAuthProblemException.error(INVALID_SCOPE);
                } else {
                    requestedScopes.add(scopeFound.get());
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
        return tokenGrant;
    }

    private boolean clientNotFound(Optional<ClientApplication> clientFound) {
        return !clientFound.isPresent();
    }

    private boolean clientRedirectUriDoesNotMatch(String decodedRedirectUri, Optional<ClientApplication> clientFound) {
        return !clientFound.get().getClientRedirectUri().equals(decodedRedirectUri);
    }
}