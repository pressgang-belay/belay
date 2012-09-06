package org.jboss.pressgang.belay.oauth2.shared.rest;

import org.apache.amber.oauth2.common.OAuth;
import org.jboss.pressgang.belay.oauth2.shared.data.model.AccessTokenExpiryInfo;

import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Interface for extending the expiry time of an access token.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface TokenExpiryExtensionEndpoint {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    AccessTokenExpiryInfo extendAccessTokenExpiry(@HeaderParam(OAuth.OAUTH_TOKEN) String token);
}
