package org.jboss.pressgang.belay.oauth2.gwt.client;

import com.google.gwt.http.client.RequestBuilder;
import net.sf.ipsedixit.annotation.Arbitrary;
import net.sf.ipsedixit.annotation.ArbitraryString;
import org.jboss.pressgang.belay.util.test.unit.gwt.BaseGwtUnitTest;
import org.junit.Before;
import org.junit.Test;

import static net.sf.ipsedixit.core.StringType.ALPHA;
import static net.sf.ipsedixit.core.StringType.ALPHANUMERIC;
import static org.hamcrest.CoreMatchers.is;
import static org.jboss.pressgang.belay.oauth2.gwt.client.Constants.AUTHORIZATION_HEADER;
import static org.junit.Assert.assertThat;

/**
 * Provides tests for {@link org.jboss.pressgang.belay.oauth2.gwt.client.OAuthRequest} class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class OAuthRequestTest extends BaseGwtUnitTest {

    private OAuthRequest oAuthRequest;
    private RequestBuilder.Method method = RequestBuilder.GET;
    @ArbitraryString(type = ALPHA)
    private String url;
    @ArbitraryString(type = ALPHA)
    private String headerName;
    @ArbitraryString(type = ALPHA)
    private String headerValue;
    @ArbitraryString(type = ALPHANUMERIC)
    private String requestData;
    @Arbitrary
    private int timeoutMilis;
    @ArbitraryString(type = ALPHANUMERIC)
    private String token;

    @Before
    public void setUp() {
        oAuthRequest = new OAuthRequest(method, url);
    }

    @Test
    public void testRequestCreation() throws Exception {
        // Given a set of OAuthRequest parameters

        // When an OAuthRequest is created with these parameters
        oAuthRequest = oAuthRequest.setHeader(headerName, headerValue)
                .setRequestData(requestData)
                .setTimeoutMillis(timeoutMilis);

        // Then these parameters are set as expected
        assertThat(oAuthRequest.getHttpMethod(), is(method.toString()));
        assertThat(oAuthRequest.getUrl(), is(url));
        assertThat(oAuthRequest.getHeader(headerName), is(headerValue));
        assertThat(oAuthRequest.getRequestData(), is(requestData));
        assertThat(oAuthRequest.getTimeoutMillis(), is(timeoutMilis));
    }

    @Test
    public void testSetOAuthHeader() throws Exception {
        // Given an OAuthRequest and token

        // When the OAuthHeader is set
        oAuthRequest.setOAuthHeader(token);

        // The result conforms to expectations
        assertThat(oAuthRequest.getHeader(AUTHORIZATION_HEADER), is("Bearer " + token));
    }
}
