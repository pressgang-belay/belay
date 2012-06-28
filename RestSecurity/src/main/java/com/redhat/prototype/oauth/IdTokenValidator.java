package com.redhat.prototype.oauth;

import com.redhat.prototype.Common;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.error.OAuthError;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.validators.AbstractValidator;

import javax.servlet.http.HttpServletRequest;

public class IdTokenValidator extends AbstractValidator {

    public IdTokenValidator() {
        requiredParams.add(OAuth.OAUTH_RESPONSE_TYPE);
        requiredParams.add(OAuth.OAUTH_CLIENT_ID);
        requiredParams.add(OAuth.OAUTH_REDIRECT_URI);
        requiredParams.add(Common.OPENID_PROVIDER);
    }

    @Override
    public void validateMethod(HttpServletRequest request) throws OAuthProblemException {
        String method = request.getMethod();
        if (!method.equals(OAuth.HttpMethod.GET)) {
            throw OAuthProblemException.error(OAuthError.TokenResponse.INVALID_REQUEST)
                    .description("Method not set to GET.");
        }
    }

    @Override
    public void validateContentType(HttpServletRequest request) throws OAuthProblemException {
        // Implement this to restrict content types allowed
    }
}
