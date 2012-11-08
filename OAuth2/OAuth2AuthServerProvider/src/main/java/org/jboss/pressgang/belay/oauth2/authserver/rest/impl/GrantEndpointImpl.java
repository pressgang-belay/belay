package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.google.common.base.Optional;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.rs.request.OAuthAccessResourceRequest;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.ClientApplication;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.GrantEndpoint;
import org.jboss.pressgang.belay.oauth2.authserver.service.AuthService;
import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

import static org.apache.amber.oauth2.common.OAuth.OAUTH_CLIENT_ID;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.isClientPublic;

/**
 * This endpoint allows client applications to invalidate a current token grant. It should be protected by Basic or
 * some other authentication when used by confidential clients. It must be protected by OAuth2.
 * <p/>
 * The status code 200 (OK) will be returned if the operation was successful. OK will be returned even if the token
 * grant was already invalidated.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GrantEndpointImpl implements GrantEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Inject
    private AuthService authService;

    @Override
    public Response invalidateTokenGrant(@Context HttpServletRequest request,
                                         @QueryParam(OAUTH_CLIENT_ID) String clientId) {
        log.info("Received request to invalidate token grant");
        OAuthAccessResourceRequest resourceRequest;
        ClientApplication client;
        TokenGrant tokenGrant;

        // Check client
        Optional<ClientApplication> clientFound = authService.getClient(clientId);
        if (!clientFound.isPresent()) {
            log.warning("Invalid clientID: " + clientId);
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid OAuth2 ClientID").build();
        }
        client = clientFound.get();
        if ((!isClientPublic(client)) && request.getAuthType() == null) {
            log.warning("Attempt to use confidential client without authorization: " + clientId);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Client unauthorized").build();
        }

        try {
            resourceRequest = new OAuthAccessResourceRequest(request);
            Optional<TokenGrant> tokenGrantFound = authService.getTokenGrantByAccessToken(resourceRequest.getAccessToken());
            if (!tokenGrantFound.isPresent()) {
                log.warning("Invalid access token");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            tokenGrant = tokenGrantFound.get();
        } catch (OAuthSystemException e) {
            log.warning("OAuthSystemException thrown during token grant invalidation attempt: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (OAuthProblemException e) {
            log.warning("OAuthProblemException thrown during token grant invalidation attempt: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }

        tokenGrant.setGrantCurrent(false);
        authService.updateTokenGrant(tokenGrant);
        log.info("Token grant invalidated");
        return Response.ok().entity("Operation completed successfully").build();
    }
}
