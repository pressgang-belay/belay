package org.jboss.pressgang.belay.oauth2.authserver.sample.rest;

import org.jboss.pressgang.belay.oauth2.authserver.rest.impl.PublicClientAuthEndpointImpl;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
@Path("/auth/authorize")
public class PublicClientAuthWebService extends PublicClientAuthEndpointImpl {
}
