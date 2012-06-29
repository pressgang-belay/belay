package com.redhat.gwtsecurity.client.oauth;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

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

    void sendRequest(String token, final RequestCallback callback) throws RequestException {
        setOAuthHeader(token);
        builder.sendRequest(builder.getRequestData(), callback);
    }

    private void setOAuthHeader(String token) {
        builder.setHeader("Authorization", "OAuth " + "oauth_signature_method " + "Bearer " + token);
    }
}
