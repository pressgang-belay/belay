package org.jboss.pressgang.belay.oauth2.authserver.exception;

import org.hibernate.HibernateException;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.join;

/**
 * Exception Mapper to catch HibernateExceptions and hide internal error information.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@Provider
public class HibernateExceptionMapper implements ExceptionMapper<HibernateException> {

    @Inject
    private Logger log;

    @Override
    public Response toResponse(HibernateException e) {
        log.warning("Mapping HibernateException to server error: " + e.getMessage() + "\n" + join(e.getStackTrace(), '\n'));
        return Response.serverError().build();
    }
}
