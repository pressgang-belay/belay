package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface PublicClientAuthEndpoint {

    @GET
    Response requestAuthenticationWithOpenId(@Context HttpServletRequest request);

    @POST
    Response authorise(@Context HttpServletRequest request);
}
