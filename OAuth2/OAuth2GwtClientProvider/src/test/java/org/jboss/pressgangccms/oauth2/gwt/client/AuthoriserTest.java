package org.jboss.pressgangccms.oauth2.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.testing.StubScheduler;
import junit.framework.Assert;
import net.sf.ipsedixit.annotation.ArbitraryString;
import org.jboss.pressgangccms.util.test.unit.gwt.BaseUnitTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.core.client.Scheduler.ScheduledCommand;
import static junit.framework.Assert.*;
import static net.sf.ipsedixit.core.StringType.ALPHA;
import static net.sf.ipsedixit.core.StringType.ALPHANUMERIC;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.number.OrderingComparison.greaterThan;
import static org.jboss.pressgangccms.oauth2.gwt.client.Authoriser.TokenInfo;
import static org.jboss.pressgangccms.oauth2.gwt.client.Constants.SEPARATOR;
import static org.junit.Assert.assertThat;

/**
 * Includes code from the AuthTest class in the gwt-oauth2-0.2-alpha library (http://code.google.com/p/gwt-oauth2/),
 * written by Jason Hall. Library code has been modified.
 * This code is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0).
 * <p/>
 * Provides tests for {@link org.jboss.pressgangccms.oauth2.gwt.client.Authoriser} class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AuthoriserTest extends BaseUnitTest {

    private MockAuthoriser authoriser;
    private static final String POPUP_NAME = "popup.html";

    @ArbitraryString(type = ALPHA)
    private String url;
    @ArbitraryString(type = ALPHA)
    private String anotherUrl;
    @ArbitraryString(type = ALPHANUMERIC)
    private String clientId;
    @ArbitraryString(type = ALPHANUMERIC)
    private String anotherClientId;
    @ArbitraryString(type = ALPHA)
    private String scope;
    @ArbitraryString(type = ALPHANUMERIC)
    private String accessToken;

    @Before
    public void setUp() {
        authoriser = new MockAuthoriser();
    }

    @After
    public void tearDown() {
        authoriser.clearAllTokens();
    }

    /**
     * When the request does not have a token stored, the popup is used to get the token.
     */
    @Test
    public void testAuthoriseNoToken() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);
        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // The popup was used and the iframe wasn't
        assertTrue(authoriser.loggedInViaPopup);
        assertEquals(url + "?client_id=" + clientId + "&response_type=token&scope=" + scope + "&redirect_uri="
                + POPUP_NAME, authoriser.lastUrl);
    }

    /**
     * When the token is found in cookies, but may expire soon, the popup will be used to refresh the token.
     */
    @Test
    public void testAuthoriseExpiringSoon() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);

        // Storing a token that expires soon (in just under one minute)
        TokenInfo info = new TokenInfo();
        info.accessToken = accessToken;
        info.expires = String.valueOf(MockClock.now + 60 * 1000 - 1);
        authoriser.setToken(req, info);

        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        assertTrue(authoriser.expiringInOneOrExpired(info));

        assertTrue(authoriser.loggedInViaPopup);
        assertEquals(url + "?client_id=" + clientId + "&response_type=token&scope=" + scope + "&redirect_uri="
                + POPUP_NAME, authoriser.lastUrl);
    }

    /**
     * When the token is found in cookies and will not expire soon, neither popup nor iframe is used, and the
     * token is immediately passed to the callback.
     */
    @Test
    public void testAuthoriseNotExpiringSoon() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);

        // Storing a token that does not expire soon (in exactly 10 minutes)
        TokenInfo info = new TokenInfo();
        info.accessToken = accessToken;
        info.expires = String.valueOf(MockClock.now + 10 * 60 * 1000);
        authoriser.setToken(req, info);

        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // A deferred command will have been scheduled. Execute it.
        List<ScheduledCommand> deferred = ((StubScheduler) authoriser.scheduler).getScheduledCommands();
        assertEquals(1, deferred.size());
        deferred.get(0).execute();

        // The popup wasn't used.
        assertFalse(authoriser.loggedInViaPopup);

        // onSuccess() was called and onFailure() wasn't.
        assertEquals(accessToken, callback.token);
        assertNull(callback.failure);
    }

    /**
     * If the forceNewRequest flag is set, the popup should be used even if a token is already in storage.
     */
    @Test
    public void testAuthoriseForceNewRequest() {
        // Given an AuthorisationRequest with the forceNewRequest flag set to true
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope).forceNewRequest(true);
        // And a token that is not due to expire soon
        TokenInfo info = new TokenInfo();
        info.accessToken = accessToken;
        info.expires = String.valueOf(MockClock.now + 10 * 60 * 1000);
        authoriser.setToken(req, info);

        // When an authorise request is made
        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // Then the user is logged in via popup
        assertTrue(authoriser.loggedInViaPopup);
        // And the forceNewRequest flag is reset to false
        assertFalse(req.isForceNewRequest());
    }

    /**
     * When the token is found in cookies and does not specify an expire time, the
     * iframe will be used to refresh the token without displaying the popup.
     */
    @Test
    public void testAuthoriseNullExpires() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);

        // Storing a token with a null expires time
        TokenInfo info = new TokenInfo();
        info.accessToken = accessToken;
        info.expires = null;
        authoriser.setToken(req, info);

        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // TODO (Jason Hall) When Authoriser supports immediate mode for supporting
        // providers, a null expiration will trigger an iframe immediate-mode
        // refresh. Until then, the popup is always used.
        assertTrue(authoriser.loggedInViaPopup);
    }

    /**
     * When finish() is called, the callback passed to login() is executed with
     * the correct token, and a cookie is set with relevant information, expiring
     * in the correct amount of time.
     */
    @Test
    public void testFinish() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);
        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // Simulates the authoriser provider's response
        authoriser.finish("#access_token=" + accessToken + "&expires_in=10000");

        // onSuccess() was called and onFailure() wasn't
        assertEquals(accessToken, callback.token);
        assertNull(callback.failure);

        // A token was stored as a result
        InMemoryTokenStore ts = (InMemoryTokenStore) authoriser.tokenStore;
        assertEquals(1, ts.store.size());

        // That token is clientId+scope -> foo+expires
        TokenInfo info = TokenInfo.fromString(ts.store.get(clientId + SEPARATOR + scope));
        assertEquals(accessToken, info.accessToken);
        assertThat(Double.parseDouble(info.expires), greaterThan(1.0000E7));
        assertThat(Double.parseDouble(info.expires), lessThan(1.0020E7));
    }

    /**
     * If finish() is passed a bad hash from the authoriser provider, a RuntimeException
     * will be passed to the callback.
     */
    @Test
    public void testFinishBadHash() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);
        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // Simulates the authoriser provider's response
        authoriser.finish("#foobarbaznonsense");

        // onFailure() was called with a RuntimeException stating the error.
        Assert.assertNotNull(callback.failure);
        assertTrue(callback.failure instanceof RuntimeException);
        assertEquals("Could not find token in hash #foobarbaznonsense",
                ((RuntimeException) callback.failure).getMessage());

        // onSuccess() was not called.
        assertNull(callback.token);
    }

    /**
     * If finish() is passed an access token but no expires time, a TokenInfo will
     * be stored without an expiration time. The next time authoriser is requested, the
     * iframe will be used..
     */
    @Test
    public void testFinishNoExpires() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);
        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // Simulates the authoriser provider's response
        authoriser.finish("#access_token=" + accessToken);

        // onSuccess() was called and onFailure() wasn't
        assertEquals(accessToken, callback.token);
        assertNull(callback.failure);

        // A token was stored as a result
        InMemoryTokenStore ts = (InMemoryTokenStore) authoriser.tokenStore;
        assertEquals(1, ts.store.size());

        // That token is clientId+scope -> foo+expires
        TokenInfo info = TokenInfo.fromString(ts.store.get(clientId + SEPARATOR + scope));
        assertEquals(accessToken, info.accessToken);
        assertNull(info.expires);
    }

    /**
     * If finish() is passed a hash that describes an error condition, a
     * RuntimeException will be passed to onFailure() with the provider's authoriser string.
     */
    @Test
    public void testFinishError() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);
        MockCallback callback = new MockCallback();
        authoriser.authorise(req, callback);

        // Simulates the authoriser provider's error response, with the error first, last,
        // and in the middle of the hash, and as the only element in the hash. Also
        // finds error descriptions and error URIs.
        assertError(
                callback, "#error=redirect_uri_mismatch", "Error from provider: redirect_uri_mismatch");
        assertError(callback, "#error=redirect_uri_mismatch&foo=bar",
                "Error from provider: redirect_uri_mismatch");
        assertError(callback, "#foo=bar&error=redirect_uri_mismatch",
                "Error from provider: redirect_uri_mismatch");
        assertError(callback, "#foo=bar&error=redirect_uri_mismatch&bar=baz",
                "Error from provider: redirect_uri_mismatch");
        assertError(callback, "#foo=bar&error=redirect_uri_mismatch&error_description=Bad dog!",
                "Error from provider: redirect_uri_mismatch (Bad dog!)");
        assertError(callback, "#foo=bar&error=redirect_uri_mismatch&error_uri=example.com",
                "Error from provider: redirect_uri_mismatch; see: example.com");
        assertError(callback,
                "#foo=bar&error=redirect_uri_mismatch&error_description=Bad dog!&error_uri=example.com",
                "Error from provider: redirect_uri_mismatch (Bad dog!); see: example.com");

        // If the hash contains a key that ends in error, but not error=, the error
        // will be that the hash was malformed
        assertError(callback, "#wxyzerror=redirect_uri_mismatch",
                "Could not find token in hash #wxyzerror=redirect_uri_mismatch");
    }

    public void assertError(MockCallback callback, String hash, String error) {
        // Simulates the authoriser provider's error response.
        authoriser.finish(hash);

        // onFailure() was called with a RuntimeException stating the error.
        Assert.assertNotNull(callback.failure);
        assertTrue(callback.failure instanceof RuntimeException);
        assertEquals(error, ((RuntimeException) callback.failure).getMessage());

        // onSuccess() was not called.
        assertNull(callback.token);
    }

    @Test
    public void testExpiresInfo() {
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);
        authoriser.authorise(req, new MockCallback());

        // Simulates the authoriser provider's response (expires in 10s)
        authoriser.finish("#access_token=" + accessToken + "&expires_in=10");

        MockClock.now += 1000; // Fast forward 1s
        assertEquals(9000.0, authoriser.expiresIn(req));

        MockClock.now += 10000; // Fast forward another 10s
        assertEquals(-1000.0, authoriser.expiresIn(req));

        // A request that has no corresponding token expires in -1ms
        AuthorisationRequest newReq = new AuthorisationRequest(anotherUrl, anotherClientId).withScopes(scope);
        assertEquals(Double.NEGATIVE_INFINITY, authoriser.expiresIn(newReq));
    }

    /**
     * Clear tokens method should remove any existing tokens.
     */
    @Test
    public void testClearTokens() {
        // Given an Authoriser with an existing token
        AuthorisationRequest req = new AuthorisationRequest(url, clientId).withScopes(scope);
        TokenInfo info = new TokenInfo();
        info.accessToken = accessToken;
        info.expires = String.valueOf(MockClock.now + 10 * 60 * 1000);
        authoriser.setToken(req, info);

        // When clear tokens is called
        authoriser.clearAllTokens();

        // The token is removed
        assertNull(authoriser.getToken(req));
    }

    protected static class MockAuthoriser extends Authoriser {
        private boolean loggedInViaPopup;
        private String lastUrl;

        private static final OAuthTokenStore TOKEN_STORE = new InMemoryTokenStore();

        MockAuthoriser() {
            super(TOKEN_STORE, new MockClock(), new MockUrlCodex(), new StubScheduler(), POPUP_NAME);
        }

        @Override
        void doAuthLogin(String authUrl, Callback<String, Throwable> callback) {
            loggedInViaPopup = true;
            lastUrl = authUrl;
        }
    }

    static class MockClock implements Authoriser.Clock {
        static double now = 5000;

        @Override
        public double now() {
            return now;
        }
    }

    static class MockUrlCodex implements Authoriser.UrlCodex {
        @Override
        public String encode(String url) {
            return url;
        }

        @Override
        public String decode(String url) {
            return url;
        }
    }

    static class InMemoryTokenStore implements OAuthTokenStore {
        Map<String, String> store = new HashMap<String, String>();

        @Override
        public void set(String key, String value) {
            store.put(key, value);
        }

        @Override
        public String get(String key) {
            return store.get(key);
        }

        @Override
        public void clear() {
            store.clear();
        }
    }

    static class MockCallback implements Callback<String, Throwable> {
        private String token;
        private Throwable failure;

        @Override
        public void onSuccess(String token) {
            this.token = token;
        }

        @Override
        public void onFailure(Throwable caught) {
            this.failure = caught;
        }
    }
}
