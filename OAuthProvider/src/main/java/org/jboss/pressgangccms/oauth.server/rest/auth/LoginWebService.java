package org.jboss.pressgangccms.oauth.server.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.code.openid.AuthorizationHeaderBuilder;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.jboss.pressgangccms.oauth.server.data.model.auth.*;
import org.jboss.pressgangccms.oauth.server.oauth.login.OAuthIdRequest;
import org.jboss.pressgangccms.oauth.server.service.AuthService;
import org.jboss.pressgangccms.oauth.server.service.TokenIssuerService;

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
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static org.apache.amber.oauth2.as.response.OAuthASResponse.OAuthTokenResponseBuilder;
import static org.jboss.pressgangccms.oauth.server.rest.auth.OAuthUtil.*;
import static org.jboss.pressgangccms.oauth.server.util.Common.*;

/**
 * Serves as an endpoint to prompt OpenID login and an OAuth authorisation endpoint.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Path("/auth/login")
@RequestScoped
public class LoginWebService {

    @Inject
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuerService tokenIssuerService;

    @GET
    public Response login(@Context HttpServletRequest request) throws IOException, OAuthSystemException {
        log.info("Processing login request");

        try {
            OAuthIdRequest oauthRequest = new OAuthIdRequest(request);
            storeOAuthRequestParams(request, oauthRequest);
            String providerUrl = checkOpenIdProvider(request, oauthRequest);
            checkOAuthClient(oauthRequest);
            Response.ResponseBuilder builder = createAuthResponse(providerUrl);
            return builder.build();
        } catch (OAuthProblemException e) {

            log.warning("OAuthProblemException thrown: " + e.getMessage());

            final Response.ResponseBuilder responseBuilder = Response.status(HttpServletResponse.SC_NOT_FOUND);
            String redirectUri = e.getRedirectUri();

            if (OAuthUtils.isEmpty(redirectUri)) {
                throw new WebApplicationException(
                        responseBuilder.entity(OAUTH_CALLBACK_URL_REQUIRED).build());
            }
            return responseBuilder.entity(e.getError()).location(URI.create(redirectUri)).build();
        }
    }

    @POST
    public Response authorise(@Context HttpServletRequest request) throws URISyntaxException {
        log.info("Processing authorisation attempt");

        String identifier = (String) request.getAttribute(OPENID_IDENTIFIER);
        String clientId = getStringAttributeFromSessionAndRemove(request, log, OAuth.OAUTH_CLIENT_ID, "OAuth client id");
        String redirectUri = getStringAttributeFromSessionAndRemove(request, log, OAuth.OAUTH_REDIRECT_URI, "OAuth redirect URI");
        Set<String> scopes = getStringSetAttributeFromSessionAndRemove(request, log, OAuth.OAUTH_SCOPE, "Scopes requested");
        String providerUrl = getStringAttributeFromSessionAndRemove(request, log, OPENID_PROVIDER, "OpenId provider");

        try {
            if (identifier == null) {
                log.warning("No identity identifier received");
                throw OAuthProblemException.error(INVALID_IDENTIFIER);
            }

            log.info("User has been authenticated as: " + identifier);

            // Check redirect URI matches expectation
            if (isRedirectUriInvalid(clientId, redirectUri)) {
                log.warning("Invalid callback URI: " + redirectUri);
                throw OAuthProblemException.error(INVALID_CALLBACK_URI);
            }

            // Check if this is a known identity or new identity
            Optional<Identity> identityFound = authService.getIdentity(identifier);
            Identity identity;

            if (identityFound.isPresent()) {
                identity = identityFound.get();
                updateIdentity(providerUrl, identity, request);
            } else {
                identity = addNewIdentity(identifier, providerUrl, request);
            }

            // Check if this is part of a identity association request
            if (clientId.equals(OAUTH_PROVIDER_ID) && redirectUri.equals(ASSOCIATE_IDENTITY_ENDPOINT)) {
                request.getSession().setAttribute(OPENID_IDENTIFIER, identifier);
                StringBuilder uriBuilder = new StringBuilder(ASSOCIATE_IDENTITY_ENDPOINT);
                String accessToken = (String)request.getSession().getAttribute(OAuth.OAUTH_TOKEN);
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

            // Check if identity already has current grant/s; if so, make them invalid
            Set<TokenGrant> grants = identity.getTokenGrants();
            if (grants != null) {
                makeGrantsNonCurrent(authService, grants);
            }

            OAuthTokenResponseBuilder oAuthTokenResponseBuilder =
                    addTokenGrantResponseParams(createTokenGrant(clientId, scopes, identity), HttpServletResponse.SC_FOUND);
            OAuthResponse response = oAuthTokenResponseBuilder.location(redirectUri).buildQueryMessage();
            return Response.status(response.getResponseStatus()).location(new URI(response.getLocationUri())).build();
        } catch (OAuthProblemException e) {
            log.warning("OAuthProblemException thrown: " + e.getError());
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getError()).location(new URI(redirectUri)).build();
        } catch (OAuthSystemException e) {
            log.warning("OAuthSystemException thrown: " + e.getMessage());
            return Response.status(Response.Status.NOT_FOUND)
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
                .usingRealm(OPENID_REALM)
                .returnTo(OPENID_RETURN_URL)
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

    private String checkOpenIdProvider(HttpServletRequest request, OAuthIdRequest oAuthRequest)
            throws UnsupportedEncodingException, OAuthProblemException {
        String providerUrl = request.getParameter(OPENID_PROVIDER);
        String decodedProviderUrl = URLDecoder.decode(providerUrl, UTF_ENCODING);
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(decodedProviderUrl);
        if (providerFound.isPresent()) {
            request.getSession().setAttribute(OPENID_PROVIDER, decodedProviderUrl);
        } else {
            OAuthProblemException e = OAuthProblemException.error(INVALID_PROVIDER);
            e.setRedirectUri(oAuthRequest.getRedirectURI());
            throw e;
        }
        return providerUrl;
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
        User newUser = authService.createUnassociatedUser();
        newIdentity.setIdentifier(identifier);
        newIdentity.setIdentityScopes(newHashSet(authService.getDefaultScope()));
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(providerUrl);
        newIdentity.setOpenIdProvider(providerFound.get());
        newIdentity.setUser(newUser);
        // Set extra attributes if available
        setExtraIdentityAttributes(request, newIdentity);
        authService.addIdentity(newIdentity);
        newUser.setPrimaryIdentity(newIdentity);
        authService.updateUser(newUser);
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
        String firstName = (String) request.getAttribute(OPENID_FIRSTNAME);
        if (firstName != null) identity.setFirstName(firstName);
        String lastName = (String) request.getAttribute(OPENID_LASTNAME);
        if (lastName != null) identity.setLastName(lastName);
        String email = (String) request.getAttribute(OPENID_EMAIL);
        if (email != null) identity.setEmail(email);
        String language = (String) request.getAttribute(OPENID_LANGUAGE);
        if (language != null) identity.setLanguage(language);
        String country = (String) request.getAttribute(OPENID_COUNTRY);
        if (country != null) identity.setCountry(country);
    }

    private TokenGrant createTokenGrant(String clientId, Set<String> scopes, Identity identity)
            throws OAuthProblemException, OAuthSystemException {
        TokenGrant tokenGrant = createTokenGrantWithDefaults(tokenIssuerService, authService, identity,
                authService.getClient(clientId).get());
        // If specific grant scopes requested, check these are valid and add to token grant
        if (scopes != null) {
            Set<Scope> grantScopes = checkScopes(authService, scopes, identity.getIdentityScopes());
            tokenGrant.setGrantScopes(grantScopes);
        }
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