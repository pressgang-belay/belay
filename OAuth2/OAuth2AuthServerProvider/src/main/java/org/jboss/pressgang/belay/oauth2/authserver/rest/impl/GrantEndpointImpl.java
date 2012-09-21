package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.GrantEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.authserver.util.Resources;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Boolean.parseBoolean;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;
import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_CLIENT;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * Return endpoint for OpenID authentication requests. Completes the OAuth2 implicit or authorization code flows
 * and returns a token grant.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GrantEndpointImpl implements GrantEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuer tokenIssuer;

    /**
     * Endpoint to complete OAuth2 authorization. Should not be accessed directly; the user agent is redirected here when
     * end-users have authenticated with OpenID.
     *
     * @return Either a token grant response including access_token and expires_in parameters (for public clients)
     * or an authorization grant response including a code parameter, and state if it was supplied
     */
    @Override
    public Response authorize(@Context HttpServletRequest request) {
        log.info("Processing authorization attempt");

        String openIdClaimedId = (String) request.getAttribute(OPENID_CLAIMED_ID);
        String openIdIdentifier = (String) request.getAttribute(OPENID_IDENTIFIER);
        String identifier = (openIdClaimedId != null) ? openIdClaimedId : openIdIdentifier;
        String clientId = getStringAttributeFromSession(request, log, OAuth.OAUTH_CLIENT_ID, "OAuth client id");
        if (clientId.equals(authServerOAuthClientId)) {
            clientId = getStringAttributeFromSession(request, log, STORED_OAUTH_CLIENT_ID, "Stored OAuth client id");
        }
        String redirectUri = getStringAttributeFromSession(request, log, OAuth.OAUTH_REDIRECT_URI, "OAuth redirect URI");
        Set<String> scopes = getStringSetAttributeFromSession(request, log, OAuth.OAUTH_SCOPE, "Scopes requested");
        String providerUrl = getStringAttributeFromSession(request, log, OPENID_PROVIDER, "OpenId provider");
        Boolean alwaysPromptUserToApproveClientApp = parseBoolean(promptEndUserToApproveClientAppOnEveryLogin);
        Optional<Boolean> endUserConsentResult = getEndUserConsent(request);
        ClientApplication client;
        User user;

        try {
            if (clientId == null || redirectUri == null || providerUrl == null) {
                log.severe("Invalid session: Key session attribute/s null");
                throw createOAuthProblemException(INVALID_SESSION, null);
            }
            client = setClientApplication(clientId, redirectUri);
            if (endUserConsentResult.isPresent() && userDeniedAccess(endUserConsentResult)) {
                log.warning("End-user has refused client application access");
                throw OAuthEndpointUtil.createOAuthProblemException(USER_CONSENT_DENIED, redirectUri);
            } else if (endUserConsentResult.isPresent() && userApprovedAccess(endUserConsentResult)) {
                log.info("User has consented to client application access");
                user = (User) request.getSession().getAttribute(OAUTH2_USER);
                if (user != null && user.getPrimaryIdentity() != null) {
                    identifier = user.getPrimaryIdentity().getIdentifier();
                } else {
                    throw OAuthEndpointUtil.createOAuthProblemException(OAuthError.CodeResponse.SERVER_ERROR, redirectUri);
                }
                saveUserApproval(redirectUri, scopes, user, client);
            } else {
                // User consent has not been given yet
                if (identifier == null) {
                    log.warning("No OpenID identifier received");
                    throw createOAuthProblemException(INVALID_IDENTIFIER, redirectUri);
                }
                log.info("User has been authenticated as: " + identifier);

                // Check if this is a known identity or new identity
                Optional<Identity> identityFound = authService.getIdentity(identifier);

                Identity identity;
                boolean previouslyApprovedAllRequestedScopesForClient = false;
                if (identityFound.isPresent()) {
                    identity = identityFound.get();
                    updateIdentity(providerUrl, identity, request);
                    Set<Scope> requestScopes = checkScopes(authService, scopes, identity.getUser().getUserScopes(), redirectUri);
                    Optional<ClientApproval> clientApprovalFound = getClientApprovalForClientFromSet(log, identity.getUser().getClientApprovals(), client);
                    if (clientApprovalFound.isPresent() && (requestScopes.isEmpty() || clientApprovalFound.get().getApprovedScopes().containsAll(requestScopes))) {
                        log.info("End-user has previously approved requested scopes");
                        previouslyApprovedAllRequestedScopesForClient = true;
                    }
                } else {
                    identity = addNewIdentity(identifier, providerUrl, request);
                    checkScopes(authService, scopes, identity.getUser().getUserScopes(), redirectUri);
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

            // Redirect identity association request
            if (redirectUri.equals(completeAssociationEndpoint)) {
                return createAssociationRequestResponse(request, identifier);
            }

            OAuthResponse response;
            if (client.getClientSecret() == null || client.getClientSecret().isEmpty()) {
                // Issue Token Grant for a public client
                // Check if user already has current token grant/s for this client; if so, make them invalid
                Set<TokenGrant> grants = user.getTokenGrants();
                if (grants != null) {
                    makeTokenGrantsNonCurrent(authService, filterTokenGrantsByClient(grants, clientId));
                }
                OAuthASResponse.OAuthTokenResponseBuilder oAuthTokenResponseBuilder =
                        addTokenGrantResponseParams(createTokenGrant(clientId, scopes, user, redirectUri, false), SC_FOUND);
                response = oAuthTokenResponseBuilder.location(redirectUri).buildQueryMessage();
            } else {
                // Issue authorization code for confidential client
                // Check if user already has current code grants for this client; if so, make them invalid
                Set<CodeGrant> grants = user.getCodeGrants();
                if (grants != null) {
                    makeCodeGrantsNonCurrent(authService, filterCodeGrantsByClient(grants, clientId));
                }
                HttpServletRequest originalRequest = (HttpServletRequest)request.getSession().getAttribute(ORIGINAL_REQUEST);
                OAuthASResponse.OAuthAuthorizationResponseBuilder oAuthAuthorizationResponseBuilder =
                        addCodeGrantResponseParams(createCodeGrant(clientId, scopes, user, redirectUri), originalRequest, SC_FOUND);
                response = oAuthAuthorizationResponseBuilder.location(redirectUri).buildQueryMessage();
            }
            request.getSession().invalidate();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthProblemException e) {
            return handleOAuthProblemException(log, e);
        } catch (OAuthSystemException e) {
            return handleOAuthSystemException(log, e, redirectUri, SERVER_ERROR);
        }
    }

    private TokenGrant createTokenGrant(String clientId, Set<String> scopes, User user, String redirectUri,
                                        boolean issueRefreshToken)
            throws OAuthProblemException, OAuthSystemException {
        TokenGrant tokenGrant = OAuthEndpointUtil.createTokenGrantWithDefaults(tokenIssuer, authService, user,
                authService.getClient(clientId).get(), issueRefreshToken);
        // If specific grant scopes requested, check these are valid and add to token grant
        if (scopes != null) {
            Set<Scope> grantScopes = checkScopes(authService, scopes, user.getUserScopes(), redirectUri);
            tokenGrant.setGrantScopes(grantScopes);
        }
        authService.addTokenGrant(tokenGrant);
        return tokenGrant;
    }

    private CodeGrant createCodeGrant(String clientId, Set<String> scopes, User user, String redirectUri)
            throws OAuthProblemException, OAuthSystemException {
        CodeGrant codeGrant = OAuthEndpointUtil.createCodeGrantWithDefaults(tokenIssuer, authService, user,
                authService.getClient(clientId).get());
        // If specific grant scopes requested, check these are valid and add to code grant
        if (scopes != null) {
            Set<Scope> grantScopes = checkScopes(authService, scopes, user.getUserScopes(), redirectUri);
            codeGrant.setGrantScopes(grantScopes);
        }
        authService.addCodeGrant(codeGrant);
        return codeGrant;
    }

    private Identity addNewIdentity(String identifier, String providerUrl, HttpServletRequest request) {
        log.info("Creating new identity and associated user");
        Identity newIdentity = new Identity();
        newIdentity.setIdentifier(identifier);
        Optional<OpenIdProvider> providerFound = authService.getOpenIdProvider(providerUrl);
        newIdentity.setOpenIdProvider(providerFound.get());
        setExtraIdentityAttributes(request, newIdentity);
        User newUser = new User();
        newIdentity.setUser(newUser);
        authService.addIdentity(newIdentity);
        newUser.setPrimaryIdentity(newIdentity);
        newUser.setUserScopes(newHashSet(authService.getDefaultScope()));
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

    private ClientApplication setClientApplication(String clientId, String redirectUri) throws OAuthProblemException {
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        if (!clientFound.isPresent()) {
            throw createOAuthProblemException(INVALID_CLIENT, redirectUri);
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

    private void saveUserApproval(String redirectUri, Set<String> scopes, User user, ClientApplication client) throws OAuthProblemException {
        Optional<ClientApproval> clientApprovalFound = getClientApprovalForClientFromSet(log, user.getClientApprovals(), client);
        Set<Scope> approvedScopes = checkScopes(authService, scopes, user.getUserScopes(), redirectUri);
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
