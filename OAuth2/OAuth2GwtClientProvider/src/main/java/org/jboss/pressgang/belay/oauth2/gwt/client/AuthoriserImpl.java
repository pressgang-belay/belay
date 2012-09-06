/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This class has been modified from the original.
 */

package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.core.client.*;
import com.google.gwt.storage.client.Storage;

import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.OAUTH_POPUP_NAME;

/**
 * Includes code from the AuthImpl class in the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/),
 * written by Jason Hall. Library code has been modified.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 * <p/>
 * Provides methods to manage authentication flow.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
class AuthoriserImpl extends Authoriser {

    static final AuthoriserImpl INSTANCE = new AuthoriserImpl();

    private Window window;

    AuthoriserImpl() {
        super(getTokenStore(), new RealClock(), new RealUrlCodex(), Scheduler.get(),
                GWT.getModuleBaseURL() + OAUTH_POPUP_NAME);
        register();
    }

    /**
     * Returns the correct {@link OAuthTokenStore} implementation to use based on
     * browser support for localStorage.
     */
    private static OAuthTokenStoreImpl getTokenStore() {
        return Storage.isLocalStorageSupported() ? new OAuthTokenStoreImpl() : new OAuthCookieStoreImpl();
    }

    /**
     * Register a global function to receive auth responses from the popup window.
     */
    private native void register() /*-{
        var self = this;
        if (!$wnd.oauth2win) {
            $wnd.oauth2win = {};
        }
        $wnd.oauth2win.__doAuthLogin = $entry(function(hash) {
          self.@org.jboss.pressgang.belay.oauth2.gwt.client.Authoriser::finish(Ljava/lang/String;)(hash);
        });
    }-*/;

    /**
     * Get the OAuth 2.0 token for which this application may not have already
     * been granted access, by displaying a popup to the user.
     */
    @Override
    void doAuthLogin(String authUrl, Callback<String, Throwable> callback) {
        if (window != null && window.isOpen()) {
            callback.onFailure(new IllegalStateException("Authentication in progress"));
        } else {
            window = openWindow(authUrl);
            if (window == null) {
                callback.onFailure(new RuntimeException(
                        "The authentication popup window appears to have been blocked"));
            }
        }
    }

    @Override
    void finish(String hash) {
        // Clean up the popup
        if (window != null && window.isOpen()) {
            window.close();
        }
        super.finish(hash);
    }

    // Because GWT's Window.open() method does not return a reference to the
    // newly-opened window, we have to manage this all ourselves manually...
    private static native Window openWindow(String url) /*-{
        return $wnd.open(url, 'popupWindow', 'width=800,height=600');
    }-*/;

    static final class Window extends JavaScriptObject {
        @SuppressWarnings("unused")
        protected Window() {
        }

        native boolean isOpen() /*-{
            return !this.closed;
        }-*/;

        native void close() /*-{
            this.close();
         }-*/;
    }

    /**
     * Real GWT implementation of Clock.
     */
    static class RealClock implements Clock {
        @Override
        public double now() {
            return Duration.currentTimeMillis();
        }
    }

    /**
     * Real GWT implementation of UrlCodex.
     */
    static class RealUrlCodex implements UrlCodex {
        @Override
        public native String encode(String url) /*-{
            var regexp = /%20/g;
            return encodeURIComponent(url).replace(regexp, "+");
        }-*/;

        @Override
        public native String decode(String url) /*-{
            var regexp = /\+/g;
            return decodeURIComponent(url.replace(regexp, "%20"));
        }-*/;
    }
}
