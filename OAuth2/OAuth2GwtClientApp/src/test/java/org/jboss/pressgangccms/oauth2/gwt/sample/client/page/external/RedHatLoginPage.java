package org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgangccms.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage.getExpectedLoginResultText;
import static org.jboss.pressgangccms.oauth2.gwt.sample.client.page.AppPage.getWindowHandle;
import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtil.*;

/**
 * Page object representing Red Hat login page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class RedHatLoginPage extends BasePage {

    @FindBy(name = "j_username") private WebElement usernameInputField;
    @FindBy(name = "j_password") private WebElement passwordInputField;
    @FindBy(xpath = "html/body/form/input") private WebElement loginButton;

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

    public RedHatLoginPage doLogin(String email, String password) throws Exception {
        usernameInputField.sendKeys(email);
        passwordInputField.sendKeys(password);
        loginButton.click();
        RedHatApprovalPage approvalPage = new RedHatApprovalPage(getDriver());
        // Workaround for WebDriver bug
        verifyAlertInParallelThreadAfterWait(getDriver(), getWindowHandle(), THREE_SECONDS, TWENTY_SECONDS, getExpectedLoginResultText());
        if (waitToSeeIfPageDisplayed(getDriver(), FIVE_SECONDS, approvalPage).isPresent()) {
            approvalPage.approve();
        }
        return this;
    }
}
