package org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage;
import org.jboss.pressgangccms.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage.getWindowHandle;
import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtil.*;

/**
 * Page object representing Yahoo! login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class YahooLoginPage extends BasePage {

    @FindBy(id = "username") private WebElement usernameInputField;
    @FindBy(id = "passwd") private WebElement passwordInputField;
    @FindBy(id = "persistent") private WebElement persistentLoginCheckbox;
    @FindBy(id = ".save") private WebElement loginButton;

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

    public YahooLoginPage doLogin(String email, String password, boolean isLoginPersistent) throws Exception {
        usernameInputField.sendKeys(email);
        passwordInputField.sendKeys(password);
        setCheckbox(persistentLoginCheckbox, isLoginPersistent);
        loginButton.click();
        YahooApprovalPage approvalPage = new YahooApprovalPage(getDriver());
        // Workaround for WebDriver bug
        verifyAlertInParallelThreadAfterWait(getDriver(), getWindowHandle(), THREE_SECONDS, TWENTY_SECONDS, AppPage.getExpectedLoginResultText());
        if (waitToSeeIfPageDisplayed(getDriver(), FIVE_SECONDS, approvalPage).isPresent()) {
            approvalPage.approve();
        }
        return this;
    }
}
