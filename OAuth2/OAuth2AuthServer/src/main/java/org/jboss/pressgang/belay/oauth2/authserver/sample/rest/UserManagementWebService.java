package org.jboss.pressgang.belay.oauth2.authserver.sample.rest;

import org.jboss.pressgang.belay.oauth2.authserver.rest.impl.UserManagementEndpointImpl;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RequestScoped
@Path("/auth/user")
public class UserManagementWebService extends UserManagementEndpointImpl {
}
