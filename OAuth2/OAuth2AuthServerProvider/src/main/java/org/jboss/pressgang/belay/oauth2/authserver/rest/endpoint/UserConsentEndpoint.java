package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface UserConsentEndpoint {
    @GET
    @Produces(MediaType.TEXT_HTML)
    String getUserConsentForm(@Context HttpServletRequest request);
}
