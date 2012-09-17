package org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.AppPage;
import org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.UserConsentPage;
import org.jboss.pressgang.belay.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.concurrent.FutureTask;

import static org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.AppPage.getWindowHandle;
import static org.jboss.pressgang.belay.util.test.functional.webdriver.WebDriverUtil.*;

/**
 * Page object representing Google login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GoogleLoginPage extends BasePage {

    @FindBy(id = "Email") private WebElement emailInputField;
    @FindBy(id = "Passwd") private WebElement passwordInputField;
    @FindBy(id = "PersistentCookie") private WebElement persistentLoginCheckbox;
    @FindBy(id = "signIn") private WebElement loginButton;

    public GoogleLoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Google Accounts";
    }

    @Override
    public boolean isPageLoaded() {
        return emailInputField.isDisplayed();
    }

    public FutureTask<String> doLogin(String email, String password, boolean isLoginPersistent, boolean isOpenIdApprovalPersistent) throws Exception {
        emailInputField.sendKeys(email);
        passwordInputField.sendKeys(password);
        setCheckbox(persistentLoginCheckbox, isLoginPersistent);
        loginButton.click();
        GoogleApprovalPage approvalPage = new GoogleApprovalPage(getDriver());
        // Workaround for WebDriver bug
        FutureTask<String> resultCheck = createFutureTaskToGetLoginResultFromAlert(getDriver(), getWindowHandle(), TWENTY_SECONDS, ONE_MINUTE);
        new Thread(resultCheck).start();
        if (waitToSeeIfPageDisplayed(getDriver(), TEN_SECONDS, approvalPage).isPresent()) {
            approvalPage.setApprovalPersistence(isOpenIdApprovalPersistent)
                        .approve();
        }
        UserConsentPage consentPage = new UserConsentPage(getDriver());
        if (waitToSeeIfPageDisplayed(getDriver(), FIVE_SECONDS, consentPage).isPresent()) {
            consentPage.makeConsentDecision(true).submitDecision();
        }
        return resultCheck;
    }
}
