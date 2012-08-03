package org.jboss.pressgangccms.oauth2.resourceserver.filter;

import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.rsfilter.OAuthDecision;
import org.apache.amber.oauth2.rsfilter.OAuthRSProvider;

import javax.servlet.http.HttpServletRequest;import java.lang.Override;import java.lang.String;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuth2RSProvider implements OAuthRSProvider {

    @Override
    public OAuthDecision validateRequest(String realm, String token, HttpServletRequest request)
            throws OAuthProblemException {
        return new OAuth2RSDecision(realm, token, request);
    }
}
