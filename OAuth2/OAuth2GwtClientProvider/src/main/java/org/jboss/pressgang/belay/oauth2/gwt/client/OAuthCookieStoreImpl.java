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
 */

package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * This a renamed copy of the CookieStoreImpl class from the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/).
 * It is recreated here as it is package private in the original library.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 * <p/>
 * Backup implementation of TokenStoreImpl storing tokens in cookies, for
 * browsers where localStorage is not supported.
 *
 * @author jasonhall@google.com (Jason Hall)
 */
public class OAuthCookieStoreImpl extends OAuthTokenStoreImpl {

    private static final String COOKIE_PREFIX = "gwt-oauth2-";

    //TODO test this - changed name to key
    @SuppressWarnings("deprecation")
    @Override
    public native void set(String key, String value) /*-{
    $doc.cookie = @org.jboss.pressgang.belay.oauth2.gwt.client.OAuthCookieStoreImpl::COOKIE_PREFIX +
        encodeURIComponent(key) + '=' + encodeURIComponent(value);
  }-*/;

    @Override
    public native String get(String key) /*-{
    var m = @org.jboss.pressgang.belay.oauth2.gwt.client.OAuthCookieStoreImpl::ensureCookies();
    return m[@org.jboss.pressgang.belay.oauth2.gwt.client.OAuthCookieStoreImpl::COOKIE_PREFIX + key];
  }-*/;

    @Override
    public native void clear() /*-{
    $doc.cookie = null;
    @org.jboss.pressgang.belay.oauth2.gwt.client.OAuthCookieStoreImpl::cachedCookies = null;
  }-*/;

    private static JavaScriptObject cachedCookies = null;

    // Used only in JSNI.
    private static String rawCookies;

    private static native void loadCookies() /*-{
    @org.jboss.pressgang.belay.oauth2.gwt.client.OAuthCookieStoreImpl::cachedCookies = {};
    var docCookie = $doc.cookie;
    if (docCookie && docCookie != '') {
      var crumbs = docCookie.split('; ');
      for ( var i = 0; i < crumbs.length; ++i) {
        var name, value;
        var eqIdx = crumbs[i].indexOf('=');
        if (eqIdx == -1) {
          name = crumbs[i];
          value = '';
        } else {
          name = crumbs[i].substring(0, eqIdx);
          value = crumbs[i].substring(eqIdx + 1);
        }
        try {
          name = decodeURIComponent(name);
        } catch (e) {
          // ignore error, keep undecoded name
        }
        try {
          value = decodeURIComponent(value);
        } catch (e) {
          // ignore error, keep undecoded value
        }
        @org.jboss.pressgang.belay.oauth2.gwt.client.OAuthCookieStoreImpl::cachedCookies[name] = value;
      }
    }
  }-*/;

    private static JavaScriptObject ensureCookies() {
        if (cachedCookies == null || needsRefresh()) {
            loadCookies();
        }
        return cachedCookies;
    }

    private static native boolean needsRefresh() /*-{
    var docCookie = $doc.cookie;
    // Check to see if cached cookies need to be invalidated.
     if (docCookie != @com.google.gwt.user.client.Cookies::rawCookies) {
      @com.google.gwt.user.client.Cookies::rawCookies = docCookie;
      return true;
    } else {
      return false;
    }
  }-*/;
}
