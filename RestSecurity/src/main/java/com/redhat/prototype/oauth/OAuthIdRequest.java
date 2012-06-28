package com.redhat.prototype.oauth;

import org.apache.amber.oauth2.as.request.OAuthRequest;
import org.apache.amber.oauth2.as.validator.CodeTokenValidator;
import org.apache.amber.oauth2.as.validator.CodeValidator;
import org.apache.amber.oauth2.as.validator.TokenValidator;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.apache.amber.oauth2.common.validators.OAuthValidator;

import javax.servlet.http.HttpServletRequest;

public class OAuthIdRequest extends OAuthRequest {

    public OAuthIdRequest(HttpServletRequest request) throws OAuthSystemException, OAuthProblemException {
        super(request);
    }

    @Override
    protected OAuthValidator initValidator() throws OAuthProblemException, OAuthSystemException {
        // end user authorisation validators
        validators.put(ResponseType.TOKEN.toString(), IdTokenValidator.class);
        String requestTypeValue = getParam(OAuth.OAUTH_RESPONSE_TYPE);
        if (OAuthUtils.isEmpty(requestTypeValue)) {
            throw OAuthUtils.handleOAuthProblemException("Missing response_type parameter value");
        }
        Class clazz = validators.get(requestTypeValue);
        if (clazz == null) {
            throw OAuthUtils.handleOAuthProblemException("Unsupported or invalid response_type parameter value");
        }
        return (OAuthValidator)OAuthUtils.instantiateClass(clazz);
    }

    public String getState() {
        return getParam(OAuth.OAUTH_STATE);
    }
}
