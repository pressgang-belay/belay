package org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.UserConsentPage;
import org.jboss.pressgang.belay.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.AppPage.getExpectedLoginResultText;
import static org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.AppPage.getWindowHandle;
import static org.jboss.pressgang.belay.util.test.functional.webdriver.WebDriverUtil.*;

/**
 * Page object representing Fedora Account System login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class FedoraLoginPage extends BasePage {

    @FindBy(id = "user_name") private WebElement usernameInputField;
    @FindBy(id = "password") private WebElement passwordInputField;
    @FindBy(name = "login") private WebElement loginButton;

    public FedoraLoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Login to the Fedora Accounts System";
    }

    @Override
    public boolean isPageLoaded() {
        return usernameInputField.isDisplayed();
    }

    public FedoraLoginPage doLogin(String username, String password) throws Exception {
        usernameInputField.sendKeys(username);
        passwordInputField.sendKeys(password);
        loginButton.click();
        FedoraApprovalPage approvalPage = new FedoraApprovalPage(getDriver());
        // Workaround for WebDriver bug
        verifyAlertInParallelThreadAfterWait(getDriver(), getWindowHandle(), TWENTY_SECONDS, ONE_MINUTE, getExpectedLoginResultText());
        if (waitToSeeIfPageDisplayed(getDriver(), TEN_SECONDS, approvalPage).isPresent()) {
            approvalPage.approve();
        }
        UserConsentPage consentPage = new UserConsentPage(getDriver());
        if (waitToSeeIfPageDisplayed(getDriver(), TEN_SECONDS, consentPage).isPresent()) {
            consentPage.makeConsentDecision(true).submitDecision();
        }
        return this;
    }
}
