package org.jboss.pressgang.belay.oauth2.authserver.rest.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.jayway.restassured.RestAssured.given;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(Arquillian.class)
public class GrantEndpointIntegrationTest extends BaseArquillianIntegrationTest {

    @Deployment
    public static WebArchive createDeployment() {
        return BaseArquillianIntegrationTest.createDeployment();
    }

    @Test
    public void shouldRequireValidClientId() {
        // Given an otherwise valid request to the grant endpoint with an invalid client ID
        // When the request is sent
        // Then the 400 bad request status code is given
        given().header("Authorization", "Bearer access_token")
               .expect().statusCode(400)
               .when().get(getBaseTestUrl() + "/auth/invalidate?client_id=invalidclientid");
    }

    @Test
    public void shouldProvideClientIdMatchingTokenGrantClient() {
        // Given an otherwise valid request with an access token supplied that is not associated with the given client
        // When the request is sent
        // Then the 400 bad request status code is given
        given().header("Authorization", "Bearer access_token")
                .expect().statusCode(400)
                .when().get(getBaseTestUrl() + "/auth/invalidate?client_id=confidentialclientid");
    }

    @Test
    public void shouldInvalidateTokenGrant() {
        // Given a valid request to the grant endpoint
        // When the request is sent
        // Then the request should be successful
        given().header("Authorization", "Bearer pc_access_token")
               .expect().statusCode(200)
               .when().get(getBaseTestUrl() + "/auth/invalidate?client_id=publicclientid");

        // And the associated token should not longer give resource access
        given().header("Authorization", "Bearer pc_access_token")
               .expect().statusCode(401)
               .when().get(getBaseTestUrl() + "/auth/invalidate?client_id=publicclientid");
    }
}
