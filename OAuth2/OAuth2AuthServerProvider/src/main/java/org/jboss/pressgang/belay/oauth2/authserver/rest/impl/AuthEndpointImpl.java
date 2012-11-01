package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.code.openid.AuthorizationHeaderBuilder;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.AuthEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.authserver.util.Resources;

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
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.parseBoolean;
import static java.net.URLDecoder.decode;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.*;
import static org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest.OAuthIdRequestParams;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * OAuth2 authorization endpoint using OpenID to authenticate end-users. The endpoint can be used for OAuth2's implicit
 * authorization flow, if the client is public, or the authorization code flow if the client is confidential. Both
 * flows must require HTTPS.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AuthEndpointImpl implements AuthEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuer tokenIssuer;

    /**
     * Endpoint for OAuth2 authorization requests, using OpenID for authentication. Required OAuth2 parameters, which
     * may be supplied query or header style, are:
     * response_type: OAuth2 response type, token for public clients and code for confidential clients
     * client_id: OAuth2 client identifier, supplied by OAuth2 Auth Server
     * redirect_uri: URI to redirect to when auth complete; must be registered with the Auth Server
     * provider: the URL or domain of the OpenID provider with which to authenticate the end-user
     * <p/>
     * Confidential clients may also supply a state parameter.
     */
    @Override
    public Response requestAuthenticationWithOpenId(@Context HttpServletRequest request) {
        log.info("Processing authentication request");
        try {
            OAuthIdRequest oAuthRequest = new OAuthIdRequest(request);
            checkOAuthClientAndRedirectUri(oAuthRequest, oAuthRequest.getClientId(), oAuthRequest.getRedirectURI());
            String baseProviderUrl = checkOpenIdProvider(oAuthRequest);
            storeOAuthRequestParams(request, oAuthRequest, baseProviderUrl);
            return createAuthResponse(request, oAuthRequest.getProvider()).build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, null, null);
        }
    }

    /**
     * Endpoint to complete OAuth2 authorization. Should not be accessed directly; the user agent is redirected here when
     * end-users have authenticated with OpenID.
     *
     * @return Either a token grant response including access_token and expires_in parameters (for public clients)
     *         or an authorization grant response including a code parameter, and state if it was supplied
     */
    @Override
    public Response authorize(@Context HttpServletRequest request) {
        log.info("Processing authorization attempt");

        String openIdClaimedId = (String) request.getAttribute(OPENID_CLAIMED_ID);
        String openIdIdentifier = (String) request.getAttribute(OPENID_IDENTIFIER);
        String identifier = (!isNullOrEmpty(openIdClaimedId)) ? openIdClaimedId : openIdIdentifier;
        Boolean alwaysPromptUserToApproveClientApp = parseBoolean(promptEndUserToApproveClientAppOnEveryLogin);
        Optional<Boolean> endUserConsentResult = getEndUserConsent(request);
        OAuthIdRequestParams oAuthRequestParams = getOAuthIdRequestParamsFromSession(request, ORIGINAL_REQUEST_PARAMS);
        String clientId;
        String redirectUri;
        Set<String> scopes;
        String providerUrl;
        ClientApplication client;
        User user;

        try {
            if (oAuthRequestParams == null) {
                log.severe("Invalid session: OAuth request params null");
                throw createOAuthProblemException(SERVER_ERROR, null, null);
            }
            clientId = oAuthRequestParams.getClientId();
            if (clientId.equals(authServerOAuthClientId)) {
                clientId = getStringAttributeFromSession(request, log, STORED_OAUTH_CLIENT_ID, "Stored OAuth client id");
            }
            redirectUri = oAuthRequestParams.getRedirectUri();
            scopes = oAuthRequestParams.getScopes();
            providerUrl = oAuthRequestParams.getProvider();
            client = setClientApplication(clientId, redirectUri, oAuthRequestParams.getState());
            if (endUserConsentResult.isPresent() && userDeniedAccess(endUserConsentResult)) {
                log.warning("End-user has refused client application access");
                throw OAuthEndpointUtil.createOAuthProblemException(ACCESS_DENIED, redirectUri, oAuthRequestParams.getState());
            } else if (endUserConsentResult.isPresent() && userApprovedAccess(endUserConsentResult)) {
                log.info("User has consented to client application access");
                user = (User) request.getSession().getAttribute(OAUTH2_USER);
                if (user != null && user.getPrimaryIdentity() != null) {
                    identifier = user.getPrimaryIdentity().getIdentifier();
                } else {
                    throw OAuthEndpointUtil.createOAuthProblemException(OAuthError.CodeResponse.SERVER_ERROR, redirectUri,
                            oAuthRequestParams.getState());
                }
                saveUserApproval(redirectUri, scopes, user, client, oAuthRequestParams.getState());
            } else {
                // User consent has not been given yet
                if (identifier == null) {
                    log.warning("No OpenID identifier received");
                    throw createOAuthProblemException(INVALID_REQUEST, redirectUri, oAuthRequestParams.getState());
                }
                log.info("User has been authenticated as: " + identifier);

                // Check if this is a known identity or new identity
                Optional<Identity> identityFound = authService.getIdentity(identifier);

                Identity identity;
                boolean previouslyApprovedAllRequestedScopesForClient = false;
                if (identityFound.isPresent()) {
                    identity = identityFound.get();
                    updateIdentity(providerUrl, identity, request);
                    Set<Scope> requestScopes = checkScopes(authService, scopes, identity.getUser().getUserScopes(), redirectUri, oAuthRequestParams.getState());
                    Optional<ClientApproval> clientApprovalFound = getClientApprovalForClientFromSet(log, identity.getUser().getClientApprovals(), client);
                    if (clientApprovalFound.isPresent() && (requestScopes.isEmpty() || clientApprovalFound.get().getApprovedScopes().containsAll(requestScopes))) {
                        log.info("End-user has previously approved requested scopes");
                        previouslyApprovedAllRequestedScopesForClient = true;
                    }
                } else {
                    identity = addNewIdentity(identifier, providerUrl, request);
                    checkScopes(authService, scopes, identity.getUser().getUserScopes(), redirectUri, oAuthRequestParams.getState());
                }
                user = identity.getUser();

                if (alwaysPromptUserToApproveClientApp || (!previouslyApprovedAllRequestedScopesForClient)) {
                    request.getSession().setAttribute(OAUTH2_USER, user);
                    String clientName = authService.getClient(clientId).get().getClientName();
                    request.getSession().setAttribute(CLIENT_NAME, clientName);
                    log.info("Redirecting end-user to approve scopes for client appplication " + clientName);
                    return Response.temporaryRedirect(URI.create(Resources.endUserConsentUri)).build();
                }
            }

            OAuthResponse response;
            if (isNullOrEmpty(client.getClientSecret())) {
                // Issue Token Grant for a public client
                // Check if user already has current token grant/s for this client; if so, make them invalid
                Set<TokenGrant> grants = user.getTokenGrants();
                if (grants != null) {
                    makeExpiringTokenGrantsNonCurrent(authService, filterTokenGrantsByClient(grants, clientId));
                }
                OAuthASResponse.OAuthTokenResponseBuilder oAuthTokenResponseBuilder =
                        addTokenGrantResponseParams(createTokenGrant(clientId, scopes, user, redirectUri,
                                oAuthRequestParams.getState(), false),
                                SC_FOUND, oAuthRequestParams.getState());
                response = oAuthTokenResponseBuilder.location(redirectUri).buildQueryMessage();
            } else {
                // Issue authorization code for confidential client
                // Check if user already has current code grants for this client; if so, make them invalid
                Set<CodeGrant> grants = user.getCodeGrants();
                if (grants != null) {
                    makeCodeGrantsNonCurrent(authService, filterCodeGrantsByClient(grants, clientId));
                }
                HttpServletRequest originalRequest = (HttpServletRequest) request.getSession().getAttribute(ORIGINAL_REQUEST_PARAMS);
                OAuthASResponse.OAuthAuthorizationResponseBuilder oAuthAuthorizationResponseBuilder =
                        addCodeGrantResponseParams(createCodeGrant(clientId, scopes, user, redirectUri,
                                oAuthRequestParams.getState()), originalRequest,
                                SC_FOUND, oAuthRequestParams.getState());
                response = oAuthAuthorizationResponseBuilder.location(redirectUri).buildQueryMessage();
            }
            request.getSession().invalidate();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, oAuthRequestParams.getRedirectUri(), SERVER_ERROR);
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

    private void checkOAuthClientAndRedirectUri(OAuthIdRequest request, String clientId, String redirectUri) throws OAuthProblemException {
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        String error;
        try {
            if (!clientFound.isPresent()) {
                log.warning("Invalid OAuth2 client with id '" + clientId + "' in login request");
                error = UNAUTHORIZED_CLIENT;
            } else if (!clientRedirectUriMatches(decode(redirectUri, urlEncoding), clientFound.get())) {
                log.warning("Invalid OAuth2 redirect URI in login request: " + decode(redirectUri, urlEncoding));
                error = INVALID_REQUEST;
            } else {
                return;
            }
        } catch (UnsupportedEncodingException e) {
            log.severe("Error during URL decoding: " + e);
            error = SERVER_ERROR;
        }
        throw createOAuthProblemException(error, redirectUri, request.getState());
    }

    private String checkOpenIdProvider(OAuthIdRequest oAuthRequest)
            throws OAuthProblemException {
        String providerUrl = oAuthRequest.getProvider();
        String decodedProviderUrl;
        try {
            decodedProviderUrl = decode(providerUrl, urlEncoding);
        } catch (UnsupportedEncodingException e) {
            log.severe("Could not decode provider URL: " + providerUrl);
            throw createOAuthProblemException(INVALID_REQUEST, oAuthRequest.getRedirectURI(), oAuthRequest.getState());
        }
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(decodedProviderUrl);
        if ((!providerFound.isPresent()) && getPossibleDomains(decodedProviderUrl).isPresent()) {
            for (String domain : getPossibleDomains(decodedProviderUrl).get()) {
                providerFound = authService.getOpenIdProvider(domain);
                if (providerFound.isPresent()) break;
            }
        }
        if (providerFound.isPresent()) {
            return providerFound.get().getProviderUrl();
        } else {
            log.warning("Invalid OpenID provider: " + providerUrl);
            throw createOAuthProblemException(INVALID_REQUEST, oAuthRequest.getRedirectURI(), oAuthRequest.getState());
        }
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

    private void storeOAuthRequestParams(HttpServletRequest request, OAuthIdRequest oAuthRequest, String providerUrl) {
        OAuthIdRequestParams oAuthIdRequestParams = oAuthRequest.copyOAuthParams();
        oAuthIdRequestParams.setProvider(providerUrl);
        request.getSession().setAttribute(ORIGINAL_REQUEST_PARAMS, oAuthIdRequestParams);
    }

    private boolean clientRedirectUriMatches(String decodedRedirectUri, ClientApplication client) {
        return client.getClientRedirectUri().equals(decodedRedirectUri);
    }

    private TokenGrant createTokenGrant(String clientId, Set<String> scopes, User user, String redirectUri,
                                        String state, boolean issueRefreshToken)
            throws OAuthProblemException, OAuthSystemException {
        TokenGrant tokenGrant = OAuthEndpointUtil.createTokenGrantWithDefaults(tokenIssuer, authService, user,
                authService.getClient(clientId).get(), issueRefreshToken);
        // If specific grant scopes requested, check these are valid and add to token grant
        if (scopes != null) {
            Set<Scope> grantScopes = checkScopes(authService, scopes, user.getUserScopes(), redirectUri, state);
            tokenGrant.setGrantScopes(grantScopes);
        }
        authService.addTokenGrant(tokenGrant);
        return tokenGrant;
    }

    private CodeGrant createCodeGrant(String clientId, Set<String> scopes, User user, String redirectUri, String state)
            throws OAuthProblemException, OAuthSystemException {
        CodeGrant codeGrant = OAuthEndpointUtil.createCodeGrantWithDefaults(tokenIssuer, authService, user,
                authService.getClient(clientId).get());
        // If specific grant scopes requested, check these are valid and add to code grant
        if (scopes != null) {
            Set<Scope> grantScopes = checkScopes(authService, scopes, user.getUserScopes(), redirectUri, state);
            codeGrant.setGrantScopes(grantScopes);
        }
        authService.addCodeGrant(codeGrant);
        return codeGrant;
    }

    private Identity addNewIdentity(String identifier, String providerUrl, HttpServletRequest request) {
        log.info("Creating new identity and associated user");
        User newUser = new User();
        authService.addUser(newUser);
        Identity newIdentity = new Identity();
        newIdentity.setIdentifier(identifier);
        newIdentity.setOpenIdProvider(authService.getOpenIdProvider(providerUrl).get());
        setExtraIdentityAttributes(request, newIdentity);
        newIdentity.setUser(newUser);
        authService.addIdentity(newIdentity);
        newUser.setPrimaryIdentity(newIdentity);
        newUser.setUserScopes(newHashSet(authService.getDefaultScope()));
        newUser.setUserIdentities(newHashSet(newIdentity));
        authService.updateUser(newUser);
        return newIdentity;
    }

    private void updateIdentity(String providerUrl, Identity identity, HttpServletRequest request) {
        log.info("Updating existing identity");
        if (! identity.getOpenIdProvider().equals(authService.getOpenIdProvider(providerUrl).get())) {
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

    private ClientApplication setClientApplication(String clientId, String redirectUri, String state)
            throws OAuthProblemException {
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        if (!clientFound.isPresent()) {
            throw createOAuthProblemException(UNAUTHORIZED_CLIENT, redirectUri, state);
        }
        return clientFound.get();
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

    private Optional<Boolean> getEndUserConsent(HttpServletRequest request) {
        String endUserConsent = request.getParameter(USER_CONSENT);
        if (endUserConsent != null) {
            return Optional.of(parseBoolean(endUserConsent));
        } else {
            return Optional.absent();
        }
    }

    private Boolean userApprovedAccess(Optional<Boolean> endUserConsentResult) {
        return endUserConsentResult.get();
    }

    private boolean userDeniedAccess(Optional<Boolean> endUserConsentResult) {
        return !endUserConsentResult.get();
    }

    private void saveUserApproval(String redirectUri, Set<String> scopes, User user, ClientApplication client,
                                  String state) throws OAuthProblemException {
        Optional<ClientApproval> clientApprovalFound = getClientApprovalForClientFromSet(log, user.getClientApprovals(), client);
        Set<Scope> approvedScopes = checkScopes(authService, scopes, user.getUserScopes(), redirectUri, state);
        if (clientApprovalFound.isPresent()) {
            clientApprovalFound.get().getApprovedScopes().addAll(approvedScopes);
            authService.updateClientApproval(clientApprovalFound.get());
        } else {
            ClientApproval clientApproval = new ClientApproval();
            clientApproval.setClientApplication(client);
            clientApproval.setApprover(user);
            authService.addClientApproval(clientApproval);
            user.getClientApprovals().add(clientApproval);
            authService.updateUser(user);
            clientApproval.setApprovedScopes(approvedScopes);
            authService.updateClientApproval(clientApproval);
        }
    }

}