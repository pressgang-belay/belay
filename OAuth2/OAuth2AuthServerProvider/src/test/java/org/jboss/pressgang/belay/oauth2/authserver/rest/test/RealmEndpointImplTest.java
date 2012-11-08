package org.jboss.pressgang.belay.oauth2.authserver.rest.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.get;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(Arquillian.class)
public class RealmEndpointImplTest extends BaseArquillianEndpointImplTest {

    @Deployment
    public static WebArchive createDeployment() {
        return BaseArquillianEndpointImplTest.createDeployment();
    }

    @Test
    public void shouldReturnRealmInfo() {
        // Given a GET request to the realm endpoint
        // When the request is sent
        // Then a 200 OK status code is given
        // And realm information is returned
        expect().statusCode(200)
                .and().body(containsString("http://specs.openid.net/auth/2.0/return_to"))
                .and().body(containsString(getBaseTestUrl() + "/auth/authorize"))
                .when().get(getBaseTestUrl() + "/auth");
    }

    @Test
    public void shouldReturnExpectedRealmXml() {
        // Given a GET request to the realm endpoint
        // When the request is sent
        String xmlResult = get(getBaseTestUrl() + "/auth").andReturn().asString();

        // Then the expected realm XML is returned
        assertThat(xmlResult.equals("<xrds:XRDS xmlns:xrds=\"xri://$xrds\" xmlns=\"xri://$xrd*($v*2.0)\">" +
                                        "<XRD>" +
                                            "<Service>" +
                                                "<Type>http://specs.openid.net/auth/2.0/return_to</Type>" +
                                                "<URI>" + getBaseTestUrl() + "/auth/authorize</URI>" +
                                            "</Service>" +
                                        "</XRD>" +
                                    "</xrds:XRDS>"), is(true));
    }
}
