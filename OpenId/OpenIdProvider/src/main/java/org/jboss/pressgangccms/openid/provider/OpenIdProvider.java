package org.jboss.pressgangccms.openid.provider;

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
 *
 * @author kamiller@redhat.com (Katie Miller)
 */

import com.jamesmurty.utils.XMLBuilder;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.logging.Logger;

import static org.jboss.pressgangccms.openid.provider.Common.*;
import static org.jboss.pressgangccms.openid.provider.OpenIdProviderManager.OpenIdMessage;

@Path("/provider")
@RequestScoped
public class OpenIdProvider {

    private final String SECURE_PAGE_URL = "https://localhost:8443/OpenIdProvider/securepage.jsp";

    @Inject
    private Logger log;

    private OpenIdProviderManager providerManager = createProviderManager();

    private OpenIdProviderManager createProviderManager() {
        OpenIdProviderManager manager = new OpenIdProviderManager();
        manager.setEndPoint(PROVIDER_ENDPOINT);
        return manager;
    }

    @Produces({ APPLICATION_XRDS_XML, TEXT_PLAIN })
    @GET
    public Response redirectGet(@Context HttpServletRequest request, @QueryParam(ID) String id) throws IOException {
        if (id == null || id.length() == 0) {
            return processRequest(request);
        } else {
            try {
                log.info("Building XRDS response for id " + id);
                String response = XMLBuilder.create(XRDS_TAG)
                        .a(XRDS_NS_TAG, XRDS_XRI)
                        .a(NS_TAG, XRD2_NS_XRI)
                            .e(XRD_TAG)
                                .e(SERVICE_TAG)
                                .a(PRIORITY, FIRST)
                                    .e(TYPE_TAG).t(OPENID2_SIGNON)
                                    .up()
                                    .e(TYPE_TAG).t(OPENID_AX_EXT)
                                    .up()
                                    .e(URI_TAG).t(providerManager.getEndPoint())
                                    .up()
                                .up()
                            .up()
                        .up()
                        .asString();
                return Response.ok(response).build();
            } catch (TransformerException e) {
                log.severe("TransformerException during user endpoint discovery: " + e.getMessage());
            } catch (ParserConfigurationException e) {
                log.severe("ParserConfigurationException during user endpoint discovery: " + e.getMessage());
            }
            return Response.serverError().entity(USER_ENDPOINT_ERROR).build();
        }
    }

    @POST
    public Response processRequest(@Context HttpServletRequest request) throws IOException {
        log.info("Processing OpenId request");

        Response.ResponseBuilder builder;
        HttpSession session = request.getSession();
        OpenIdParameterList requestParameters;

        if (COMPLETE.equals(request.getParameter(ACTION))) {
            log.info("Completing OpenId request");
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
                // This is not an error for us as we will determine the identifier
                log.info("No OpenId identity supplied");
            }
        }

        String mode = requestParameters.hasParameter(OPENID_MODE) ?
                requestParameters.getParameterValue(OPENID_MODE) : null;
        log.info("OpenId mode is: " + mode);

        OpenIdMessage responseMessage;
        String responseText;

        if (ASSOCIATE.equals(mode)) {
            log.info("Processing association request");
            // Process an association request
            responseMessage = providerManager.processAssociationRequest(requestParameters);
            responseText = responseMessage.getResponseText();
        } else if (CHECKID_SETUP.equals(mode)
                || CHECKID_IMMEDIATE.equals(mode)) {
            log.info("Processing checkid request");
            // Interact with the user and obtain data needed to continue
            // List userData = userInteraction(requestParameters);
            String userSelectedId;
            String userSelectedClaimedId;
            Boolean authenticatedAndApproved;

            if ((session.getAttribute(AUTHENTICATED_APPROVED) == null) ||
                    ((session.getAttribute(AUTHENTICATED_APPROVED)) == Boolean.FALSE)) {
                log.info("User not yet authenticated");
                session.setAttribute(PARAM_LIST, requestParameters);
                URI redirectUri = URI.create(this.SECURE_PAGE_URL);
                log.info("Redirecting to secure page: " + redirectUri);
                builder = Response.seeOther(redirectUri);
                return builder.build();
            } else {
                log.info("User has been authenticated");
                userSelectedClaimedId = (String) session.getAttribute(OPENID_IDENTITY);
                log.info("OpenId identity: " + userSelectedClaimedId);
                userSelectedId = (String) session.getAttribute(OPENID_CLAIMED);
                log.info("Claimed Id: " + userSelectedId);
                authenticatedAndApproved = (Boolean) session.getAttribute(AUTHENTICATED_APPROVED);
                Principal user = request.getUserPrincipal();
                if (userSelectedId == null && user != null && user.getName() != null) {
                    String userIdentifier = providerManager.getEndPoint() + QUERY_STRING_MARKER + ID
                            + KEY_VALUE_SEPARATOR + user.getName();
                    log.info("Setting OpenId identity to: " + userIdentifier);
                    userSelectedClaimedId = userIdentifier;
                    userSelectedId = userIdentifier;
                } else {
                    // If we can't resolve a user identifier, don't authenticate user
                    if (authenticatedAndApproved) {
                        log.warning("User was authenticated but could not resolve identifier so revoking authentication");
                    }
                    authenticatedAndApproved = false;
                }
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
            log.info("Processing check authentication request");
            // Processing a verification request
            responseMessage = providerManager.verify(requestParameters);
            responseText = responseMessage.getResponseText();
        } else {
            log.warning("Reached an error state");
            // Error response
            responseMessage = providerManager.getDirectError(UNKNOWN_REQUEST_ERROR);
            responseText = responseMessage.getResponseText();
        }

        log.info("Sending response=" + responseText);

        builder = Response.ok().entity(responseText);
        return builder.build();
    }
}