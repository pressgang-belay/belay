package org.jboss.pressgang.belay.oauth2.gwt.sample.client.page;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.external.*;
import org.jboss.pressgang.belay.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.Alert;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.containsString;
import static org.jboss.pressgang.belay.util.test.functional.webdriver.WebDriverUtil.*;
import static org.junit.Assert.assertThat;

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
    private WebElement getUserInfoButton;
    private WebElement getIdentityInfoButton;
    private WebElement clearStoredTokensButton;
    private WebElement inputTextBox;

    // These are only exposed to assist with WebDriver bug workaround
    private static String windowHandle;
    private final static String EXPECTED_LOGIN_RESULT_TEXT = "Result";

    public AppPage(WebDriver driver) {
        super(driver);
        windowHandle = driver.getWindowHandle();
    }

    @Override
    public String getExpectedPageTitle() {
        return "GWT OAuth Client";
    }

    @Override
    public boolean isPageLoaded() {
        return clearStoredTokensButton.isDisplayed();
    }

    // This is only provided to assist with WebDriver bug workaround
    public static String getWindowHandle() {
        return windowHandle;
    }

    public AppPage loginWithGoogle(String email, String password, boolean isLoginPersistent,
                                   boolean isOpenIdApprovalPersistent) throws Exception {
        googleLoginButton.click();
        return doGoogleLogin(email, password, isLoginPersistent, isOpenIdApprovalPersistent);
    }

    public AppPage loginWithYahoo(String username, String password, boolean isLoginPersistent) throws Exception {
        yahooLoginButton.click();
        YahooLoginPage yahooLoginPage = new YahooLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), THIRTY_SECONDS, yahooLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TWENTY_SECONDS, yahooLoginPage);
        verifyLoginResult(yahooLoginPage.doLogin(username, password, isLoginPersistent));
        return this;
    }

    public AppPage loginWithFedora(String username, String password) throws Exception {
        inputTextBox.clear();
        inputTextBox.sendKeys(username);
        fedoraLoginButton.click();
        FedoraLoginPage fedoraLoginPage = new FedoraLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), THIRTY_SECONDS, fedoraLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TWENTY_SECONDS, fedoraLoginPage);
        verifyLoginResult(fedoraLoginPage.doLogin(username, password));
        return this;
    }

    public AppPage loginWithMyOpenId(String username, String password, boolean isLoginPersistent,
                                     boolean isOpenIdApprovalPersistent) throws Exception {
        inputTextBox.clear();
        inputTextBox.sendKeys(username);
        myOpenIdLoginButton.click();
        return doMyOpenIdLogin(password, isLoginPersistent, isOpenIdApprovalPersistent);
    }

    public AppPage loginWithRedHat(String username, String password) throws Exception {
        redHatLoginButton.click();
        RedHatApprovalPage approvalPage = new RedHatApprovalPage(getDriver());
        Optional<String> approvalPopupHandle = waitToSeeIfPopupPresent(getDriver(), TEN_SECONDS, approvalPage.getExpectedPageTitle());
        if (approvalPopupHandle.isPresent()) {
            getDriver().switchTo().window(approvalPopupHandle.get());
            FutureTask<String> resultCheck = createFutureTaskToGetLoginResultFromAlert(getDriver(), getWindowHandle(), THREE_SECONDS, TWENTY_SECONDS);
            new Thread(resultCheck).start();
            approvalPage.approve();
            verifyLoginResult(resultCheck);
            return this;
        }
        RedHatLoginPage redHatLoginPage = new RedHatLoginPage(getDriver());
        String loginPopupHandle = waitUntilPopupPresent(getDriver(), TEN_SECONDS, redHatLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(loginPopupHandle);
        waitUntilPageDisplayed(getDriver(), THIRTY_SECONDS, redHatLoginPage);
        verifyLoginResult(redHatLoginPage.doLogin(username, password));
        return this;
    }

    public AppPage clearStoredTokens() {
        clearStoredTokensButton.click();
        return this;
    }

    public AppPage associateGoogleIdentity(String email, String password, boolean isLoginPersistent,
                                           boolean isOpenIdApprovalPersistent) throws Exception {
        inputTextBox.clear();
        inputTextBox.sendKeys("gmail.com");
        associateProviderIdentityButton.click();
        Optional<Alert> alert = waitToSeeIfAlertPresent(getDriver(), TEN_SECONDS);
        if (alert.isPresent()) {
            alert.get().accept();
            return this;
        }
        return doGoogleLogin(email, password, isLoginPersistent, isOpenIdApprovalPersistent);
    }

    public AppPage associateMyOpenIdIdentity(String username, String password, boolean isLoginPersistent, boolean isOpenIdApprovalPersistent) throws Exception {
        inputTextBox.clear();
        inputTextBox.sendKeys("https://" + username + ".myopenid.com");
        associateProviderIdentityButton.click();
        Optional<Alert> alert = waitToSeeIfAlertPresent(getDriver(), TEN_SECONDS);
        if (alert.isPresent()) {
            alert.get().accept();
            return this;
        }
        return doMyOpenIdLogin(password, isLoginPersistent, isOpenIdApprovalPersistent);
    }

    public String makeIdentityPrimary(String newPrimaryIdentifier) throws Exception {
        inputTextBox.clear();
        inputTextBox.sendKeys(newPrimaryIdentifier);
        makeIdentityPrimaryButton.click();
        return getAlertText(THIRTY_SECONDS);
    }

    public String getResultFromRedHatLoginClick() throws Exception {
        redHatLoginButton.click();
        return getAlertText(THIRTY_SECONDS);
    }

    public String getResultFromFedoraLoginClick() throws Exception {
        fedoraLoginButton.click();
        return getAlertText(THIRTY_SECONDS);
    }

    public String getAllPeople() {
        getAllPeopleButton.click();
        return getAlertText(THIRTY_SECONDS);
    }

    public String getUserInfo() {
        getUserInfoButton.click();
        return getAlertText(THIRTY_SECONDS);
    }

    public String getIdentityInfo() {
        getIdentityInfoButton.click();
        return getAlertText(THIRTY_SECONDS);
    }

    private AppPage doGoogleLogin(String email, String password, boolean isLoginPersistent, boolean isOpenIdApprovalPersistent) throws Exception {
        GoogleLoginPage googleLoginPage = new GoogleLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), THIRTY_SECONDS, googleLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TWENTY_SECONDS, googleLoginPage);
        verifyLoginResult(googleLoginPage.doLogin(email, password, isLoginPersistent, isOpenIdApprovalPersistent));
        return this;
    }

    private AppPage doMyOpenIdLogin(String password, boolean isLoginPersistent, boolean isOpenIdApprovalPersistent) throws Exception {
        MyOpenIdLoginPage myOpenIdLoginPage = new MyOpenIdLoginPage(getDriver());
        String popupHandle = waitUntilPopupPresent(getDriver(), THIRTY_SECONDS, myOpenIdLoginPage.getExpectedPageTitle());
        getDriver().switchTo().window(popupHandle);
        waitUntilPageDisplayed(getDriver(), TWENTY_SECONDS, myOpenIdLoginPage);
        verifyLoginResult((myOpenIdLoginPage.doLogin(password, isLoginPersistent, isOpenIdApprovalPersistent)));
        return this;
    }

    private String getAlertText(long timeout) {
        Alert alert = waitUntilAlertPresent(getDriver(), timeout);
        alert.accept();
        return alert.getText();
    }

    private void verifyLoginResult(FutureTask<String> result) throws Exception {
        String alertText = result.get(ONE_MINUTE, TimeUnit.SECONDS);
        assertThat(alertText, containsString(EXPECTED_LOGIN_RESULT_TEXT));
    }
}
