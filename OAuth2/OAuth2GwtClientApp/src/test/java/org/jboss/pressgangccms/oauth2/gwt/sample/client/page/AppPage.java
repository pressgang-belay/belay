package org.jboss.pressgangccms.oauth2.gwt.sample.client.page;

import org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import static org.jboss.pressgangccms.oauth2.gwt.sample.client.WebDriverUtils.*;

/**
 * Page object representing the (only) page in the demo Application.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class AppPage extends BasePage {

    private WebElement redHatLoginButton;
    private WebElement googleLoginButton;
    private WebElement yahooLoginButton;
    private WebElement facebookLoginButton;
    private WebElement myOpenIdLoginButton;
    private WebElement fedoraLoginButton;
    private WebElement getAllPeopleButton;
    private WebElement getPersonButton;
    private WebElement associateProviderIdentityButton;
    private WebElement makeIdentityPrimaryButton;
    private WebElement getIdentityInfoButton;
    private WebElement clearStoredTokensButton;
    private WebElement inputTextBox;

    // These are only exposed to assist with WebDriver bug workaround
    private static String windowHandle;
    private static String expectedLoginResultText = "Result";

    public AppPage(WebDriver driver) {
        super(driver);
        windowHandle = driver.getWindowHandle();
    }

    @Override
    public String getExpectedPageTitle() {
        return "GwT OAuth Client";
    }

    @Override
    public boolean isPageLoaded() {
        return clearStoredTokensButton.isDisplayed();
    }

    // This is only provided to assist with WebDriver bug workaround
    public static String getWindowHandle() {
        return windowHandle;
    }

    // This is only provided to assist with WebDriver bug workaround
    public static String getExpectedLoginResultText() {
        return expectedLoginResultText;
    }

    public AppPage loginWithRedHat() {
        redHatLoginButton.click();
        return this;
    }

    public AppPage loginWithGoogle(String email, String password, boolean isLoginPersistent,
                                   boolean isOpenIdApprovalPersistent) throws Exception {
        googleLoginButton.click();
        GoogleLoginPage googleLoginPage = new GoogleLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), TWENTY_SECONDS, googleLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TEN_SECONDS, googleLoginPage);
        googleLoginPage.doLogin(email, password, isLoginPersistent, isOpenIdApprovalPersistent);
        return this;
    }

    public AppPage loginWithYahoo(String username, String password, boolean isLoginPersistent) throws Exception {
        yahooLoginButton.click();
        YahooLoginPage yahooLoginPage = new YahooLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), TWENTY_SECONDS, yahooLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TEN_SECONDS, yahooLoginPage);
        yahooLoginPage.doLogin(username, password, isLoginPersistent);
        return this;
    }

    public AppPage loginWithFedora(String username, String password) throws Exception {
        inputTextBox.sendKeys(username);
        fedoraLoginButton.click();
        FedoraLoginPage fedoraLoginPage = new FedoraLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), TWENTY_SECONDS, fedoraLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TEN_SECONDS, fedoraLoginPage);
        fedoraLoginPage.doLogin(username, password);
        return this;
    }

    public AppPage loginWithMyOpenId(String username, String password, boolean isLoginPersistent,
                                     boolean isOpenIdApprovalPersistent) throws Exception {
        inputTextBox.sendKeys(username);
        myOpenIdLoginButton.click();
        MyOpenIdLoginPage myOpenIdLoginPage = new MyOpenIdLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), TWENTY_SECONDS, myOpenIdLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TEN_SECONDS, myOpenIdLoginPage);
        myOpenIdLoginPage.doLogin(password, isLoginPersistent, isOpenIdApprovalPersistent);
        return this;
    }

    public AppPage loginWithRedHat(String username, String password) throws Exception {
        redHatLoginButton.click();
        RedHatLoginPage redHatLoginPage = new RedHatLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), TWENTY_SECONDS, redHatLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TEN_SECONDS, redHatLoginPage);
        redHatLoginPage.doLogin(username, password);
        return this;
    }

    public AppPage clearStoredTokens() {
        clearStoredTokensButton.click();
        return this;
    }
}
