package org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage;
import org.jboss.pressgangccms.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage.getWindowHandle;
import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtil.*;

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

    public GoogleLoginPage doLogin(String email, String password, boolean isLoginPersistent, boolean isOpenIdApprovalPersistent) throws Exception {
        emailInputField.sendKeys(email);
        passwordInputField.sendKeys(password);
        setCheckbox(persistentLoginCheckbox, isLoginPersistent);
        loginButton.click();
        GoogleApprovalPage approvalPage = new GoogleApprovalPage(getDriver());
        // Workaround for WebDriver bug
        verifyAlertInParallelThreadAfterWait(getDriver(), getWindowHandle(), THREE_SECONDS, TWENTY_SECONDS, AppPage.getExpectedLoginResultText());
        if (waitToSeeIfPageDisplayed(getDriver(), FIVE_SECONDS, approvalPage).isPresent()) {
            approvalPage.setApprovalPersistence(isOpenIdApprovalPersistent)
                        .approve();
        }
        return this;
    }
}
