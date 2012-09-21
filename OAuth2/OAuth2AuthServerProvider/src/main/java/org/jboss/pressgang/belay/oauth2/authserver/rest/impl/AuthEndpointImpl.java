package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.code.openid.AuthorizationHeaderBuilder;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.ClientApplication;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.OpenIdProvider;
import org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.AuthEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import static java.net.URLDecoder.decode;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * OAuth authorization endpoint using OpenID to authenticate end-users. The endpoint can be used for OAuth2's implicit
 * authorization flow, if the client is public, or the authorization code flow if the client is confidential and has a
 * client secret. If used for confidential clients, basic authentication must be required to access the endpoint. Both
 * flows must require TLS.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AuthEndpointImpl implements AuthEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    /**
     * Endpoint for OAuth2 authorization requests, using OpenID for authentication. Required OAuth2 parameters, which
     * may be supplied query or header style, are:
     * response_type: OAuth2 response type, token for public clients and code for confidential clients
     * client_id: OAuth2 client identifier, supplied by OAuth2 Auth Server
     * redirect_uri: URI to redirect to when auth complete; must be registered with the Auth Server
     * provider: the URL or domain of the OpenID provider with which to authenticate the end-user
     *
     * Confidential clients may also supply a state parameter.
     *
     * @return
     */
    @Override
    public Response requestAuthenticationWithOpenId(@Context HttpServletRequest request) {
        log.info("Processing authentication request");
        try {
            OAuthIdRequest oauthRequest = new OAuthIdRequest(request);
            boolean isAuthorized = request.getAuthType() != null;
            boolean isPublicClient = checkOAuthClientAndRedirectUri(oauthRequest.getClientId(), oauthRequest.getRedirectURI());
            checkAuthorization(isAuthorized, isPublicClient, oauthRequest.getRedirectURI());
            storeOAuthRequestParams(request, oauthRequest);
            String providerUrl = checkOpenIdProvider(request, oauthRequest);
            Response.ResponseBuilder builder = createAuthResponse(request, providerUrl);
            return builder.build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, null, null);
        }
    }

    private Response.ResponseBuilder createAuthResponse(HttpServletRequest request, String providerUrl) {
        Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
        log.info("Sending request for OpenID authentication");
        String baseUrl = buildBaseUrl(request);
        AuthorizationHeaderBuilder authHeaderBuilder = new AuthorizationHeaderBuilder()
                .forIdentifier(providerUrl)
                .usingRealm(baseUrl + openIdRealm)
                .returnTo(baseUrl + openIdReturnUri)
                .includeStandardAttributes();
        addRequiredAttributes(authHeaderBuilder);
        String authHeader = authHeaderBuilder.buildHeader();
        log.fine("Request auth header: " + authHeader);
        builder.header(AUTHENTICATE_HEADER, authHeader);
        return builder;
    }

    private void addRequiredAttributes(AuthorizationHeaderBuilder authHeaderBuilder) {
        authHeaderBuilder.requireAttribute(FULLNAME, "http://axschema.org/namePerson");
        authHeaderBuilder.requireAttribute(FULLNAME_TITLE_CASE, "http://axschema.org/namePerson");
    }

    // Returns true if this is a public client, or false otherwise
    private boolean checkOAuthClientAndRedirectUri(String clientId, String redirectUri) throws OAuthProblemException {
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        String error;
        try {
            if (!clientFound.isPresent()) {
                log.warning("Invalid OAuth2 client with id '" + clientId + "' in login request");
                error = INVALID_CLIENT;
            } else if (!clientRedirectUriMatches(decode(redirectUri, urlEncoding), clientFound.get())) {
                log.warning("Invalid OAuth2 redirect URI in login request: " + decode(redirectUri, urlEncoding));
                error = INVALID_REDIRECT_URI;
            } else {
                return clientFound.get().getClientSecret() == null || clientFound.get().getClientSecret().isEmpty();
            }
        } catch (UnsupportedEncodingException e) {
            log.severe("Error during URL decoding: " + e);
            error = URL_DECODING_ERROR;
        }
        throw createOAuthProblemException(error, null);
    }

    private String checkOpenIdProvider(HttpServletRequest request, OAuthIdRequest oAuthRequest)
            throws OAuthProblemException {
        String providerUrl = request.getParameter(OPENID_PROVIDER);
        String decodedProviderUrl;
        try {
            decodedProviderUrl = decode(providerUrl, urlEncoding);
        } catch (UnsupportedEncodingException e) {
            log.severe("Could not decode provider URL: " + providerUrl);
            throw createOAuthProblemException(URL_DECODING_ERROR, oAuthRequest.getRedirectURI());
        }
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(decodedProviderUrl);
        if ((!providerFound.isPresent()) && getPossibleDomains(decodedProviderUrl).isPresent()) {
            for (String domain : getPossibleDomains(decodedProviderUrl).get()) {
                providerFound = authService.getOpenIdProvider(domain);
                if (providerFound.isPresent()) break;
            }
        }
        if (providerFound.isPresent()) {
            request.getSession().setAttribute(OPENID_PROVIDER, providerFound.get().getProviderUrl());
        } else {
            log.warning("Invalid OpenID provider: " + providerUrl);
            throw OAuthEndpointUtil.createOAuthProblemException(INVALID_PROVIDER, oAuthRequest.getRedirectURI());
        }
        return providerUrl;
    }

    private Optional<List<String>> getPossibleDomains(String providerUrl) {
        try {
            URL url = new URL(providerUrl);
            List<String> possibleDomains = Lists.newArrayList(url.getHost());
            if (url.getHost().split("\\.").length > 2) {
                // Cut off the first part, which may be a user identifier
                possibleDomains.add(url.getHost().substring(url.getHost().indexOf('.') + 1));
            }
            return Optional.of(possibleDomains);
        } catch (MalformedURLException e) {
            // Do nothing
        }
        return Optional.absent();
    }

    private void storeOAuthRequestParams(HttpServletRequest request, OAuthIdRequest oAuthRequest) {
        request.getSession().setAttribute(OAuth.OAUTH_CLIENT_ID, oAuthRequest.getClientId());
        request.getSession().setAttribute(OAuth.OAUTH_REDIRECT_URI, oAuthRequest.getRedirectURI());
        if (oAuthRequest.getScopes() != null) {
            request.getSession().setAttribute(OAuth.OAUTH_SCOPE, oAuthRequest.getScopes());
        }
        if (oAuthRequest.getState() != null) {
            request.getSession().setAttribute(ORIGINAL_REQUEST, request);
        }
    }

    private boolean clientRedirectUriMatches(String decodedRedirectUri, ClientApplication client) {
        return client.getClientRedirectUri().equals(decodedRedirectUri);
    }
}