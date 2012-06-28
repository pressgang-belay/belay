package com.redhat.gwtsecurity.client.oauth;

/**
 * This code is from the gwt-oauth2-0.2-alpha libary found at http://code.google.com/p/gwt-oauth2/.
 * It is recreated here as it is package private in the original library.
 *
 * Interface for storing, retrieving, and clearing stored tokens.
 *
 * @author jasonhall@google.com (Jason Hall)
 */
interface OAuthTokenStore {
    public void set(String key, String value);

    public String get(String key);

    public void clear();
}
