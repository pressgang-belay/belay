package org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgangccms.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtils.setCheckbox;

/**
 * Page object representing Google's OpenID approval page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class GoogleApprovalPage extends BasePage {

    @FindBy(id = "approve_button") private WebElement approveButton;
    @FindBy(id = "remember_choices_checkbox") private WebElement persistenceCheckbox;

    protected GoogleApprovalPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Google Accounts";
    }

    @Override
    public boolean isPageLoaded() {
        return approveButton.isDisplayed();
    }

    public GoogleApprovalPage setApprovalPersistence(boolean isPersistent) {
        setCheckbox(persistenceCheckbox, isPersistent);
        return this;
    }

    public GoogleApprovalPage approve() {
        approveButton.click();
        return this;
    }
}
