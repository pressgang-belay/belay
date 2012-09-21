package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import org.jboss.pressgang.belay.oauth2.shared.data.model.IdentityInfo;
import org.jboss.pressgang.belay.oauth2.shared.data.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.IDENTIFIER;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface UserManagementEndpoint {
    @GET
    @Path("/makeIdentityPrimary")
    Response makeIdentityPrimary(@Context HttpServletRequest request,
                                 @QueryParam(IDENTIFIER) String identifier);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/queryIdentity")
    IdentityInfo getIdentityInfo(@Context HttpServletRequest request,
                                 @QueryParam(IDENTIFIER) String identifier);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/queryUser")
    UserInfo getUserInfo(@Context HttpServletRequest request);
}
