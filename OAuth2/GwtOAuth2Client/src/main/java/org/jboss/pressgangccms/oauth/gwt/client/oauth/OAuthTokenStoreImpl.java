package org.jboss.pressgangccms.oauth.gwt.client.oauth;

import com.google.gwt.storage.client.Storage;

/**
 * This a renamed copy of the TokenStoreImpl class from the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/).
 * It is recreated here as it is package private in the original library.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 *
 * Default implementation of token storage, using localStorage to store tokens
 * (if supported).
 *
 * @author jasonhall@google.com (Jason Hall)
 */

class OAuthTokenStoreImpl implements OAuthTokenStore {
    private static final Storage STORAGE = Storage.getLocalStorageIfSupported();

    // Make sure that the Storage we have isn't null.
    static {
        if (STORAGE == null) {
            throw new NullPointerException("Storage is null");
        }
    }

    public void set(String key, String value) {
        STORAGE.setItem(key, value);
    }

    public String get(String key) {
        return STORAGE.getItem(key);
    }

    public void clear() {
        STORAGE.clear();
    }
}
