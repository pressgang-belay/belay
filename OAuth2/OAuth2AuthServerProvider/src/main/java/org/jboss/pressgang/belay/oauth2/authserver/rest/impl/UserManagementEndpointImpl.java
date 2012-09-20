package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.*;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.UserManagementEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.service.TokenIssuer;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;
import org.jboss.pressgang.belay.oauth2.shared.data.model.IdentityInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.UserInfo;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.security.Principal;
import java.util.logging.Logger;

import static com.google.common.collect.Sets.newHashSet;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.createWebApplicationException;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.*;

/**
 * Provides identity services for client applications, such as associating another identity with the currently
 * authenticated user and obtaining identity information. These services must be protected by the OAuth filter.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class UserManagementEndpointImpl implements UserManagementEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Inject
    private TokenIssuer tokenIssuer;

    /**
     * This endpoint sets an identity of the currently authorized user as the user's primary identity. The endpoint
     * will return successfully even if the identity is already the user's primary identity.
     *
     * @param request    The servlet request
     * @param identifier The identifier of the identity to set as primary
     * @return A token response for the resulting primary identity
     */
    @Override
    public Response makeIdentityPrimary(@Context HttpServletRequest request,
                                        @QueryParam(IDENTIFIER) String identifier) {
        log.info("Processing request to make " + identifier + " primary identity");
        checkIdentityAssociatedWithAuthorizedUser(request, identifier);
        Identity newPrimaryIdentity = authService.getIdentity(identifier).get();
        User user = newPrimaryIdentity.getUser();
        user.setPrimaryIdentity(newPrimaryIdentity);
        authService.updateUser(user);
        log.info("Sending OK result for request to make identity primary");
        return Response.ok().build();
    }

    /**
     * This endpoint provides information about identities associated with the authorized user. If an identifier is
     * provided and the identity represented by that identifier is associated with the authorized user, information
     * will be provided about that identity. Otherwise, information will be returned on the primary identity associated
     * with the authorized user.
     *
     * @param request    The servlet request
     * @param identifier Identifier of the identity to return information about
     * @return Identity information in JSON format
     */
    @Override
    public IdentityInfo getIdentityInfo(@Context HttpServletRequest request,
                                        @QueryParam(IDENTIFIER) String identifier) {
        Principal userPrincipal = request.getUserPrincipal();
        String identifierToQuery;

        if (identifier == null) {
            identifierToQuery = userPrincipal.getName();
        } else {
            checkIdentityAssociatedWithAuthorizedUser(request, identifier);
            identifierToQuery = identifier;
        }
        Optional<IdentityInfo> identityInfoFound = authService.getIdentityInfo(identifierToQuery);
        if (!identityInfoFound.isPresent()) {
            log.warning("Identity query failed; could not find identity info for " + identifierToQuery);
            throw createWebApplicationException(IDENTITY_QUERY_ERROR, HttpServletResponse.SC_NOT_FOUND);
        }
        log.info("Returning identity info for " + identifierToQuery);
        return identityInfoFound.get();
    }

    /**
     * This endpoint provides information about the authorized user.
     *
     * @param request The servlet request
     * @return User information in JSON format
     */
    @Override
    public UserInfo getUserInfo(@Context HttpServletRequest request) {
        String identifier = request.getUserPrincipal().getName();
        Optional<UserInfo> userInfoFound = authService.getUserInfo(identifier);
        if (!userInfoFound.isPresent()) {
            log.warning("User query failed; could not find user info for identity " + identifier);
            throw createWebApplicationException(USER_QUERY_ERROR, HttpServletResponse.SC_NOT_FOUND);
        }
        log.info("Returning user info for " + identifier);
        return userInfoFound.get();
    }

    private void checkIdentityAssociatedWithAuthorizedUser(HttpServletRequest request, String identifier) {
        Optional<Identity> primaryIdentity = authService.getIdentity(request.getUserPrincipal().getName());
        if ((!primaryIdentity.isPresent() || (!authService.isIdentityAssociatedWithUser(identifier,
                primaryIdentity.get().getUser())))) {
            log.warning("Could not process request related to identity " + identifier
                    + " ; user unauthorized or identity not found");
            throw createWebApplicationException(UNAUTHORIZED_QUERY_ERROR + " " + identifier,
                    HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
