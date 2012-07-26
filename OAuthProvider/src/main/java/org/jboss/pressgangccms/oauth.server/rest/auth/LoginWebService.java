package org.jboss.pressgangccms.oauth.server.rest.auth;

import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.code.openid.AuthorizationHeaderBuilder;
import com.google.common.collect.Lists;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Lists.newArrayList;
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
    public Response login(@Context HttpServletRequest request) {
        log.info("Processing login request");
        try {
            OAuthIdRequest oauthRequest = new OAuthIdRequest(request);
            storeOAuthRequestParams(request, oauthRequest);
            String providerUrl = checkOpenIdProvider(request, oauthRequest);
            checkOAuthClient(oauthRequest);
            Response.ResponseBuilder builder = createAuthResponse(providerUrl);
            return builder.build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, null, null, SYSTEM_ERROR);
        }
    }

    @POST
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
            if (identifier == null) {
                log.warning("No OpenID identifier received");
                throw createOAuthProblemException(INVALID_IDENTIFIER, redirectUri);
            }
            log.info("User has been authenticated as: " + identifier);

            // Check redirect URI matches expectation
            if (isRedirectUriInvalid(clientId, redirectUri)) {
                log.warning("Invalid callback URI: " + redirectUri);
                throw createOAuthProblemException(INVALID_CALLBACK_URI, redirectUri);
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

            // Check if this is part of an identity association request
            if (clientId.equals(OAUTH_PROVIDER_ID) && redirectUri.equals(COMPLETE_ASSOCIATION_ENDPOINT)) {
                return createAssociationRequestResponse(request, identifier);
            }

            // Check if identity already has current grant/s; if so, make them invalid
            Set<TokenGrant> grants = identity.getTokenGrants();
            if (grants != null) {
                makeGrantsNonCurrent(authService, grants);
            }

            OAuthTokenResponseBuilder oAuthTokenResponseBuilder =
                    addTokenGrantResponseParams(createTokenGrant(clientId, scopes, identity, redirectUri),
                            HttpServletResponse.SC_FOUND);
            OAuthResponse response = oAuthTokenResponseBuilder.location(redirectUri).buildQueryMessage();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, redirectUri, HttpServletResponse.SC_NOT_FOUND, SYSTEM_ERROR);
        }
    }

    private Response.ResponseBuilder createAuthResponse(String providerUrl) {
        Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED);
        log.info("Sending request for login authentication");
        AuthorizationHeaderBuilder authHeaderBuilder = new AuthorizationHeaderBuilder()
                .forIdentifier(providerUrl)
                .usingRealm(OPENID_REALM)
                .returnTo(OPENID_RETURN_URL)
                .includeStandardAttributes();
        addRequiredAttributes(authHeaderBuilder);
        String authHeader = authHeaderBuilder.buildHeader();
        log.info("Request auth header: " + authHeader);
        builder.header(AUTHENTICATE_HEADER, authHeader);
        return builder;
    }

    private void addRequiredAttributes(AuthorizationHeaderBuilder authHeaderBuilder) {
        authHeaderBuilder.requireAttribute(FULLNAME, "http://axschema.org/namePerson");
        authHeaderBuilder.requireAttribute(FULLNAME_TITLE_CASE, "http://axschema.org/namePerson");
        authHeaderBuilder.requireAttribute(FIRSTNAME, "http://axschema.org/namePerson/first");
        authHeaderBuilder.requireAttribute(LASTNAME, "http://axschema.org/namePerson/last");
        // These may be worth adding, but they overwrite the standard values already set so should be based on the provider,
        //        authHeaderBuilder.requireAttribute(LANGUAGE, "http://openid.net/schema/language/pref");
        //        authHeaderBuilder.requireAttribute(EMAIL, "http://openid.net/schema/contact/internet/email");
        //        authHeaderBuilder.requireAttribute(COUNTRY, "http://openid.net/schema/contact/country/home");
    }

    private TokenGrant createTokenGrant(String clientId, Set<String> scopes, Identity identity, String redirectUri)
            throws OAuthProblemException, OAuthSystemException {
        TokenGrant tokenGrant = createTokenGrantWithDefaults(tokenIssuerService, authService, identity,
                authService.getClient(clientId).get());
        // If specific grant scopes requested, check these are valid and add to token grant
        if (scopes != null) {
            Set<Scope> grantScopes = checkScopes(authService, scopes, identity.getIdentityScopes(), redirectUri);
            tokenGrant.setGrantScopes(grantScopes);
        }
        authService.addGrant(tokenGrant);
        return tokenGrant;
    }

    private void checkOAuthClient(OAuthIdRequest oauthRequest) throws OAuthProblemException {
        String clientId = oauthRequest.getClientId();
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        if (clientNotFound(clientFound)) {
            throw createOAuthProblemException(INVALID_CLIENT_APPLICATION, oauthRequest.getRedirectURI());
        }
    }

    private String checkOpenIdProvider(HttpServletRequest request, OAuthIdRequest oAuthRequest)
            throws OAuthProblemException, OAuthSystemException {
        String providerUrl = request.getParameter(OPENID_PROVIDER);
        String decodedProviderUrl;
        try {
            decodedProviderUrl = URLDecoder.decode(providerUrl, UTF_ENCODING);
        } catch (UnsupportedEncodingException e) {
            log.severe("Could not decode provider URL: " + providerUrl);
            throw createOAuthProblemException(URL_DECODING_ERROR, oAuthRequest.getRedirectURI());
        }
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(decodedProviderUrl);
        if ((!providerFound.isPresent()) && getUrlDomain(decodedProviderUrl).isPresent()) {
            providerFound = authService.getOpenIdProvider(getUrlDomain(decodedProviderUrl).get());
        }
        if (providerFound.isPresent()) {
            request.getSession().setAttribute(OPENID_PROVIDER, providerFound.get().getProviderUrl());
        } else {
            throw createOAuthProblemException(INVALID_PROVIDER, oAuthRequest.getRedirectURI());
        }
        return providerUrl;
    }

    private Optional<String> getUrlDomain(String providerUrl) {
        try {
            URL url = new URL(providerUrl);
            String providerDomain;
            if (url.getHost().split("\\.").length > 2) {
                // Cut off the first part, which is generally the user identifier
                // This code would need to change to support other OpenID URL formats
                providerDomain = url.getHost().substring(url.getHost().indexOf('.') + 1);
            } else {
                providerDomain = url.getHost();
            }
            log.info("Extracted provider domain: " + providerDomain);
            return Optional.of(providerDomain);
        } catch (MalformedURLException e) {
            // Do nothing
        }
        return Optional.absent();
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
        String firstName = getOpenIdAttribute(request, newArrayList(FIRSTNAME, FIRSTNAME_TITLE_CASE));
        if (firstName != null) identity.setFirstName(firstName);
        String lastName = getOpenIdAttribute(request, newArrayList(LASTNAME, LASTNAME_TITLE_CASE));
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
        List<String> prefixes = Lists.newArrayList(OPENID_AX_PREFIX, OPENID_AX_VALUE_PREFIX, OPENID_EXT_VALUE_PREFIX);
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

    private boolean clientNotFound(Optional<ClientApplication> clientFound) {
        return !clientFound.isPresent();
    }

    private boolean clientRedirectUriDoesNotMatch(String decodedRedirectUri, Optional<ClientApplication> clientFound) {
        return !clientFound.get().getClientRedirectUri().equals(decodedRedirectUri);
    }

    private Response createAssociationRequestResponse(HttpServletRequest request, String identifier) {
        request.getSession().setAttribute(OPENID_IDENTIFIER, identifier);
        StringBuilder uriBuilder = new StringBuilder(COMPLETE_ASSOCIATION_ENDPOINT);
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