package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.http.client.*;

import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.*;

/**
 * Wraps standard GWT RequestBuilder for the creation of authorized requests.
 * Designed for use with OAuthHandler.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthRequest {

    RequestBuilder builder;

    public OAuthRequest(RequestBuilder.Method method, String url) {
        this.builder = new RequestBuilder(method, url);
    }

    public OAuthRequest setHeader(String header, String value) {
        builder.setHeader(header, value);
        return this;
    }

    public OAuthRequest setTimeoutMillis(int timeoutMillis) {
        builder.setTimeoutMillis(timeoutMillis);
        return this;
    }

    public OAuthRequest setRequestData(String requestData) {
        builder.setRequestData(requestData);
        return this;
    }

    public String getUrl() {
        return builder.getUrl();
    }

    public String getHttpMethod() {
        return builder.getHTTPMethod();
    }

    public String getHeader(String header) {
        return builder.getHeader(header);
    }

    public int getTimeoutMillis() {
        return builder.getTimeoutMillis();
    }

    public String getRequestData() {
        return builder.getRequestData();
    }

    void sendRequest(final String token, final OAuthHandler handler, final AuthorizationRequest authorization,
                     final RequestCallback callback)
            throws RequestException {
        setOAuthHeader(token);
        builder.sendRequest(builder.getRequestData(), new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                handler.processOAuthRequestResponse(request, response, authorization, callback);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                callback.onError(request, exception);
            }
        });
    }

    void setOAuthHeader(String token) {
        builder.setHeader(AUTHORIZATION_HEADER, buildOAuthRequestString(token));
    }

    String buildOAuthRequestString(String token) {
        return new StringBuilder(OAUTH_HEADER_NAME)
                .append(SPACE)
                .append(token)
                .toString();
    }
}
