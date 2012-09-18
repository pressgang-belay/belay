package org.jboss.pressgang.belay.oauth2.gwt.sample.client;

import org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.AppPage;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.pressgang.belay.util.test.functional.webdriver.WebDriverUtil.*;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

/**
 * Provides functional tests for {@link org.jboss.pressgang.belay.oauth2.gwt.sample.client.App}.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AppTest extends BaseAppTest {

    private AppPage appPage;

    @Before
    public void setUp() {
        super.setUp();
        getDriver().manage().window().maximize();
        getDriver().manage().deleteAllCookies();
        getDriver().get(BASE_URL);
        this.appPage = waitUntilPageDisplayed(getDriver(), TEN_SECONDS, new AppPage(getDriver())).clearStoredTokens();
    }

    @Test
    public void loginWithGoogle() throws Exception {
        appPage.loginWithGoogle(testUsers.get("googleUser"), testUsers.get("googlePassword"), false, false);
    }

    @Test
    public void loginWithYahoo() throws Exception {
        appPage.loginWithYahoo(testUsers.get("yahooUser"), testUsers.get("yahooPassword"), false);
    }

    @Test
    public void loginWithFedora() throws Exception {
        appPage.loginWithFedora(testUsers.get("fedoraUser"), testUsers.get("fedoraPassword"));
    }

    @Test
    public void loginWithMyOpenId() throws Exception {
        appPage.loginWithMyOpenId(testUsers.get("myOpenIdUser"), testUsers.get("myOpenIdPassword"), false, false);
    }

    @Test
    public void loginWithRedHat() throws Exception {
        appPage.loginWithRedHat(testUsers.get("redHatUser"), testUsers.get("redHatPassword"));
    }

    @Test
    public void loginAndGetResource() throws Exception {
        // Given a valid user is logged in
        appPage.loginWithRedHat(testUsers.get("redHatUser"), testUsers.get("redHatPassword"));
        assertLoggedInWithRedHat();

        // When a GET request is made for all people
        String result = appPage.getAllPeople();

        // Then JSON is returned
        assertThat(result, containsString("Result:"));
        assertThat(result, containsString("{\"personId\":2,\"personName\":\"Clark Kent\",\"personUsername\":\"scoop\",\"personEmail\":\"superdude99@flymail.com\"}"));
        assertThat(result, containsString("{\"personId\":1,\"personName\":\"Jane Doe\",\"personUsername\":\"jdoe\",\"personEmail\":\"jane.doe@domail.com\"}"));
        assertThat(result, containsString("{\"personId\":0,\"personName\":\"John Smith\",\"personUsername\":\"smithy\",\"personEmail\":\"john.smith@mail.com\"}"));
    }

    @Test
    public void getResourceBeforeLoginFails() throws Exception {
        // Given no login attempts have been made

        // When a GET request is made for all people
        String result = appPage.getAllPeople();

        // Then an error is returned
        assertThat(result, containsString("You must be authorized before making requests"));
    }

    @Test
    public void associateSecondIdentity() throws Exception {
        // Given a valid user is logged in
        appPage.loginWithRedHat(testUsers.get("redHatUser"), testUsers.get("redHatPassword"));
        assertLoggedInWithRedHat();

        // When a request is made to associate another identity
        appPage.associateMyOpenIdIdentity(testUsers.get("myOpenIdUser"), testUsers.get("myOpenIdPassword"), false, false);

        // Then the identities are associated
        String result = appPage.getUserInfo();
        assertThat(result, containsString("myopenid.com"));
        assertThat(result, containsString("/OpenIdProvider/openid/provider?id="));
    }

    @Test
    public void makeIdentityPrimary() throws Exception {
        // Given a valid user is logged in and has two identities, with the logged in identity the primary
        appPage.loginWithFedora(testUsers.get("fedoraUser"), testUsers.get("fedoraPassword"));
        String loginResult = appPage.getResultFromFedoraLoginClick();
        assertThat(loginResult, containsString("Result"));
        appPage.associateGoogleIdentity(testUsers.get("googleUser"), testUsers.get("googlePassword"), false, false);
        String userInfo = appPage.getUserInfo();
        log.info("UserInfo: " + userInfo);

        // When the other identity is made primary
        String newPrimaryIdentifier = "https://www.google.com/accounts/o8/id?id=AItOawlu2J4UUb2RUlQnfbSZH2rdSZ0VQABj3I0";
        String result = appPage.makeIdentityPrimary(newPrimaryIdentifier);
        userInfo = appPage.getIdentityInfo();

        // Then the second identity becomes the primary, logged in identity and a new token is returned
        assertThat(result, containsString("Result"));
        assertThat(userInfo, containsString("\"identifier\":\"" + newPrimaryIdentifier));
        assertThat(userInfo, containsString("\"primaryIdentity\":true"));
    }

    @Test
    public void getIdentityInfo() throws Exception {
        // Given a valid user is logged in
        appPage.loginWithRedHat(testUsers.get("redHatUser"), testUsers.get("redHatPassword"));
        assertLoggedInWithRedHat();

        // When a call is made to get identity info
        String result = appPage.getIdentityInfo();

        // Then info is returned
        assertThat(result, containsString("Result: {\"identifier\""));
    }

    private String assertLoggedInWithRedHat() throws Exception {
        String result = appPage.getResultFromRedHatLoginClick();
        assertThat(result, containsString("Result"));
        return result;
    }
}
