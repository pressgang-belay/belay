package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static org.apache.amber.oauth2.common.OAuth.OAUTH_CLIENT_ID;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface GrantEndpoint {
    @GET
    Response invalidateTokenGrant(@Context HttpServletRequest request,
                                  @QueryParam(OAUTH_CLIENT_ID) String clientId);
}
