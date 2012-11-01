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
 * Page object representing Yahoo! login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class YahooLoginPage extends BasePage {

    @FindBy(id = "username")
    private WebElement usernameInputField;
    @FindBy(id = "passwd")
    private WebElement passwordInputField;
    @FindBy(id = "persistent")
    private WebElement persistentLoginCheckbox;
    @FindBy(id = ".save")
    private WebElement loginButton;

    public YahooLoginPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Sign in to Yahoo!";
    }

    @Override
    public boolean isPageLoaded() {
        return usernameInputField.isDisplayed();
    }

    public FutureTask<String> doLogin(String email, String password, boolean isLoginPersistent) throws Exception {
        usernameInputField.sendKeys(email);
        passwordInputField.sendKeys(password);
        setCheckbox(persistentLoginCheckbox, isLoginPersistent);
        loginButton.click();
        YahooApprovalPage approvalPage = new YahooApprovalPage(getDriver());
        // Workaround for WebDriver bug
        FutureTask<String> resultCheck = createFutureTaskToGetLoginResultFromAlert(getDriver(), getWindowHandle(), TWENTY_SECONDS, ONE_MINUTE);
        new Thread(resultCheck).start();
        if (waitToSeeIfPageDisplayed(getDriver(), TEN_SECONDS, approvalPage).isPresent()) {
            approvalPage.approve();
        }
        UserConsentPage consentPage = new UserConsentPage(getDriver());
        if (waitToSeeIfPageDisplayed(getDriver(), TEN_SECONDS, consentPage).isPresent()) {
            consentPage.makeConsentDecision(true).submitDecision();
        }
        return resultCheck;
    }
}
