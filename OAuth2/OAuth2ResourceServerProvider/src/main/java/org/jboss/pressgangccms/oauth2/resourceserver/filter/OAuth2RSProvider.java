package org.jboss.pressgangccms.oauth2.resourceserver.filter;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;
import org.apache.amber.oauth2.rsfilter.OAuthRSProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuth2RSProvider implements OAuthRSProvider {

    private HttpServletResponse httpServletResponse;

    @Override
    public OAuthDecision validateRequest(String realm, String token, HttpServletRequest request)
            throws OAuthProblemException {
        return new OAuth2RSDecision(realm, token, request, httpServletResponse);
    }

    public void setServletResponse(HttpServletResponse response) {
        httpServletResponse = response;
    }
}
