package org.jboss.pressgang.belay.oauth2.authserver.rest.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.restassured.RestAssured.expect;
import static com.jayway.restassured.RestAssured.given;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(Arquillian.class)
public class GrantEndpointImplTest extends BaseArquillianEndpointImplTest {

    @Deployment
    public static WebArchive createDeployment() {
        return BaseArquillianEndpointImplTest.createDeployment();
    }

    @Test
    public void shouldRequireClientAuthenticationForConfidentialGrantEndpoint() {
        // Given a request to the confidential client grant endpoint without authentication
        // When the request is sent
        // Then a 401 unauthenticated status code is given
        expect().statusCode(401)
                .when().get(getBaseTestUrl() + "/auth/confidential/invalidate");
    }

    @Test
    public void shouldReturnUnauthorizedStatusIfConfidentialClientAttemptsToUsePublicGrantEndpoint() {
        // Given an otherwise valid request from a confidential client to a public grant endpoint without authentication
        // When the request is sent
        // Then a 401 unauthenticated status code is given
        given().header("Authorization", "Bearer cc_access_token")
                .expect().statusCode(401)
                .when().get(getBaseTestUrl() + "/auth/confidential/invalidate?client_id=confidential_client_id");
    }

    @Test
    public void shouldInvalidateTokenGrant() {
        // Given a valid request to the public grant endpoint
        // When the request is sent

        // Then the request should be successful
        given().header("Authorization", "Bearer access_token")
                .header("client_id", "public_client_id")
                .expect().statusCode(200)
                .when().get(getBaseTestUrl() + "/auth/invalidate?client_id=public_client_id");

        // And the associated token should be invalidated
        given().header("Authorization", "Bearer access_token")
                .expect().statusCode(401)
                .when().get(getBaseTestUrl() + "/auth/invalidate?client_id=public_client_id");
    }
}
