package org.jboss.pressgangccms.oauth2.gwt.client.oauth;

import com.google.gwt.http.client.*;

import static org.jboss.pressgangccms.oauth2.gwt.client.oauth.Constants.*;

/**
 * Wraps standard GWT RequestBuilder for the creation of authorised requests.
 * Designed for use with OAuthHandler.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthRequest {

    private RequestBuilder builder;

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

    void sendRequest(final String token, final RequestCallback callback, final OAuthHandler handler,
                     final AuthorisationRequest authorisation)
            throws RequestException {
        setOAuthHeader(token);
        builder.sendRequest(builder.getRequestData(), new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                handler.processOAuthRequestResponse(request, response, callback, authorisation);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                callback.onError(request, exception);
            }
        });
    }

    private void setOAuthHeader(String token) {
        builder.setHeader(AUTHORISATION_HEADER, buildOAuthRequestString(token));
    }

    private String buildOAuthRequestString(String token) {
        return new StringBuilder(OAUTH_HEADER_NAME)
                .append(SPACE)
                .append(token)
                .toString();
    }
}
