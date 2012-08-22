package org.jboss.pressgangccms.oauth2.gwt.sample.client;

import org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage;
import org.junit.Before;
import org.junit.Test;

import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtils.TEN_SECONDS;
import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtils.waitUntilPageDisplayed;

/**
 * Provides functional tests for {@link org.jboss.pressgangccms.oauth2.gwt.sample.client.App}.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AppTest extends BaseAppTest {

    private AppPage appPage;

    @Before
    public void setUp() {
        super.setUp();
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();
        driver.get(BASE_URL);
        this.appPage = waitUntilPageDisplayed(driver, TEN_SECONDS, new AppPage(driver)).clearStoredTokens();
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


}
