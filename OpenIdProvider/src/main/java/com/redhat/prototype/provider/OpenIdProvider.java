package com.redhat.prototype.provider;

/**
 * This class is based on org.picketlink.identity.federation.api.openid.provider.OpenIDProvider by Anil Saldhana.
 * It has been modified from the original, in 2012.
 *
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import static com.redhat.prototype.provider.OpenIdProviderManager.OpenIdMessage;

@Path("/provider")
@RequestScoped
public class OpenIdProvider {

    @Inject
    private Logger log;

    private final String SECURE_PAGE_NAME = "https://localhost:8443/OpenIdProvider/securepage.jsp";
    private OpenIdProviderManager providerManager = new OpenIdProviderManager();

    @GET
    public Response redirectGet(@Context HttpServletRequest request) throws IOException {
        return processRequest(request);
    }

    @POST
    public Response processRequest(@Context HttpServletRequest request) throws IOException {

        log.info("Processing OpenId request");

        Response.ResponseBuilder builder = null;
        HttpSession session = request.getSession();

        if (providerManager.getEndPoint() == null) {
            String endpoint = request.getScheme() + "://" +
                    request.getServerName() + ":" +
                    request.getServerPort() +
                    request.getContextPath() +
                    "/openid/provider/";
            log.info("Setting OP endpoint URL to: " + endpoint);
            providerManager.setEndPoint(endpoint);
        }

        OpenIdParameterList requestParameters;

        if ("complete".equals(request.getParameter("_action"))) // Completing the authz and authn process by redirecting here
        {
            OpenIdParameterList list = (OpenIdParameterList) session.getAttribute("parameterlist"); // On a redirect from the OP authn & authz sequence
            if (list != null) {
                requestParameters = list;
            } else {
                throw new RuntimeException("No OpenId parameters found");
            }
        } else {
            requestParameters = new OpenIdParameterList(request.getParameterMap());
            if (requestParameters.hasParameter("openid.identity") && requestParameters.getParameter("openid.identity").getValue().length() > 0) {
                session.setAttribute("openid.identity", requestParameters.getParameter("openid.identity").getValue());
            } else {
                builder = Response.status(Response.Status.BAD_REQUEST).entity("OpenId identity was not supplied");
                return builder.build();
            }
        }

        log.info("About to check for openid.mode param");
        String mode = requestParameters.hasParameter("openid.mode") ?
                requestParameters.getParameterValue("openid.mode") : null;

        OpenIdMessage responseMessage;
        String responseText;

        if ("associate".equals(mode)) {
            // --- process an association request ---
            responseMessage = providerManager.processAssociationRequest(requestParameters);
            responseText = responseMessage.getResponseText();
        } else if ("checkid_setup".equals(mode)
                || "checkid_immediate".equals(mode)) {
            // interact with the user and obtain data needed to continue
            //List userData = userInteraction(requestParameters);
            String userSelectedId = null;
            String userSelectedClaimedId = null;
            Boolean authenticatedAndApproved = Boolean.FALSE;

            if ((session.getAttribute("authenticatedAndApproved") == null) ||
                    (((Boolean) session.getAttribute("authenticatedAndApproved")) == Boolean.FALSE)) {
                session.setAttribute("parameterlist", requestParameters);
                URI redirectUri = URI.create(this.SECURE_PAGE_NAME);
                log.info("Redirecting to secure page: " + redirectUri);
                builder = Response.seeOther(redirectUri);
                return builder.build();
            } else {
                userSelectedId = (String) session.getAttribute("openid.claimed_id");
                userSelectedClaimedId = (String) session.getAttribute("openid.identity");
                authenticatedAndApproved = (Boolean) session.getAttribute("authenticatedAndApproved");
                // Remove the parameterlist so this provider can accept requests from elsewhere
                session.removeAttribute("parameterlist");
                session.setAttribute("authenticatedAndApproved", Boolean.FALSE); // Makes you authorize each and every time
            }

            // --- process an authentication request ---
            responseMessage = providerManager.processAuthenticationRequest(requestParameters,
                    userSelectedId,
                    userSelectedClaimedId,
                    authenticatedAndApproved);

            if (responseMessage.isSuccessful()) {
                URI redirectUri = URI.create(responseMessage.getDestinationURL(true));
                builder = Response.seeOther(redirectUri);
                log.info("Redirecting to: " + redirectUri);
                return builder.build();
            } else {
                responseText = "<pre>" + responseMessage.getResponseText() + "</pre>";
            }
        } else if ("check_authentication".equals(mode)) {
            // --- processing a verification request ---
            responseMessage = providerManager.verify(requestParameters);
            responseText = responseMessage.getResponseText();
        } else {
            // --- error response ---
            responseMessage = providerManager.getDirectError("Unknown request");
            responseText = responseMessage.getResponseText();
        }

        log.info("Sending response=" + responseText);

        builder = Response.ok().entity(responseText);
        return builder.build();
    }
}