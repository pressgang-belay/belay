package com.redhat.prototype.oauth.login;

import org.apache.amber.oauth2.as.request.OAuthRequest;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.apache.amber.oauth2.common.validators.OAuthValidator;

import javax.servlet.http.HttpServletRequest;

import static com.redhat.prototype.util.Common.INVALID_RESPONSE_TYPE;
import static com.redhat.prototype.util.Common.MISSING_RESPONSE_TYPE;

public class OAuthIdRequest extends OAuthRequest {

    public OAuthIdRequest(HttpServletRequest request) throws OAuthSystemException, OAuthProblemException {
        super(request);
    }

    @Override
    protected OAuthValidator initValidator() throws OAuthProblemException, OAuthSystemException {
        // End user authorisation validators
        validators.put(ResponseType.TOKEN.toString(), OAuthIdRequestValidator.class);
        String requestTypeValue = getParam(OAuth.OAUTH_RESPONSE_TYPE);
        if (OAuthUtils.isEmpty(requestTypeValue)) {
            throw OAuthUtils.handleOAuthProblemException(MISSING_RESPONSE_TYPE);
        }
        Class clazz = validators.get(requestTypeValue);
        if (clazz == null) {
            throw OAuthUtils.handleOAuthProblemException(INVALID_RESPONSE_TYPE);
        }
        return (OAuthValidator)OAuthUtils.instantiateClass(clazz);
    }

    public String getState() {
        return getParam(OAuth.OAUTH_STATE);
    }
}
