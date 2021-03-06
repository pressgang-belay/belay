package org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.UserConsentPage;
import org.jboss.pressgang.belay.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.concurrent.FutureTask;

import static org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.AppPage.getWindowHandle;
import static org.jboss.pressgang.belay.util.test.functional.webdriver.WebDriverUtil.*;

/**
 * Page object representing Red Hat login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class RedHatLoginPage extends BasePage {

    @FindBy(name = "j_username")
    private WebElement usernameInputField;
    @FindBy(name = "j_password")
    private WebElement passwordInputField;
    @FindBy(xpath = "html/body/form/input")
    private WebElement loginButton;

    public RedHatLoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Login Page";
    }

    @Override
    public boolean isPageLoaded() {
        return usernameInputField.isDisplayed();
    }

    public FutureTask<String> doLogin(String email, String password) throws Exception {
        usernameInputField.sendKeys(email);
        passwordInputField.sendKeys(password);
        loginButton.click();
        RedHatApprovalPage approvalPage = new RedHatApprovalPage(getDriver());
        // Workaround for WebDriver bug
        FutureTask<String> resultCheck = createFutureTaskToGetLoginResultFromAlert(getDriver(), getWindowHandle(), TWENTY_SECONDS, ONE_MINUTE);
        new Thread(resultCheck).start();
        if (waitToSeeIfPageDisplayed(getDriver(), TEN_SECONDS, approvalPage).isPresent()) {
            approvalPage.approve();
        }
        UserConsentPage consentPage = new UserConsentPage(getDriver());
        if (waitToSeeIfPageDisplayed(getDriver(), FIVE_SECONDS, consentPage).isPresent()) {
            consentPage.makeConsentDecision(true).submitDecision();
        }
        return resultCheck;
    }
}
