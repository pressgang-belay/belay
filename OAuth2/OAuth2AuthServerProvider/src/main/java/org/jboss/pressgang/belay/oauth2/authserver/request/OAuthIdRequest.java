package org.jboss.pressgang.belay.oauth2.authserver.request;

import org.apache.amber.oauth2.as.request.OAuthRequest;
import org.apache.amber.oauth2.common.OAuth;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.amber.oauth2.common.utils.OAuthUtils;
import org.apache.amber.oauth2.common.validators.OAuthValidator;
import org.jboss.pressgang.belay.oauth2.authserver.util.Constants;

import javax.servlet.http.HttpServletRequest;

import java.util.Set;

import static org.apache.amber.oauth2.common.error.OAuthError.CodeResponse.UNSUPPORTED_RESPONSE_TYPE;

/**
 * Custom OAuth request type for combined use of OpenID and OAuth.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthIdRequest extends OAuthRequest {

    public OAuthIdRequest(HttpServletRequest request) throws OAuthSystemException, OAuthProblemException {
        super(request);
    }

    @Override
    protected OAuthValidator<HttpServletRequest> initValidator() throws OAuthProblemException, OAuthSystemException {
        // End user authorisation validators
        validators.put(ResponseType.TOKEN.toString(), OAuthIdRequestValidator.class);
        validators.put(ResponseType.CODE.toString(), OAuthIdRequestValidator.class);
        String requestTypeValue = getParam(OAuth.OAUTH_RESPONSE_TYPE);
        if (OAuthUtils.isEmpty(requestTypeValue)) {
            throw OAuthUtils.handleOAuthProblemException(UNSUPPORTED_RESPONSE_TYPE);
        }
        Class<? extends OAuthValidator<HttpServletRequest>> clazz = validators.get(requestTypeValue);
        if (clazz == null) {
            throw OAuthUtils.handleOAuthProblemException(UNSUPPORTED_RESPONSE_TYPE);
        }
        return OAuthUtils.instantiateClass(clazz);
    }

    public String getState() {
        return getParam(OAuth.OAUTH_STATE);
    }

    public String getProvider() {
        return getParam(Constants.OPENID_PROVIDER);
    }

    public OAuthIdRequestParams copyOAuthParams() {
        return new OAuthIdRequestParams()
                .setClientId(this.getClientId())
                .setClientSecret(this.getClientSecret())
                .setProvider(this.getProvider())
                .setRedirectUri(this.getRedirectURI())
                .setScopes(this.getScopes())
                .setResponseType(getParam(OAuth.OAUTH_RESPONSE_TYPE))
                .setState(this.getState());
    }

    public static class OAuthIdRequestParams {
        private String clientId;
        private String clientSecret;
        private String provider;
        private String redirectUri;
        private Set<String> scopes;
        private String responseType;
        private String state;

        public OAuthIdRequestParams() {
        }

        public String getClientId() {
            return clientId;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public String getProvider() {
            return provider;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public Set<String> getScopes() {
            return scopes;
        }

        public String getResponseType() {
            return responseType;
        }

        public String getState() {
            return state;
        }

        public OAuthIdRequestParams setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public OAuthIdRequestParams setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
            return this;
        }

        public OAuthIdRequestParams setProvider(String provider) {
            this.provider = provider;
            return this;
        }

        public OAuthIdRequestParams setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
            return this;
        }

        public OAuthIdRequestParams setScopes(Set<String> scopes) {
            this.scopes = scopes;
            return this;
        }

        public OAuthIdRequestParams setResponseType(String responseType) {
            this.responseType = responseType;
            return this;
        }

        public OAuthIdRequestParams setState(String state) {
            this.state = state;
            return this;
        }
    }
}
