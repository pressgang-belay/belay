package org.jboss.pressgang.belay.oauth2.authserver.sample.rest;

import org.jboss.pressgang.belay.oauth2.authserver.rest.impl.AuthEndpointImpl;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
@Path("/auth/confidential/authorize")
public class ConfidentialClientAuthWebService extends AuthEndpointImpl {
}
