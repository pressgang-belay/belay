package org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgangccms.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import static org.jboss.pressgangccms.util.test.functional.webdriver.WebDriverUtils.setCheckbox;

/**
 * Page object representing MyOpenID's OpenID approval page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class MyOpenIdApprovalPage extends BasePage {

    @FindBy(id = "continue-button") private WebElement continueButton;
    @FindBy(id = "skip_step") private WebElement persistenceCheckbox;

    protected MyOpenIdApprovalPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "OpenID sign-in for";
    }

    @Override
    public boolean isPageLoaded() {
        return continueButton.isDisplayed();
    }

    public MyOpenIdApprovalPage setApprovalPersistence(boolean isPersistent) {
        setCheckbox(persistenceCheckbox, isPersistent);
        return this;
    }

    public MyOpenIdApprovalPage approve() {
        continueButton.click();
        return this;
    }
}
