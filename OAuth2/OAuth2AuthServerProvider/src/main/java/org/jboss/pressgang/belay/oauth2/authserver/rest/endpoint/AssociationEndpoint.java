package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.net.URISyntaxException;

import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.NEW_IDENTITY_PRIMARY;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface AssociationEndpoint {
    @GET
    Response associateIdentity(@Context HttpServletRequest request,
                               @QueryParam(NEW_IDENTITY_PRIMARY) Boolean newIsPrimary) throws URISyntaxException;
}
