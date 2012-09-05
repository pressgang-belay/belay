package org.jboss.pressgangccms.oauth2.authserver.rest.impl;

import com.jamesmurty.utils.XMLBuilder;
import org.jboss.pressgangccms.oauth2.authserver.rest.endpoint.RealmEndpoint;
import org.jboss.pressgangccms.oauth2.authserver.util.AuthServer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.logging.Logger;

import static org.jboss.pressgangccms.oauth2.authserver.util.Constants.*;

/**
 * Provides OpenID Relying Party XRDS document to facilitate verification by OpenID Provider.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public abstract class RealmEndpointImpl implements RealmEndpoint {

    @Inject
    @AuthServer
    private Logger log;

    @Override
    public Response getRelyingPartyRealmEndpoints(@Context HttpServletRequest request) {
        try {
            String response = XMLBuilder.create(XRDS_TAG)
                    .a(XRDS_NS_TAG, XRDS_XRI)
                    .a(NS_TAG, XRD2_NS_XRI)
                        .e(XRD_TAG)
                            .e(SERVICE_TAG)
                                .e(TYPE_TAG).t(OPENID2_RETURN_TO)
                                .up()
                                .e(URI_TAG).t(OAuthEndpointUtil.buildBaseUrl(request) + OPENID_RETURN_URI)
                                .up()
                            .up()
                        .up()
                    .up()
                    .asString();
            return javax.ws.rs.core.Response.ok(response).build();
        } catch (TransformerException e) {
            log.severe("TransformerException during Relying Party realm endpoint discovery: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            log.severe("ParserConfigurationException during Relying Party realm endpoint discovery: " + e.getMessage());
        }
        return javax.ws.rs.core.Response.serverError().entity(REALM_ENDPOINT_ERROR).build();
    }
}