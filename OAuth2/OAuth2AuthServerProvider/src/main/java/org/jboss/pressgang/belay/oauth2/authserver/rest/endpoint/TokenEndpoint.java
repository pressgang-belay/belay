package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import org.apache.amber.oauth2.common.exception.OAuthSystemException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface TokenEndpoint {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    Response authorize(@Context HttpServletRequest request) throws OAuthSystemException;
}
