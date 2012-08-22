package org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgangccms.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage.getExpectedLoginResultText;
import static org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage.getWindowHandle;
import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtils.*;

/**
 * Page object representing MyOpenID login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class MyOpenIdLoginPage extends BasePage {

    @FindBy(id = "password") private WebElement passwordInputField;
    @FindBy(id = "stay_signed_in") private WebElement persistentLoginCheckbox;
    @FindBy(id = "signin_button") private WebElement loginButton;

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

    public MyOpenIdLoginPage doLogin(String password, boolean isLoginPersistent, boolean isOpenIdApprovalPersistent) throws Exception {
        passwordInputField.sendKeys(password);
        setCheckbox(persistentLoginCheckbox, isLoginPersistent);
        loginButton.click();
        MyOpenIdApprovalPage approvalPage = new MyOpenIdApprovalPage(getDriver());
        // Workaround for WebDriver bug
        verifyAlertInParallelThreadAfterWait(getDriver(), getWindowHandle(), THREE_SECONDS, TWENTY_SECONDS, getExpectedLoginResultText());
        if (waitToSeeIfPageDisplayed(getDriver(), FIVE_SECONDS, approvalPage).isPresent()) {
            approvalPage.setApprovalPersistence(isOpenIdApprovalPersistent)
                        .approve();
        }
        return this;
    }
}
