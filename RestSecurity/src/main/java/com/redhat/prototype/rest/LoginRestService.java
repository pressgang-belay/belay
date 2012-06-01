package com.redhat.prototype.rest;

import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;

@Path("/login")
@RequestScoped
public class LoginRestService {
	
	@Inject
	private Logger log;
	
	

}
