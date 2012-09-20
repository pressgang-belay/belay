package org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public interface CompleteAssociationEndpoint {
    @GET
    Response completeAssociation(@Context HttpServletRequest request) throws OAuthProblemException,
            OAuthSystemException;
}
