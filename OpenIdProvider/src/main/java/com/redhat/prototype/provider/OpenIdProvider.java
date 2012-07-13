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

import static com.redhat.prototype.provider.Common.*;
import static com.redhat.prototype.provider.OpenIdProviderManager.OpenIdMessage;

@Path("/provider")
@RequestScoped
public class OpenIdProvider {

    private final String SECURE_PAGE_URL = "https://localhost:8443/OpenIdProvider/securepage.jsp";

    @Inject
    private Logger log;

    private OpenIdProviderManager providerManager = new OpenIdProviderManager();

    @GET
    public Response redirectGet(@Context HttpServletRequest request) throws IOException {
        return processRequest(request);
    }

    @POST
    public Response processRequest(@Context HttpServletRequest request) throws IOException {
        log.info("Processing OpenId request");

        Response.ResponseBuilder builder;
        HttpSession session = request.getSession();

        if (providerManager.getEndPoint() == null) {
            String endpoint = request.getScheme() + SCHEME_END +
                    request.getServerName() + COLON +
                    request.getServerPort() +
                    request.getContextPath() +
                    PROVIDER_ENDPOINT;
            log.info("Setting OP endpoint URL to: " + endpoint);
            providerManager.setEndPoint(endpoint);
        }

        OpenIdParameterList requestParameters;

        if (COMPLETE.equals(request.getParameter(ACTION))) {
            // Completing the authz and authn process by redirecting here
            OpenIdParameterList list = (OpenIdParameterList) session.getAttribute(PARAM_LIST);
            // On a redirect from the OP authn/authz sequence
            if (list != null) {
                requestParameters = list;
            } else {
                throw new RuntimeException(NO_OPENID_PARAMS_ERROR);
            }
        } else {
            requestParameters = new OpenIdParameterList(request.getParameterMap());
            if (requestParameters.hasParameter(OPENID_IDENTITY)
                    && requestParameters.getParameter(OPENID_IDENTITY).getValue().length() > 0) {
                session.setAttribute(OPENID_IDENTITY, requestParameters.getParameter(OPENID_IDENTITY).getValue());
                log.info("OpenId identity supplied is: " + session.getAttribute(OPENID_IDENTITY));
            } else {
                builder = Response.status(Response.Status.BAD_REQUEST).entity(MISSING_IDENTITY_ERROR);
                return builder.build();
            }
        }

        log.info("About to check for openid.mode param");
        String mode = requestParameters.hasParameter(OPENID_MODE) ?
                requestParameters.getParameterValue(OPENID_MODE) : null;

        OpenIdMessage responseMessage;
        String responseText;

        if (ASSOCIATE.equals(mode)) {
            // Process an association request
            responseMessage = providerManager.processAssociationRequest(requestParameters);
            responseText = responseMessage.getResponseText();
        } else if (CHECKID_SETUP.equals(mode)
                || CHECKID_IMMEDIATE.equals(mode)) {
            // Interact with the user and obtain data needed to continue
            // List userData = userInteraction(requestParameters);
            String userSelectedId;
            String userSelectedClaimedId;
            Boolean authenticatedAndApproved;

            if ((session.getAttribute(AUTHENTICATED_APPROVED) == null) ||
                    ((session.getAttribute(AUTHENTICATED_APPROVED)) == Boolean.FALSE)) {
                session.setAttribute(PARAM_LIST, requestParameters);
                URI redirectUri = URI.create(this.SECURE_PAGE_URL);
                log.info("Redirecting to secure page: " + redirectUri);
                builder = Response.seeOther(redirectUri);
                return builder.build();
            } else {
                userSelectedId = (String) session.getAttribute(OPENID_CLAIMED);
                log.info("Claimed Id: " + userSelectedId);
                userSelectedClaimedId = (String) session.getAttribute(OPENID_IDENTITY);
                log.info("OpenId identity: " + userSelectedClaimedId);
                authenticatedAndApproved = (Boolean) session.getAttribute(AUTHENTICATED_APPROVED);
                // Remove the parameterlist so this provider can accept requests from elsewhere
                session.removeAttribute(PARAM_LIST);
                session.setAttribute(AUTHENTICATED_APPROVED, Boolean.FALSE); // Makes you authorise each and every time
            }

            // Process an authentication request
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
                responseText = PRE_TAG_OPEN + responseMessage.getResponseText() + PRE_TAG_CLOSE;
            }
        } else if (CHECK_AUTHENTICATION.equals(mode)) {
            // Processing a verification request
            responseMessage = providerManager.verify(requestParameters);
            responseText = responseMessage.getResponseText();
        } else {
            // Error response
            responseMessage = providerManager.getDirectError(UNKNOWN_REQUEST_ERROR);
            responseText = responseMessage.getResponseText();
        }

        log.info("Sending response=" + responseText);

        builder = Response.ok().entity(responseText);
        return builder.build();
    }
}