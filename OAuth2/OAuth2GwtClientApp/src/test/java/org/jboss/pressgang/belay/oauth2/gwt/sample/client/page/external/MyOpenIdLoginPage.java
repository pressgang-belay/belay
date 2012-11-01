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
 * Page object representing MyOpenID login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class MyOpenIdLoginPage extends BasePage {

    @FindBy(id = "password")
    private WebElement passwordInputField;
    @FindBy(id = "stay_signed_in")
    private WebElement persistentLoginCheckbox;
    @FindBy(id = "signin_button")
    private WebElement loginButton;

    public MyOpenIdLoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Sign In";
    }

    @Override
    public boolean isPageLoaded() {
        return passwordInputField.isDisplayed();
    }

    public FutureTask<String> doLogin(String password, boolean isLoginPersistent, boolean isOpenIdApprovalPersistent) throws Exception {
        passwordInputField.sendKeys(password);
        setCheckbox(persistentLoginCheckbox, isLoginPersistent);
        loginButton.click();
        MyOpenIdApprovalPage approvalPage = new MyOpenIdApprovalPage(getDriver());
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
