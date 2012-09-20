package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableSet;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.CompleteAssociationEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Set;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static javax.servlet.http.HttpServletResponse.SC_FOUND;
import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.SERVER_ERROR;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class CompleteAssociationEndpointImpl implements CompleteAssociationEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuer tokenIssuer;

    @Override
    public Response completeAssociation(@Context HttpServletRequest request) throws OAuthProblemException,
            OAuthSystemException {
        log.info("Processing identity association request completion");

        String secondId = OAuthEndpointUtil.getStringAttributeFromSession(request, log, OPENID_IDENTIFIER, "Second identifier");
        String oAuthRedirectUri = OAuthEndpointUtil.getStringAttributeFromSession(request, log, STORED_OAUTH_REDIRECT_URI,
                "Stored OAuth redirect URI");
        TokenGrant requestTokenGrant = getTokenGrantFromAccessToken(log, authService, request, oAuthRedirectUri);
        String firstId = OAuthEndpointUtil.getStringAttributeFromSession(request, log, FIRST_IDENTIFIER,
                "First identifier");
        Boolean secondIdentityIsPrimary = OAuthEndpointUtil.getBooleanAttributeFromSession(request, log, NEW_IDENTITY_PRIMARY,
                "Second identity is primary flag");
        String clientId = OAuthEndpointUtil.getStringAttributeFromSession(request, log, STORED_OAUTH_CLIENT_ID,
                "OAuth client id");
        Set<String> scopesRequested = OAuthEndpointUtil.getStringSetAttributeFromSession(request, log,
                OAuth.OAUTH_SCOPE, "Scopes requested");
        request.getSession().invalidate();

        if (firstId == null || secondId == null || secondIdentityIsPrimary == null || oAuthRedirectUri == null
                || clientId == null){
            log.severe("Identity association session attribute null or invalid");
            throw new OAuthSystemException("Null session attribute");
        }
        ClientApplication client = checkClient(authService, oAuthRedirectUri, clientId);

        // Get users for identifiers
        Optional<Identity> firstIdentityFound = authService.getIdentity(firstId);
        Optional<Identity> secondIdentityFound = authService.getIdentity(secondId);
        if ((!firstIdentityFound.isPresent()) || (!secondIdentityFound.isPresent())) {
            throw new OAuthSystemException("Could not find both identities to associate");
        }
        User firstUser = firstIdentityFound.get().getUser();
        User secondUser = secondIdentityFound.get().getUser();

        // Check identities not already associated
        if (firstUser.equals(secondUser)) {
            throw OAuthEndpointUtil.createOAuthProblemException(IDENTITIES_ASSOCIATED_ERROR, oAuthRedirectUri);
        }

        User finalUser = mergeUsers(secondIdentityIsPrimary, secondIdentityFound.get(), firstUser, secondUser);
        // Use scopes from previous TokenGrant if first identity was the primary, or the requested scopes if not
        Identity primaryIdentity = finalUser.getPrimaryIdentity();
        Set<Scope> grantScopes = getGrantScopes(requestTokenGrant, secondIdentityIsPrimary, scopesRequested,
                secondIdentityFound.get(), oAuthRedirectUri);
        Response response = createTokenGrantResponseForUser(oAuthRedirectUri, client, grantScopes,
                finalUser, (requestTokenGrant.getRefreshToken() != null));

        if (response.getStatus() == HttpServletResponse.SC_FOUND) {
            // Make original TokenGrant non-current
            makeTokenGrantNonCurrent(authService, requestTokenGrant);
        }
        log.info("Sending token response to " + oAuthRedirectUri);
        return response;
    }


    private Set<Scope> getGrantScopes(TokenGrant requestTokenGrant, Boolean secondIdentityIsPrimary,
                                      Set<String> scopesRequested, Identity secondIdentity,
                                      String redirectUri) throws OAuthProblemException {
        if (secondIdentityIsPrimary) {
            if (scopesRequested != null) {
                return checkScopes(authService, scopesRequested, secondIdentity.getUser().getUserScopes(), redirectUri);
            } else {
                log.warning("Identity has default scope after association request as no scopes requested");
                return null;
            }
        } else {
            return newHashSet(requestTokenGrant.getGrantScopes());
        }
    }

    private User mergeUsers(Boolean secondIdentityIsPrimary, Identity secondIdentity, User firstUser, User secondUser) {
        if (secondIdentityIsPrimary) {
            secondUser.setPrimaryIdentity(secondIdentity);
            return mergeUserAttributes(firstUser, secondUser);
        } else {
            return mergeUserAttributes(secondUser, firstUser);
        }
    }

    private User mergeUserAttributes(final User oldUser, final User newUser) {
        if (isNullOrEmpty(newUser.getUsername()) && (! isNullOrEmpty(oldUser.getUsername()))) {
            newUser.setUsername(oldUser.getUsername());
        }
        newUser.getUserIdentities().addAll(updateIdentities(oldUser, newUser));
        newUser.getTokenGrants().addAll(updateOldTokenGrants(oldUser, newUser));
        newUser.getCodeGrants().addAll(updateOldCodeGrants(oldUser, newUser));
        newUser.getUserScopes().addAll(newHashSet(oldUser.getUserScopes()));
        newUser.getClientApprovals().addAll(updateOldClientApprovals(oldUser, newUser));

        authService.updateUser(newUser);
        authService.deleteUser(oldUser);
        return newUser;
    }

    private ImmutableSet<Identity> updateIdentities(User oldUser, final User newUser) {
        return copyOf(transform(oldUser.getUserIdentities(), new Function<Identity, Identity>() {
            @Override
            public Identity apply(Identity identity) {
                identity.setUser(newUser);
                authService.updateIdentity(identity);
                return identity;
            }
        }));
    }

    private ImmutableSet<ClientApproval> updateOldClientApprovals(User oldUser, final User newUser) {
        return copyOf(transform(oldUser.getClientApprovals(), new Function<ClientApproval, ClientApproval>() {
            @Override
            public ClientApproval apply(ClientApproval clientApproval) {
                clientApproval.setApprover(newUser);
                authService.updateClientApproval(clientApproval);
                return clientApproval;
            }
        }));
    }

    private ImmutableSet<TokenGrant> updateOldTokenGrants(User oldUser, final User newUser) {
        return copyOf(transform(oldUser.getTokenGrants(), new Function<TokenGrant, TokenGrant>() {
            @Override
            public TokenGrant apply(TokenGrant tokenGrant) {
                tokenGrant.setGrantUser(newUser);
                authService.updateTokenGrant(tokenGrant);
                return tokenGrant;
            }
        }));
    }

    private ImmutableSet<CodeGrant> updateOldCodeGrants(User oldUser, final User newUser) {
        return copyOf(transform(oldUser.getCodeGrants(), new Function<CodeGrant, CodeGrant>() {
            @Override
            public CodeGrant apply(CodeGrant codeGrant) {
                codeGrant.setGrantUser(newUser);
                authService.updateCodeGrant(codeGrant);
                return codeGrant;
            }
        }));
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    private Response createTokenGrantResponseForUser(String oAuthRedirectUri, ClientApplication client,
                                                     Set<Scope> requestedGrantScopes, User user,
                                                     boolean issueRefreshToken) {
        TokenGrant newTokenGrant;
        try {
            newTokenGrant = createTokenGrantWithDefaults(tokenIssuer, authService,
                    user, client, issueRefreshToken);
            if (requestedGrantScopes != null) {
                Set<Scope> grantScopes = newTokenGrant.getGrantScopes();
                for (Scope grantScope : requestedGrantScopes) {
                    grantScopes.add(grantScope);
                }
                newTokenGrant.setGrantScopes(grantScopes);
            }
            authService.addTokenGrant(newTokenGrant);
            OAuthASResponse.OAuthTokenResponseBuilder oAuthTokenResponseBuilder
                    = addTokenGrantResponseParams(newTokenGrant, SC_FOUND);
            OAuthResponse response = oAuthTokenResponseBuilder.location(oAuthRedirectUri).buildQueryMessage();
            return Response.status(response.getResponseStatus()).location(URI.create(response.getLocationUri())).build();
        } catch (OAuthSystemException e) {
            log.severe("Could not create new token grant: " + e.getMessage());
            return handleOAuthSystemException(log, e, oAuthRedirectUri, SERVER_ERROR);
        }
    }
}
