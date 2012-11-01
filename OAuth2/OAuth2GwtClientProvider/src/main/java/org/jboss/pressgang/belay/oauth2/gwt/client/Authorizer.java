package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;

/**
 * Includes code from the Auth class in the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/),
 * written by Jason Hall. Library code has been modified.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 * <p/>
 * Provides methods to manage authentication flow.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public abstract class Authorizer {

    AuthorizationRequest lastAuthRequest;
    Callback<String, Throwable> lastCallback;

    /**
     * Instance of the {@link Authorizer} to use in a GWT application.
     */
    public static Authorizer get() {
        return AuthorizerImpl.INSTANCE;
    }

    private final UrlCodex urlCodex;
    private final Clock clock;
    final OAuthTokenStore tokenStore;
    final Scheduler scheduler;
    String oAuthWindowUrl;

    Authorizer(OAuthTokenStore tokenStore, Clock clock, UrlCodex urlCodex, Scheduler scheduler,
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
     * <p/>
     * If it can be determined that the user has already granted access, and the
     * token has not yet expired, and that the token will not expire soon, the
     * existing token will be passed to the callback -- provided the request flag
     * is not set to force a call to the OAuth provider.
     * <p/>
     * Otherwise, a popup window will be displayed which may prompt the user to
     * login and/or grant access. If the user has already granted access the popup
     * will close and the token will be passed to the callback. If access
     * hasn't been granted the user will be prompted, and when they grant access,
     * the token will be passed to the callback.
     *
     * @param request  Request for authentication.
     * @param callback Callback to pass the token to when access has been granted.
     */
    public void authorize(AuthorizationRequest request, final Callback<String, Throwable> callback) {
        lastAuthRequest = request;
        lastCallback = callback;

        // Try to look up the token we have stored.
        final TokenInfo info = getToken(request);
        if (request.isForceNewRequest() || info == null || info.expires == null || expiringInOneOrExpired(info)) {
            // Reset forceAuthRequest flag
            lastAuthRequest.forceNewRequest(false);
            // A new request has been forced, token wasn't found or doesn't have an expiration, or is expired or
            // about to expire. Request a new access token.
            String authUrl = new StringBuilder(request.toAuthUrl(urlCodex))
                    .append(Constants.PARAMETER_SEPARATOR)
                    .append(Constants.REDIRECT_URI)
                    .append(Constants.KEY_VALUE_SEPARATOR)
                    .append(urlCodex.encode(oAuthWindowUrl))
                    .toString();
            doAuthLogin(authUrl, callback);
        } else {
            // Token was found and is good, immediately execute the callback with the access token.
            scheduler.scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    callback.onSuccess(info.accessToken);
                }
            });
        }
    }

    /**
     * Returns whether or not the token will be expiring within the next minute
     * or has already expired.
     */
    boolean expiringInOneOrExpired(TokenInfo info) {
        return Double.valueOf(info.expires) < (clock.now() + Constants.ONE_MINUTE);
    }

    /**
     * Get the OAuth 2.0 token for which this application may not have already
     * been granted access, by displaying a popup to the user.
     */
    abstract void doAuthLogin(String authUrl, Callback<String, Throwable> callback);


    public String encodeUrl(String url) {
        return urlCodex.encode(url);
    }

    /**
     * Set the oauth2 window URL to use to authenticate. This will be provided by
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
    // This method is called via a global method defined in AuthorizerImpl.register()
    @SuppressWarnings("unused")
    void finish(String hash) {
        TokenInfo info = new TokenInfo();
        String error = null;
        String errorDesc = "";
        String errorUri = "";

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
            int nextAmp = hash.indexOf(Constants.PARAMETER_SEPARATOR, nextEq);
            nextAmp = nextAmp < 0 ? hash.length() : nextAmp;
            String val = hash.substring(nextEq + 1, nextAmp);

            // Start looking from here from now on.
            idx = nextAmp + 1;

            // Store relevant values to be used later.
            if (key.equals(Constants.ACCESS_TOKEN)) {
                info.accessToken = val;
            } else if (key.equals(Constants.EXPIRES_IN)) {
                // expires_in is seconds, convert to milliseconds and add to now
                info.expires = convertExpiresInFromSeconds(val);
            } else if (key.equals(Constants.ERROR)) {
                error = val;
            } else if (key.equals(Constants.ERROR_DESCRIPTION)) {
                errorDesc = " (" + val + ")";
            } else if (key.equals(Constants.ERROR_URI)) {
                errorUri = "; see: " + val;
            }
        }

        if (error != null) {
            lastCallback.onFailure(
                    new RuntimeException("Error from provider: " + error + errorDesc + errorUri));
        } else if (info.accessToken == null) {
            lastCallback.onFailure(new RuntimeException("Could not find token in hash "
                    + hash));
        } else {
            setToken(lastAuthRequest, info);
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
         * URL-encode a string. This is abstract so that the Authorizer class can be
         * tested.
         */
        String encode(String url);

        /**
         * URL-decode a string. This is abstract so that the Authorizer class can be
         * tested.
         */
        String decode(String url);
    }

    TokenInfo getToken(AuthorizationRequest req) {
        String tokenStr = tokenStore.get(req.asString());
        return tokenStr != null ? TokenInfo.fromString(tokenStr) : null;
    }

    void setToken(AuthorizationRequest req, TokenInfo info) {
        tokenStore.set(req.asString(), info.asString());
    }

    /**
     * Clears all tokens stored by this class.
     * <p/>
     * <p>
     * This will result in subsequent calls to
     * {@link #authorize} displaying a popup to the user. If
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
        String expires;

        String asString() {
            return accessToken + Constants.SEPARATOR + (expires == null ? "" : expires);
        }

        static TokenInfo fromString(String val) {
            String[] parts = val.split(Constants.SEPARATOR);
            TokenInfo info = new TokenInfo();
            info.accessToken = parts[0];
            info.expires = parts.length > 1 ? parts[1] : null;
            return info;
        }
    }

    /*
    * @param req The authentication request of which to request the expiration
    *        status.
    * @return The number of milliseconds until the token expires, or negative
    *         infinity if no token was found.
    */
    public double expiresIn(AuthorizationRequest req) {
        String val = tokenStore.get(req.asString());
        return val == null ? Double.NEGATIVE_INFINITY :
                Double.valueOf(TokenInfo.fromString(val).expires) - clock.now();
    }

    /**
     * Exports a function to the page's global scope that can be called from regular JavaScript.
     * <p/>
     * Usage (in JavaScript):
     * <code>
     * oauth2win.authorize({
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
        $wnd.oauth2win.authorize = $entry(function(req, success, failure) {
          @org.jboss.pressgang.belay.oauth2.gwt.client.Authorizer::nativeLogin(*)(req, success, failure);
        });

        $wnd.oauth2win.expiresIn = $entry(function(req) {
          return @org.jboss.pressgang.belay.oauth2.gwt.client.Authorizer::nativeExpiresIn(*)(req);
        });
    }-*/;

    private static void nativeLogin(AuthRequestJso req, JsFunction success, JsFunction failure) {
        AuthorizerImpl.INSTANCE.authorize(fromJso(req), CallbackWrapper.create(success, failure));
    }

    private static double nativeExpiresIn(AuthRequestJso req) {
        return AuthorizerImpl.INSTANCE.expiresIn(fromJso(req));
    }

    private static AuthorizationRequest fromJso(AuthRequestJso jso) {
        return new AuthorizationRequest(jso.getAuthUrl(), jso.getClientId())
                .withScopes(jso.getScopes())
                .withScopeDelimiter(jso.getScopeDelimiter());
    }

    private static final class AuthRequestJso extends JavaScriptObject {

        protected AuthRequestJso() {
        }

        private final native String getAuthUrl() /*-{
            return this.authUrl;
        }-*/;

        private final native String getClientId() /*-{
            return this.clientId;
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
