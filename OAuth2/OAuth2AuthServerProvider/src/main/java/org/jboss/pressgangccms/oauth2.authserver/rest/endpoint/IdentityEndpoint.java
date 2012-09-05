package org.jboss.pressgangccms.oauth2.authserver.rest.endpoint;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.jboss.pressgangccms.oauth2.shared.data.model.IdentityInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

import static org.jboss.pressgangccms.oauth2.authserver.util.Constants.IDENTIFIER;
import static org.jboss.pressgangccms.oauth2.authserver.util.Constants.NEW_IDENTITY_PRIMARY;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface IdentityEndpoint {

    @GET
    @Path("/associate")
    Response associateUser(@Context HttpServletRequest request,
                                  @QueryParam(NEW_IDENTITY_PRIMARY) Boolean newIsPrimary) throws URISyntaxException;

    @GET
    @Path("/completeAssociation")
    Response completeAssociation(@Context HttpServletRequest request) throws OAuthProblemException,
            OAuthSystemException;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/query")
    IdentityInfo getPrimaryUserInfo(@Context HttpServletRequest request,
                                           @QueryParam(IDENTIFIER) String identifier);

    @GET
    @Path("/makePrimary")
    Response makeIdentityPrimary(@Context HttpServletRequest request,
                                        @QueryParam(IDENTIFIER) String identifier);

}
