package org.jboss.pressgangccms.oauth2.gwt.client;

import net.sf.ipsedixit.annotation.Arbitrary;
import net.sf.ipsedixit.annotation.ArbitraryString;
import org.jboss.pressgangccms.util.test.unit.gwt.BaseUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static net.sf.ipsedixit.core.StringType.ALPHA;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.pressgangccms.oauth2.gwt.client.Constants.SEPARATOR;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Provides tests for {@link org.jboss.pressgangccms.oauth2.gwt.client.AuthorisationRequest} class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AuthorisationRequestTest extends BaseUnitTest {

    private AuthorisationRequest authRequest;
    private String scopeDelimiter = ":";
    @ArbitraryString(type = ALPHA)
    private String url;
    @ArbitraryString(type = ALPHA)
    private String clientId;
    @ArbitraryString(type = ALPHA)
    private String scope;
    @ArbitraryString(type = ALPHA)
    private String anotherScope;
    @Arbitrary
    private boolean forceNew;
    @Mock
    private Authoriser.UrlCodex urlCodex;


    @Before
    public void setUp() {
        authRequest = new AuthorisationRequest(url, clientId)
                .withScopes(scope, anotherScope)
                .withScopeDelimiter(scopeDelimiter)
                .forceNewRequest(forceNew);
    }

    @Test
    public void testCreateAuthRequest() {
        // Given a set of AuthorisationRequest parameters

        // When an AuthorisationRequest is created with these parameters

        // Then these parameters are set as expected
        assertThat(authRequest.isForceNewRequest(), is(forceNew));
        assertThat(authRequest.asString(), is(clientId + SEPARATOR + scope + scopeDelimiter + anotherScope));
    }

    @Test
    public void testCreateAuthUrl() {
        // Given a set of AuthorisationRequest parameters and a URL encoder that just returns the argument
        when(urlCodex.encode(anyString())).thenAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }
        });

        // When an auth URL is created
        String result = authRequest.toAuthUrl(urlCodex);

        // Then it is formed as expected
        assertThat(result, is(url + "?client_id=" + clientId + "&response_type=token&scope=" + scope + scopeDelimiter + anotherScope));
    }

}
