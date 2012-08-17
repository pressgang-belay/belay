package org.jboss.pressgangccms.oauth2.gwt.sample.client;

import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import static org.hamcrest.Matchers.containsString;
import static org.jboss.pressgangccms.oauth2.gwt.sample.client.WebDriverUtils.*;
import static org.junit.Assert.assertThat;

/**
 * Provides tests for {@link org.jboss.pressgangccms.oauth2.gwt.sample.client.App} class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AppTest extends WebDriverBaseTest {

    private static String googleUser;
    private static String googlePassword;

    @BeforeClass
    public static void initialise() {
        googleUser = (String)testProperties.get("googleUser");
        googlePassword = (String)testProperties.get("googlePassword");
        if (googleUser == null || googlePassword == null || googleUser.isEmpty() || googlePassword.isEmpty()) {
            throw new WebDriverException("Properties could not be initialised");
        }
    }

    @Before
    public void setUp() {
        clearStoredTokens();
    }

    @After
    public void tearDown() {
        driver.close();
    }

    @Test
    public void loginWithGoogle() throws Exception {
        String originalWindowHandle = driver.getWindowHandle();
        driver.findElement(By.id("googleLoginButton")).click();
        String popupHandle = waitUntilPopupPresent(driver, log, TEN_SECONDS, "Google Accounts");
        driver.switchTo().window(popupHandle);
        waitUntilElementPresent(driver, log, TEN_SECONDS, By.id("Email")).sendKeys(googleUser);
        driver.findElement(By.id("Passwd")).sendKeys(googlePassword);
        driver.findElement(By.id("signIn")).click();
        Optional<WebElement> approveButton = waitToSeeIfElementPresent(driver, log, TEN_SECONDS, By.id("approve_button"));
        if (approveButton.isPresent()) {
            approveButton.get().click();
            log.info("Clicked Allow after login");
        }
        driver.switchTo().window(originalWindowHandle);
        Alert alert = driver.switchTo().alert();
        String alertText = alert.getText();
        log.info("Alert text: " + alertText);
        alert.accept();
        assertThat(alertText, containsString("Result"));
    }

    private void clearStoredTokens() {
        log.info("Clearing stored tokens");
        driver.get(BASE_URL);
        driver.findElement(By.id("clearStoredTokensButton")).click();
        driver.switchTo().alert().accept();
    }
}
