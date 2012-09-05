package org.jboss.pressgangccms.oauth2.authserver.sample.rest;

import org.jboss.pressgangccms.oauth2.authserver.rest.impl.TokenEndpointImpl;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

/**
 * @author katie@codemiller.com (Katie Miller)
 */
@RequestScoped
@Path("/auth/token")
public class TokenWebService extends TokenEndpointImpl {
}
