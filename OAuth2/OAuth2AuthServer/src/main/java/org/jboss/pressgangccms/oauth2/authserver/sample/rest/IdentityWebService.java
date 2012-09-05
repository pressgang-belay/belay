package org.jboss.pressgangccms.oauth2.authserver.sample.rest;

import org.jboss.pressgangccms.oauth2.authserver.rest.impl.IdentityEndpointImpl;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
@Path("/auth/identity")
public class IdentityWebService extends IdentityEndpointImpl {
}
