package org.jboss.pressgangccms.oauth2.gwt.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import net.sf.ipsedixit.annotation.Arbitrary;
import net.sf.ipsedixit.annotation.ArbitraryString;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static net.sf.ipsedixit.core.StringType.ALPHA;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.pressgangccms.oauth2.gwt.client.Authoriser.TokenInfo;
import static org.jboss.pressgangccms.oauth2.gwt.client.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Provides tests for {@link org.jboss.pressgangccms.oauth2.gwt.client.OAuthHandler} class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthHandlerTest extends BaseUnitTest {

    @Mock
    private Authoriser authoriser;
    @Mock
    private OAuthRequest oAuthRequest;
    @Mock
    private AuthorisationRequest authRequest;
    @Mock
    private TokenInfo tokenInfo;
    @Mock
    private Callback callback;
    @Mock
    private RequestCallback requestCallback;
    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private Exception exception;
    @ArbitraryString(type = ALPHA)
    private String url;
    @ArbitraryString(type = ALPHA)
    private String result;
    @ArbitraryString(type = ALPHA)
    private String token;
    @Arbitrary
    private int expiry;
    @Arbitrary
    private int newExpiry;
    @Arbitrary
    private int convertedExpiry;

    private OAuthHandler handler;

    @Before
    public void setUp() {
        handler = new OAuthHandler(authoriser);
    }

    @Test
    public void testLogin() throws Exception {
        // Given an OAuthHandler and an AuthorisationRequest

        // When login is called
        handler.login(authRequest, callback);

        // Then the authoriser is called to authorise the request
        verify(authoriser).authorise(authRequest, callback);
        // And the authorisation request is recorded
        assertThat(handler.lastAuthRequest.equals(authRequest), is(true));
    }

    @Test
    public void testSendRequestBeforeLogin() throws Exception {
        // Given a call to login has not been made
        handler.lastAuthRequest = null;

        // When an attempt to login is made
        // Then the callback's error branch is taken
        handler.sendRequest(oAuthRequest, new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                assert (false);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                assertThat(exception.getMessage(), is("You must log in before making requests"));
            }
        });
    }

    @Test
    public void testSendRequestAfterLogin() throws Exception {
        // Given the user has logged in previously
        handler.lastAuthRequest = authRequest;

        // When an attempt to send an OAuth request is made
        handler.sendRequest(oAuthRequest, requestCallback);

        // Then an attempt to authorise the request will be made
        verify(authoriser).authorise(any(AuthorisationRequest.class), any(Callback.class));
    }

    @Test
    public void testSendRequestWhenTokenInvalid() throws Exception {
        // Given a user has logged in but a valid token cannot be retrieved
        handler.lastAuthRequest = authRequest;
        CallbackMockStubber.callFailureWith(exception).when(authoriser)
                .authorise(any(AuthorisationRequest.class), any(Callback.class));

        // When an attempt to send an OAuth request is made
        // Then the supplied callback's error branch is taken
        handler.sendRequest(oAuthRequest, new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
                assert (false);
            }

            @Override
            public void onError(Request request, Throwable exception) {
                assertThat(exception.getMessage(), is("Could not obtain request authorisation"));
            }
        });
    }

    @Test
    public void testSendRequestWhenTokenValid() throws Exception {
        // Given a user has logged in and their token has been successfully retrieved
        handler.lastAuthRequest = authRequest;
        CallbackMockStubber.callSuccessWith(token).when(authoriser)
                .authorise(any(AuthorisationRequest.class), any(Callback.class));

        // When an attempt to send an OAuth request is made
        handler.sendRequest(oAuthRequest, requestCallback);

        // Then the request is sent
        verify(oAuthRequest).sendRequest(token, handler, authRequest, requestCallback);
    }

    @Test
    public void testRequestWithResponse() throws Exception {
        // Given a user has sent a successful request
        handler.lastAuthRequest = authRequest;
        CallbackMockStubber.callSuccessWith(token).when(authoriser)
                .authorise(any(AuthorisationRequest.class), any(Callback.class));
        CallbackMockStubber.callOnResponseReceivedWith(request, response).when(oAuthRequest)
                .sendRequest(token, handler, authRequest, requestCallback);

        // When a response is received
        handler.sendRequest(oAuthRequest, requestCallback);

        // Then it is passed back to the caller
        verify(requestCallback).onResponseReceived(request, response);
    }

    @Test
    public void testRequestWithErrorResponse() throws Exception {
        // Given a user has sent a request successfully but the response is an error
        handler.lastAuthRequest = authRequest;
        CallbackMockStubber.callSuccessWith(token).when(authoriser)
                .authorise(any(AuthorisationRequest.class), any(Callback.class));
        CallbackMockStubber.callOnErrorWith(request, exception).when(oAuthRequest)
                .sendRequest(token, handler, authRequest, requestCallback);

        // When the error response is received
        handler.sendRequest(oAuthRequest, requestCallback);

        // Then it is passed back to the caller
        verify(requestCallback).onError(request, exception);
    }

    @Test
    public void testProcessNewExpiry() throws Exception {
        // Given a user has sent a successful request and the response contains new expiry info
        TokenInfo info = new TokenInfo();
        info.accessToken = token;
        info.expires = Integer.toString(expiry);

        given(authoriser.getToken(authRequest)).willReturn(info);
        given(authoriser.convertExpiresInFromSeconds(Integer.toString(newExpiry))).willReturn(Integer.toString(convertedExpiry));
        given(response.getHeader(AUTHORISATION_HEADER)).willReturn(OAUTH_HEADER_NAME + " " + EXPIRES_IN + "='" + newExpiry + "'");

        // When processOAuthRequestResponse is called
        handler.processOAuthRequestResponse(request, response, authRequest, requestCallback);

        // Then the token info is updated and the response is passed on
        verify(requestCallback).onResponseReceived(request, response);
        ArgumentCaptor<AuthorisationRequest> authRequestArgument = ArgumentCaptor.forClass(AuthorisationRequest.class);
        ArgumentCaptor<TokenInfo> tokenInfoArgument = ArgumentCaptor.forClass(TokenInfo.class);
        verify(authoriser).setToken(authRequestArgument.capture(), tokenInfoArgument.capture());
        assertEquals(authRequest, authRequestArgument.getValue());
        assertEquals(Integer.toString(convertedExpiry), tokenInfoArgument.getValue().expires);
    }

    @Test
    public void testClearAllTokens() throws Exception {
        // Given an OAuthHandler

        // When clearAllTokens is called
        handler.clearAllTokens();

        // The authoriser is called to clear all tokens
        verify(authoriser).clearAllTokens();
    }

    @Test
    public void testEncodeUrl() throws Exception {
        // Given an OAuthHandler with an authoriser instance
        when(authoriser.encodeUrl(url)).thenReturn(result);

        // When encodeUrl is called
        String encoded = handler.encodeUrl(url);

        // The authoriser is called to encode the URL and the result is returned
        verify(authoriser).encodeUrl(url);
        assertThat(encoded.equals(result), is(true));
    }
}
