package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableSet;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.AssociationEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static com.google.appengine.repackaged.com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.base.Strings.emptyToNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.amber.oauth2.common.OAuth.OAUTH_CLIENT_ID;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.*;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;

/**
 * This endpoint allows two users and their sets of identities to be associated together to form one user and identity set.
 * It can be used for confidential clients or public clients; if for confidential clients, it must be protected by Basic
 * or some other authentication. It must be protected by OAuth2 for both confidential and public clients; the user associated
 * with the access token presented is the authorized user to which the second set of identities are associated.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AssociationEndpointImpl implements AssociationEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    /**
     * This endpoint allows client applications to associate a second user and their set of identities with
     * the currently authorized user (the user associated with the token used to access this endpoint), to
     * create one user. The scopes and other sets of attributes of each user are merged (the result is the
     * union of the sets). The primary user during the merge will be the authorizing user, unless the
     * secondUserPrimary parameter is set to true. The username of the resulting user is the username of the
     * primary user, unless they do not have one and the other user does, in which case the other user's username
     * is used. The primary identity of the resulting user will be the primary identity of the primary user of
     * the pair.
     * <p/>
     * This operation does not affect the token grant for the currently authorized user. The token grant of
     * the second user will be invalidated during the operation.
     * <p/>
     * A 200 OK response will be returned if the operation is successful. Otherwise, an error status code
     * will be returned.
     *
     * @param request     The HTTP servlet request.
     * @param clientId    The ID of the client application making the request.
     * @param secondToken A current access token for the user to be associated.
     * @param secondUserPrimary True if the second user's attributes should be used as the primary attributes
     *                          of the resulting user, false if the authorizing user should be the primary. Defaults
     *                          to false.
     * @return
     */
    @Override
    public Response associateIdentities(@Context HttpServletRequest request,
                                        @FormParam(OAUTH_CLIENT_ID) String clientId,
                                        @FormParam(SECOND_TOKEN) String secondToken,
                                        @FormParam(SECOND_USER_PRIMARY) Boolean secondUserPrimary) {
        log.info("Processing identity association request");
        OAuthAccessResourceRequest resourceRequest;
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        boolean newUserPrimary = (secondUserPrimary == null) ? false : secondUserPrimary;
        ClientApplication client;
        TokenGrant firstUserTokenGrant;
        TokenGrant secondUserTokenGrant;
        User firstUser;
        User secondUser;

        try {
            resourceRequest = new OAuthAccessResourceRequest(request);
            if (!clientFound.isPresent()) {
                log.warning("Invalid OAuth2 client id: " + clientId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid OAuth2 ClientID").build();
            }
            client = clientFound.get();
            if ((!isClientPublic(client)) && request.getAuthType() == null) {
                log.warning("Unauthorized client: " + clientId);
                return Response.status(Response.Status.UNAUTHORIZED).entity("Client unauthorized").build();
            }
            if (isNullOrEmpty(secondToken)
                    || secondToken.equals(resourceRequest.getAccessToken())
                    || (!authService.getTokenGrantByAccessToken(secondToken).isPresent())
                    || (!authService.getTokenGrantByAccessToken(secondToken).get().getGrantCurrent())) {
                log.warning("Attempt to associate user with invalid token: " + secondToken);
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid token").build();
            }
            secondUserTokenGrant = authService.getTokenGrantByAccessToken(secondToken).get();
            if (!authService.getTokenGrantByAccessToken(resourceRequest.getAccessToken()).isPresent()) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            firstUserTokenGrant = authService.getTokenGrantByAccessToken(resourceRequest.getAccessToken()).get();
            firstUser = firstUserTokenGrant.getGrantUser();
            log.info("Authorized identity is: " + request.getUserPrincipal().getName());
            log.info("Authorizing user's primary identity is: " + firstUser.getPrimaryIdentity().getIdentifier());
            secondUser = secondUserTokenGrant.getGrantUser();
            log.info("Second user's primary identity is: " + secondUser.getPrimaryIdentity().getIdentifier());
            if (secondUser.getUserIdentities().contains(request.getUserPrincipal().getName())) {
                log.severe("Authorized identity already associated with second user");
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (OAuthSystemException e) {
            log.warning("OAuthSystemException thrown during identity association attempt: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OAuthProblemException e) {
            log.warning("OAuthProblemException thrown during identity association attempt: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        if (newUserPrimary) {
            mergeUserAttributes(firstUser, secondUser);
            invalidateTokenGrant(authService, firstUserTokenGrant);
        } else {
            mergeUserAttributes(secondUser, firstUser);
            invalidateTokenGrant(authService, secondUserTokenGrant);
        }
        return Response.ok().entity("Operation completed successfully").build();
    }

    private User mergeUserAttributes(final User oldUser, final User newUser) {
        if (isNullOrEmpty(newUser.getUsername()) && (!isNullOrEmpty(oldUser.getUsername()))) {
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
}
