package org.jboss.pressgangccms.oauth2.authserver.sample.rest;

import org.jboss.pressgangccms.oauth2.authserver.rest.impl.RealmEndpointImpl;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
@Path("/auth")
public class RealmWebService extends RealmEndpointImpl {
}
