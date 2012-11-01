package org.jboss.pressgang.belay.oauth2.authserver.rest.impl;

import com.googlecode.jatl.Html;
import org.jboss.pressgang.belay.oauth2.authserver.request.OAuthIdRequest;
import org.jboss.pressgang.belay.oauth2.authserver.rest.endpoint.UserConsentEndpoint;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.io.StringWriter;
import java.util.Set;
import java.util.logging.Logger;

import static org.apache.amber.oauth2.common.OAuth.OAUTH_SCOPE;
import static org.jboss.pressgang.belay.oauth2.authserver.rest.impl.OAuthEndpointUtil.getOAuthIdRequestParamsFromSession;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.CLIENT_NAME;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Constants.ORIGINAL_REQUEST_PARAMS;
import static org.jboss.pressgang.belay.oauth2.authserver.util.Resources.*;

/**
 * Endpoint handling end-user approval of OAuth2 authorization attempts. Presents a web form to the end-user, asking
 * them to approve a client application's access to their OpenID data and the requested scopes.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class UserConsentEndpointImpl implements UserConsentEndpoint {

    @Inject
    private Logger log;

    @Override
    public String getUserConsentForm(@Context HttpServletRequest request) {
        final String clientName = OAuthEndpointUtil.getStringAttributeFromSession(request, log, CLIENT_NAME, "Application client name");
        final OAuthIdRequest.OAuthIdRequestParams oAuthRequestParams = getOAuthIdRequestParamsFromSession(request, ORIGINAL_REQUEST_PARAMS);
        final Set<String> requestedScopes = oAuthRequestParams.getScopes();
        log.info("Serving end-user approval form for client application " + clientName);

        final String header = "Authorization Required";
        StringWriter stringWriter = new StringWriter();
        new Html(stringWriter) {{
            html();
            head().title().text(header);
            if (endUserConsentFormCssLocation != null && (!endUserConsentFormCssLocation.isEmpty())) {
                link().href(endUserConsentFormCssLocation).rel("stylesheet").type("text/css");
            }
            end();
            body();
            h3().id("userConsentHead").text(header).end();
            p().id("userConsentText").text("The " + clientName + " application wants to authenticate you based on your OpenID account/s.").end();
            if (requestedScopes != null && (!requestedScopes.isEmpty())) {
                p().id("userConsentScopesText").text("The application has also requested access to resources accessible under the following scopes:").end();
                ul();
                for (String requestedScope : requestedScopes) {
                    li().text(requestedScope).end();
                }
                end();
            }
            p().id("userApprovalText").text("Do you approve this access?").end();
            form().id("userConsentForm").action(restEndpointBasePath + authEndpoint).method("post")
                    .text("Approve").input().type("radio").name("user_consent").id("approve").value("TRUE").text(" ")
                    .text("Deny").input().type("radio").name("user_consent").id("deny").value("FALSE").checked("TRUE").p().end()
                    .button().id("submitButton").text("Submit").end();
            endAll();
            done();
        }};
        return stringWriter.getBuffer().toString();
    }
}
