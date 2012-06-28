package com.redhat.gwtsecurity.client.oauth;

import com.google.gwt.storage.client.Storage;

/**
 * This code is from the gwt-oauth2-0.2-alpha libary found at http://code.google.com/p/gwt-oauth2/.
 * It is recreated here as it is package private in the original library.
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
