package org.jboss.pressgangccms.oauth2.gwt.client.oauth;

/**
 * This a renamed copy of the TokenStore class from the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/).
 * It is recreated here as it is package private in the original library.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
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
