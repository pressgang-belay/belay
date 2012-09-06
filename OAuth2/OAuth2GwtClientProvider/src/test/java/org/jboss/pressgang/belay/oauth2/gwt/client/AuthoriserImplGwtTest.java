package org.jboss.pressgang.belay.oauth2.gwt.client;

/**
 * Provides tests for {@link org.jboss.pressgang.belay.oauth2.gwt.client.AuthoriserImpl} class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AuthoriserImplGwtTest extends BaseGwtTest {

    private AuthoriserImpl.UrlCodex urlCodex;
    private String unencodedUrl = "http://www.example.com/test address";
    private String encodedUrl = "http%3A%2F%2Fwww.example.com%2Ftest+address";

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        urlCodex = new AuthoriserImpl.RealUrlCodex();
    }

    public void testUrlCodexEncode() {
        // Given a UrlCodex and an unencoded URL

        // When a URL is encoded
        String result = urlCodex.encode(unencodedUrl);

        // Then it should match encoded form
        assertEquals(encodedUrl, result);
    }

    public void testUrlCodexDecode() {
        // Given a UrlCodex and an encoded URL

        // When a URL is decoded
        String result = urlCodex.decode(encodedUrl);

        // Then it should match the decoded form
        assertEquals(unencodedUrl, result);
    }
}
