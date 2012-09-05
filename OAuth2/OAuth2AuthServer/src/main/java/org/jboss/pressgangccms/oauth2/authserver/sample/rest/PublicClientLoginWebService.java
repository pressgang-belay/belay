package org.jboss.pressgangccms.oauth2.authserver.sample.rest;

import org.jboss.pressgangccms.oauth2.authserver.rest.impl.PublicClientLoginEndpointImpl;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
@Path("/auth/login")
public class PublicClientLoginWebService extends PublicClientLoginEndpointImpl {
}
