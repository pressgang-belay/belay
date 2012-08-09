package org.jboss.pressgangccms.oauth2.shared.rest;

import org.apache.amber.oauth2.common.OAuth;
import org.jboss.pressgangccms.oauth2.shared.data.model.TokenGrantInfo;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Interface for token grant info endpoint.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface TokenGrantInfoEndpoint {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    TokenGrantInfo getTokenGrantInfoForAccessToken(@HeaderParam(OAuth.OAUTH_TOKEN) String token);
}
