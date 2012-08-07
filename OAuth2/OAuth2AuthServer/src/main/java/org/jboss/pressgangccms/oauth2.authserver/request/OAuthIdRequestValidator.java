package org.jboss.pressgangccms.oauth2.authserver.request;

import org.jboss.pressgangccms.oauth2.authserver.util.Common;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.validators.AbstractValidator;

import javax.servlet.http.HttpServletRequest;

import static org.apache.amber.oauth2.common.error.OAuthError.TokenResponse.INVALID_REQUEST;
import static org.jboss.pressgangccms.oauth2.authserver.util.Common.INVALID_METHOD;

/**
 * Custom OAuth request validator for combined use of OpenID and OAuth.
 * All requests must include an OpenID provider, as well as the standard
 * OAuth parameters.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthIdRequestValidator extends AbstractValidator {

    public OAuthIdRequestValidator() {
        requiredParams.add(OAuth.OAUTH_RESPONSE_TYPE);
        requiredParams.add(OAuth.OAUTH_CLIENT_ID);
        requiredParams.add(OAuth.OAUTH_REDIRECT_URI);
        requiredParams.add(Common.OPENID_PROVIDER);
    }

    @Override
    public void validateMethod(HttpServletRequest request) throws OAuthProblemException {
        String method = request.getMethod();
        if (!method.equals(OAuth.HttpMethod.GET)) {
            throw OAuthProblemException.error(INVALID_REQUEST)
                    .description(INVALID_METHOD);
        }
    }

    @Override
    public void validateContentType(HttpServletRequest request) throws OAuthProblemException {
        // Implement this to restrict content types allowed
    }
}