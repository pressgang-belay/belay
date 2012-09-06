package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.APPLICATION_XRDS_XML;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface RealmEndpoint {

    @GET
    @Produces(APPLICATION_XRDS_XML)
    Response getRelyingPartyRealmEndpoints(@Context HttpServletRequest request);
}
