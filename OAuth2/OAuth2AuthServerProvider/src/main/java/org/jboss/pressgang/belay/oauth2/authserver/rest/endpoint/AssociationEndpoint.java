package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.apache.amber.oauth2.common.OAuth.OAUTH_CLIENT_ID;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.SECOND_TOKEN;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.SECOND_USER_PRIMARY;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface AssociationEndpoint {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    Response associateIdentities(@Context HttpServletRequest request,
                                 @FormParam(OAUTH_CLIENT_ID) String clientId,
                                 @FormParam(SECOND_TOKEN) String secondUserToken,
                                 @FormParam(SECOND_USER_PRIMARY) Boolean secondUserPrimary);
}
