package org.jboss.pressgang.belay.oauth2.authserver.exception;

import org.jboss.pressgang.belay.oauth2.authserver.util.AuthServer;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.join;

/**
 * Exception Mapper to catch WebApplicationExceptions and hide internal error information.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {

    @Inject
    @AuthServer
    private Logger log;

    @Override
    public Response toResponse(WebApplicationException e) {
        log.warning("Mapping WebApplicationException: " + e.getMessage() + "\n" + join(e.getStackTrace(), '\n'));
        if (e.getResponse().getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            return e.getResponse();
        }
        return Response.serverError().build();
    }
}
