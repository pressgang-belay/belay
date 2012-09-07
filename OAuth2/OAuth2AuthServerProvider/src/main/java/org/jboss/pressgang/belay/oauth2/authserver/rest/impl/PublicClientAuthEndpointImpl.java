package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.code.openid.AuthorizationHeaderBuilder;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.PublicClientAuthEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuerService;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Lists.newArrayList;
import static java.net.URLDecoder.decode;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthTokenResponseBuilder;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * Serves as an endpoint to prompt OpenID login and an OAuth authorisation endpoint. The endpoint's functionality
 * is closest to OAuth2's implicit authorisation flow, however, depending on the app settings the resource owner may
 * not be prompted to authorise the access, as in cases where the client application, authorisation server and resource
 * server are all operating as one application, this may be inappropriate. This endpoint is for use by public clients.
 *
 * //TODO add Authorisation Code flow for confidential clients
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public abstract class PublicClientAuthEndpointImpl implements PublicClientAuthEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuerService tokenIssuerService;

    @Override
    public Response requestAuthenticationWithOpenId(@Context HttpServletRequest request) {
        log.info("Processing authentication request");
        try {
            OAuthIdRequest oauthRequest = new OAuthIdRequest(request);
            checkOAuthClientAndRedirectUri(oauthRequest.getClientId(), oauthRequest.getRedirectURI());
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

    @Override
    public Response authorise(@Context HttpServletRequest request) {
        log.info("Processing authorisation attempt");

        String openIdClaimedId = (String) request.getAttribute(OPENID_CLAIMED_ID);
        String openIdIdentifier = (String) request.getAttribute(OPENID_IDENTIFIER);
        String identifier = (openIdClaimedId != null) ? openIdClaimedId : openIdIdentifier;
        String clientId = getStringAttributeFromSessionAndRemove(request, log, OAuth.OAUTH_CLIENT_ID, "OAuth client id");
        String redirectUri = getStringAttributeFromSessionAndRemove(request, log, OAuth.OAUTH_REDIRECT_URI, "OAuth redirect URI");
        Set<String> scopes = getStringSetAttributeFromSessionAndRemove(request, log, OAuth.OAUTH_SCOPE, "Scopes requested");
        String providerUrl = getStringAttributeFromSessionAndRemove(request, log, OPENID_PROVIDER, "OpenId provider");

        try {
            if (clientId == null || redirectUri == null || providerUrl == null) {
                throw createOAuthProblemException(INVALID_SESSION, null);
            }
            if (identifier == null) {
                log.warning("No OpenID identifier received");
                throw createOAuthProblemException(INVALID_IDENTIFIER, redirectUri);
            }
            log.info("User has been authenticated as: " + identifier);

            // Check if this is a known identity or new identity
            Optional<Identity> identityFound = authService.getIdentity(identifier);
            Identity identity;

            if (identityFound.isPresent()) {
                identity = identityFound.get();
                updateIdentity(providerUrl, identity, request);
            } else {
                identity = addNewIdentity(identifier, providerUrl, request);
            }

            // Check if this is part of an identity association request
            if (clientId.equals(authServerOAuthClientId) && redirectUri.equals(completeAssociationEndpoint)) {
                return createAssociationRequestResponse(request, identifier);
            }

            //TODO add logic to prompt user to accept client app request to access resource with scopes
            // based on whether or not property is set

            // Check if identity already has current grant/s; if so, make them invalid
            Set<TokenGrant> grants = identity.getTokenGrants();
            if (grants != null) {
                makeGrantsNonCurrent(authService, grants);
            }

            OAuthTokenResponseBuilder oAuthTokenResponseBuilder =
                    addTokenGrantResponseParams(createTokenGrant(clientId, scopes, identity, redirectUri, false), SC_FOUND);
            OAuthResponse response = oAuthTokenResponseBuilder.location(redirectUri).buildQueryMessage();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, redirectUri, SERVER_ERROR);
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

    private TokenGrant createTokenGrant(String clientId, Set<String> scopes, Identity identity, String redirectUri,
                                        boolean issueRefreshToken)
            throws OAuthProblemException, OAuthSystemException {
        TokenGrant tokenGrant = OAuthEndpointUtil.createTokenGrantWithDefaults(tokenIssuerService, authService, identity,
                authService.getClient(clientId).get(), issueRefreshToken);
        // If specific grant scopes requested, check these are valid and add to token grant
        if (scopes != null) {
            Set<Scope> grantScopes = OAuthEndpointUtil.checkScopes(authService, scopes, identity.getIdentityScopes(), redirectUri);
            tokenGrant.setGrantScopes(grantScopes);
        }
        authService.addGrant(tokenGrant);
        return tokenGrant;
    }

    private void checkOAuthClientAndRedirectUri(String clientId, String redirectUri) throws OAuthProblemException {
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        String error;
        try {
            if (! clientFound.isPresent()) {
                log.warning("Invalid OAuth2 client with id '" + clientId + "' in login request");
                error = INVALID_CLIENT;
            } else if (! clientRedirectUriMatches(decode(redirectUri, urlEncoding), clientFound.get())) {
                log.warning("Invalid OAuth2 redirect URI in login request: " + decode(redirectUri, urlEncoding));
                error = INVALID_REDIRECT_URI;
            } else {
                return;
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
            throw OAuthEndpointUtil.createOAuthProblemException(URL_DECODING_ERROR, oAuthRequest.getRedirectURI());
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
    }

    private Identity addNewIdentity(String identifier, String providerUrl, HttpServletRequest request) {
        log.info("Creating new identity and associated user");
        Identity newIdentity = new Identity();
        newIdentity.setIdentifier(identifier);
        newIdentity.setIdentityScopes(newHashSet(authService.getDefaultScope()));
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(providerUrl);
        newIdentity.setOpenIdProvider(providerFound.get());
        // Set extra attributes if available
        setExtraIdentityAttributes(request, newIdentity);
        authService.addIdentity(newIdentity);
        User newUser = authService.createUnassociatedUser();
        newIdentity.setUser(newUser);
        newUser.setPrimaryIdentity(newIdentity);
        authService.updateIdentity(newIdentity);
        return newIdentity;
    }

    private void updateIdentity(String providerUrl, Identity identity, HttpServletRequest request) {
        // Update any changed identity details
        log.info("Updating existing identity");
        if (identity.getOpenIdProvider() != authService.getOpenIdProvider(providerUrl).get()
                && authService.getOpenIdProvider(providerUrl) != null) {
            identity.setOpenIdProvider(authService.getOpenIdProvider(providerUrl).get());
        }
        setExtraIdentityAttributes(request, identity);
        authService.updateIdentity(identity);
    }

    private void setExtraIdentityAttributes(HttpServletRequest request, Identity identity) {
        String firstName = getOpenIdAttribute(request, newArrayList(FIRSTNAME));
        if (firstName != null) identity.setFirstName(firstName);
        String lastName = getOpenIdAttribute(request, newArrayList(LASTNAME));
        if (lastName != null) identity.setLastName(lastName);
        String fullName = getOpenIdAttribute(request, newArrayList(FULLNAME, FULLNAME_TITLE_CASE));
        if (fullName != null) identity.setFullName(fullName);
        String email = getOpenIdAttribute(request, newArrayList(EMAIL));
        if (email != null) identity.setEmail(email);
        String language = getOpenIdAttribute(request, newArrayList(LANGUAGE));
        if (language != null) identity.setLanguage(language);
        String country = getOpenIdAttribute(request, newArrayList(COUNTRY));
        if (country != null) identity.setCountry(country);
    }

    private String getOpenIdAttribute(HttpServletRequest request, List<String> openIdAttributeNames) {
        List<String> prefixes = Lists.newArrayList(OPENID_AX_PREFIX, OPENID_EXT_PREFIX);
        for (String prefix : prefixes) {
            for (String attributeName : openIdAttributeNames) {
                if (request.getAttribute(prefix + attributeName) != null) {
                    log.info("Found " + prefix + attributeName + " attribute");
                    return (String) request.getAttribute(prefix + attributeName);
                }
            }
        }
        return null;
    }

    private boolean clientRedirectUriMatches(String decodedRedirectUri, ClientApplication client) {
        return client.getClientRedirectUri().equals(decodedRedirectUri);
    }

    private Response createAssociationRequestResponse(HttpServletRequest request, String identifier) {
        request.getSession().setAttribute(OPENID_IDENTIFIER, identifier);
        StringBuilder uriBuilder = new StringBuilder(completeAssociationEndpoint);
        String accessToken = (String) request.getSession().getAttribute(OAuth.OAUTH_TOKEN);
        if (accessToken != null) {
            log.info("Setting OAuth token for redirect");
            uriBuilder.append(QUERY_STRING_MARKER)
                    .append(OAuth.OAUTH_TOKEN)
                    .append(KEY_VALUE_SEPARATOR)
                    .append(accessToken);
        }
        log.info("Redirecting back to: " + uriBuilder.toString());
        return Response.seeOther(URI.create(uriBuilder.toString())).build();
    }
}