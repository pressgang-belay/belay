package org.jboss.pressgangccms.oauth.gwt.client.oauth;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.http.client.*;
import com.google.gwt.json.client.*;

import static org.jboss.pressgangccms.oauth.gwt.client.oauth.Common.*;

/**
 * Includes code from the Auth class in the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/),
 * written by Jason Hall. Library code has been modified.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * Provides methods to manage authentication flow.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public abstract class Authoriser {

    private AuthorisationRequest lastAuthRequest;
    private Callback<String, Throwable> lastCallback;

    /**
     * Instance of the {@link Authoriser} to use in a GWT application.
     */
    public static final Authoriser get() {
        return AuthoriserImpl.INSTANCE;
    }

    private final UrlCodex urlCodex;
    private final Clock clock;
    final OAuthTokenStore tokenStore;
    final Scheduler scheduler;
    String oAuthWindowUrl;

    Authoriser(OAuthTokenStore tokenStore, Clock clock, UrlCodex urlCodex, Scheduler scheduler,
               String oAuthWindowUrl) {
        this.tokenStore = tokenStore;
        this.clock = clock;
        this.urlCodex = urlCodex;
        this.scheduler = scheduler;
        this.oAuthWindowUrl = oAuthWindowUrl;
    }

    /**
     * Ensure the user has a valid access token from an OAuth 2.0 provider,
     * requesting one if necessary.
     *
     * If it can be determined that the user has already granted access, and the
     * token has not yet expired, and that the token will not expire soon, the
     * existing token will be passed to the callback -- provided the request flag
     * is not set to force a call to the OAuth provider.
     *
     * If a refresh token is available, this will be used to obtain a new access
     * token.
     *
     * Otherwise, a popup window will be displayed which may prompt the user to
     * login and/or grant access. If the user has already granted access the popup
     * will close and the token will be passed to the callback. If access
     * hasn't been granted the user will be prompted, and when they grant access,
     * the token will be passed to the callback.
     *
     * @param request  Request for authentication.
     * @param callback Callback to pass the token to when access has been granted.
     */
    public void authorise(AuthorisationRequest request, final Callback<String, Throwable> callback) {
        lastAuthRequest = request;
        lastCallback = callback;

        // Try to look up the token we have stored.
        final TokenInfo info = getToken(request);
        if (request.isForceNewRequest() || info == null || info.expires == null || info.refreshToken == null) {
            // A new request has been forced, token wasn't found or doesn't have an expiration, or there is
            // no refresh token to use to get a new one. Request a new access token.
            String authUrl = new StringBuilder(request.toLoginUrl(urlCodex))
                    .append(PARAMETER_SEPARATOR)
                    .append(REDIRECT_URI)
                    .append(KEY_VALUE_SEPARATOR)
                    .append(urlCodex.encode(oAuthWindowUrl))
                    .toString();
            doAuthLogin(authUrl, callback);
        } else if (expiringInFiveOrExpired(info)) {
            // Token needs to be refreshed
            doRefresh(request, info, callback);
        } else {
            // Token was found and is good, immediately execute the callback with the
            // access token.
            scheduler.scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    callback.onSuccess(info.accessToken);
                }
            });
        }
    }

    /**
     * Returns whether or not the token will be expiring within the next five
     * minutes or has already expired.
     */
    boolean expiringInFiveOrExpired(TokenInfo info) {
        return Double.valueOf(info.expires) < (clock.now() + FIVE_MINUTES);
    }

    /**
     * Get the OAuth 2.0 token for which this application may not have already
     * been granted access, by displaying a popup to the user.
     */
    abstract void doAuthLogin(String authUrl, Callback<String, Throwable> callback);

    /**
     * Refresh the OAuth 2.0 token for this application.
     */
    void doRefresh(final AuthorisationRequest authorisationRequest, TokenInfo tokenInfo,
                   final Callback<String, Throwable> callback) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, authorisationRequest.getTokenUrl());
        builder.setHeader(CONTENT_TYPE, FORM_URLENCODED);
        try {
            builder.sendRequest(buildOAuthRefreshTokenString(authorisationRequest, tokenInfo.refreshToken),
                    new RequestCallback() {
                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (SC_OK == response.getStatusCode()) {
                        String responseJson = response.getText();
                        try {
                            // Parse the response text into JSON
                            JSONValue jsonValue = JSONParser.parseStrict(responseJson);
                            JSONObject jsonObject = jsonValue.isObject();

                            if (jsonObject != null) {
                                TokenInfo newInfo = extractTokenInfo(jsonObject);
                                setToken(authorisationRequest, newInfo);
                                callback.onSuccess(newInfo.accessToken);
                            } else {
                                throw new JSONException();
                            }
                        } catch (Throwable t) {
                            callback.onFailure(t);
                        }
                    } else {
                        // TODO create exception class/es for these failures
                        callback.onFailure(new RuntimeException("Could not obtain login authorisation. Response status: "
                                + response.getStatusCode()));
                    }
                }

                @Override
                public void onError(Request request, Throwable exception) {
                    callback.onFailure(exception);
                }
            });
        } catch (RequestException e) {
            callback.onFailure(e);
        }
    }

    private TokenInfo extractTokenInfo(JSONObject jsonObject) {
        TokenInfo newInfo = new TokenInfo();
        JSONValue jsonValue;
        JSONString accessToken, refreshToken, expires;

        if ((jsonValue = jsonObject.get(ACCESS_TOKEN)) != null
                && (accessToken = jsonValue.isString()) != null
                && (jsonValue = jsonObject.get(REFRESH_TOKEN)) != null
                && (refreshToken = jsonValue.isString()) != null
                && (jsonValue = jsonObject.get(EXPIRES_IN)) != null
                && (expires = jsonValue.isString()) != null
                ) {
            newInfo.accessToken = accessToken.stringValue();
            newInfo.refreshToken = refreshToken.stringValue();
            newInfo.expires = convertExpiresInFromSeconds(expires.stringValue());
            return newInfo;
        } else {
            throw new JSONException("Could not parse authorisation credentials from response");
        }
    }

    private String buildOAuthRefreshTokenString(AuthorisationRequest request, String refreshToken) {
        return new StringBuilder(GRANT_TYPE).append(KEY_VALUE_SEPARATOR).append(REFRESH_TOKEN).append(PARAMETER_SEPARATOR)
                .append(CLIENT_ID).append(KEY_VALUE_SEPARATOR).append(request.getClientId()).append(PARAMETER_SEPARATOR)
                .append(REFRESH_TOKEN).append(KEY_VALUE_SEPARATOR).append(refreshToken).append(PARAMETER_SEPARATOR)
                .append(CLIENT_SECRET).append(KEY_VALUE_SEPARATOR).append(request.getClientSecret())
                .toString();
    }

    /**
     * Set the oauth window URL to use to authenticate. This will be provided by
     * default, and is essentially only provided to enable authentication in the
     * embedded Explorer.
     */
    public void setOAuthWindowUrl(String url) {
        this.oAuthWindowUrl = url;
    }

    /**
     * Called by the {@code doAuthLogin()} method which is registered as a global
     * variable on the page.
     */
    // This method is called via a global method defined in AuthoriserImpl.register()
    @SuppressWarnings("unused")
    void finish(String queryString, String hash) {
        TokenInfo info = new TokenInfo();
        String error = null;
        String errorDesc = "";
        String errorUri = "";

        // Get the refresh token value from the query string if it's there
        // Future versions of the Apache Amber library may include this in the URI fragment with everything else
        // but for now this is the way it is delivered
        String refreshString = REFRESH_TOKEN + KEY_VALUE_SEPARATOR;

        if (queryString.startsWith(QUERY_STRING_MARKER) && queryString.contains(refreshString)) {
            int tokenStartIndex = queryString.indexOf(refreshString) + refreshString.length();
            int tokenEndIndex = Math.min(queryString.indexOf(FRAGMENT_MARKER, tokenStartIndex),
                    queryString.indexOf(QUERY_STRING_MARKER, tokenStartIndex));
            if (tokenEndIndex < 0) {
                tokenEndIndex = queryString.length();
            }
            if (tokenStartIndex >= 0 && tokenEndIndex >= 0) {
                info.refreshToken = queryString.substring(tokenStartIndex, tokenEndIndex);
            }
        }

        // Iterate over keys and values in the string hash value to find relevant
        // information like the access token or an error message. The string will be
        // in the form of: #key1=val1&key2=val2&key3=val3 (etc.)
        int idx = 1;
        while (idx < hash.length() - 1) {
            // Grab the next key (between start and '=')
            int nextEq = hash.indexOf('=', idx);
            if (nextEq < 0) {
                break;
            }
            String key = hash.substring(idx, nextEq);

            // Grab the next value (between '=' and '&')
            int nextAmp = hash.indexOf(PARAMETER_SEPARATOR, nextEq);
            nextAmp = nextAmp < 0 ? hash.length() : nextAmp;
            String val = hash.substring(nextEq + 1, nextAmp);

            // Start looking from here from now on.
            idx = nextAmp + 1;

            // Store relevant values to be used later.
            if (key.equals(ACCESS_TOKEN)) {
                info.accessToken = val;
            } else if (key.equals(REFRESH_TOKEN)) {
                info.refreshToken = val;
            } else if (key.equals(EXPIRES_IN)) {
                // expires_in is seconds, convert to milliseconds and add to now
                info.expires = convertExpiresInFromSeconds(val);
            } else if (key.equals(ERROR)) {
                error = val;
            } else if (key.equals(ERROR_DESCRIPTION)) {
                errorDesc = " (" + val + ")";
            } else if (key.equals(ERROR_URI)) {
                errorUri = "; see: " + val;
            }
        }

        if (error != null) {
            lastCallback.onFailure(
                    new RuntimeException("Error from provider: " + error + errorDesc + errorUri));
        } else if (info.accessToken == null || info.refreshToken == null) {
            lastCallback.onFailure(new RuntimeException("Could not find access_token and/or refresh_token in hash "
                    + hash));
        } else {
            setToken(lastAuthRequest, info);
            // Reset forceAuthRequest flag
            lastAuthRequest.forceNewRequest(false);
            lastCallback.onSuccess(info.accessToken);
        }
    }

    String convertExpiresInFromSeconds(String seconds) {
        // Convert to milliseconds and add to now
        Double expiresIn = Double.valueOf(seconds) * 1000;
        return String.valueOf(clock.now() + expiresIn);
    }

    /**
     * Test-compatible abstraction for getting the current time.
     */
    static interface Clock {
        // Using double to avoid longs in GWT, which are slow.
        double now();
    }

    /**
     * Test-compatible URL encoder/decoder.
     */
    static interface UrlCodex {
        /**
         * URL-encode a string. This is abstract so that the Authoriser class can be
         * tested.
         */
        String encode(String url);

        /**
         * URL-decode a string. This is abstract so that the Authoriser class can be
         * tested.
         */
        String decode(String url);
    }

    TokenInfo getToken(AuthorisationRequest req) {
        String tokenStr = tokenStore.get(req.asString());
        return tokenStr != null ? TokenInfo.fromString(tokenStr) : null;
    }

    void setToken(AuthorisationRequest req, TokenInfo info) {
        tokenStore.set(req.asString(), info.asString());
    }

    /**
     * Clears all tokens stored by this class.
     * <p/>
     * <p>
     * This will result in subsequent calls to
     * {@link #authorise} displaying a popup to the user. If
     * the user has already granted access, that popup will immediately close.
     * </p>
     */
    public void clearAllTokens() {
        tokenStore.clear();
    }

    /**
     * Encapsulates information an access token and when it will expire.
     */
    static class TokenInfo {
        String accessToken;
        String refreshToken;
        String expires;

        String asString() {
            return accessToken + SEPARATOR + refreshToken + SEPARATOR + (expires == null ? "" : expires);
        }

        static TokenInfo fromString(String val) {
            String[] parts = val.split(SEPARATOR);
            TokenInfo info = new TokenInfo();
            info.accessToken = parts[0];
            info.refreshToken = parts[1];
            info.expires = parts.length > 2 ? parts[2] : null;
            return info;
        }
    }

    /*
    * @param req The authentication request of which to request the expiration
    *        status.
    * @return The number of milliseconds until the token expires, or negative
    *         infinity if no token was found.
    */
    public double expiresIn(AuthorisationRequest req) {
        String val = tokenStore.get(req.asString());
        return val == null ? Double.NEGATIVE_INFINITY :
                Double.valueOf(TokenInfo.fromString(val).expires) - clock.now();
    }

    /**
     * Exports a function to the page's global scope that can be called from regular JavaScript.
     *
     * Usage (in JavaScript):
     * <code>
     * oauth2win.authorise({
     * "authUrl": "..." // the auth URL to use
     * "tokenUrl": "..." // the token URL to use
     * "clientId": "..." // the client ID for this app
     * "clientSecret": "..." // the client secret for this app
     * "scopes": ["...", "..."], // (optional au) the scopes to request access to
     * "scopeDelimiter": "..." // (optional) the scope delimiter to use
     * }, function(token) { // (optional) called on success, with the token
     * }, function(error) { // (optional) called on error, with the error message
     * });
     * </code>
     */
    public static native void export() /*-{
        if (!$wnd.oauth2win) {
          $wnd.oauth2win = {};
        }
        $wnd.oauth2win.authorise = $entry(function(req, success, failure) {
          @org.jboss.pressgangccms.oauth.gwt.client.oauth.Authoriser::nativeLogin(*)(req, success, failure);
        });

        $wnd.oauth2win.expiresIn = $entry(function(req) {
          return @org.jboss.pressgangccms.oauth.gwt.client.oauth.Authoriser::nativeExpiresIn(*)(req);
        });
    }-*/;

    private static void nativeLogin(AuthRequestJso req, JsFunction success, JsFunction failure) {
        AuthoriserImpl.INSTANCE.authorise(fromJso(req), CallbackWrapper.create(success, failure));
    }

    private static double nativeExpiresIn(AuthRequestJso req) {
        return AuthoriserImpl.INSTANCE.expiresIn(fromJso(req));
    }

    private static AuthorisationRequest fromJso(AuthRequestJso jso) {
        return new AuthorisationRequest(jso.getAuthUrl(), jso.getTokenUrl(), jso.getClientId(), jso.getClientSecret())
                .withScopes(jso.getScopes())
                .withScopeDelimiter(jso.getScopeDelimiter());
    }

    private static final class AuthRequestJso extends JavaScriptObject {

        protected AuthRequestJso() {
        }

        private final native String getAuthUrl() /*-{
            return this.authUrl;
        }-*/;

        private final native String getTokenUrl() /*-{
            return this.tokenUrl;
        }-*/;

        private final native String getClientId() /*-{
            return this.clientId;
        }-*/;

        private final native String getClientSecret() /*-{
            return this.clientSecret;
        }-*/;

        private final native JsArrayString getScopesNative() /*-{
            return this.scopes || [];
        }-*/;

        private final String[] getScopes() {
            JsArrayString jsa = getScopesNative();
            String[] arr = new String[jsa.length()];
            for (int i = 0; i < jsa.length(); i++) {
                arr[i] = jsa.get(i);
            }
            return arr;
        }

        private final native String getScopeDelimiter() /*-{
            return this.scopeDelimiter || " ";
        }-*/;

    }

    private static final class JsFunction extends JavaScriptObject {
        protected JsFunction() {
        }

        private final native void execute(String input) /*-{
            this(input);
        }-*/;
    }

    private static final class CallbackWrapper implements Callback<String, Throwable> {
        private final JsFunction success;
        private final JsFunction failure;

        private CallbackWrapper(JsFunction success, JsFunction failure) {
            this.success = success;
            this.failure = failure;
        }

        private static CallbackWrapper create(JsFunction success, JsFunction failure) {
            return new CallbackWrapper(success, failure);
        }

        @Override
        public void onSuccess(String result) {
            if (success != null) {
                success.execute(result);
            }
        }

        @Override
        public void onFailure(Throwable reason) {
            if (failure != null) {
                failure.execute(reason.getMessage());
            }
        }
    }
}
