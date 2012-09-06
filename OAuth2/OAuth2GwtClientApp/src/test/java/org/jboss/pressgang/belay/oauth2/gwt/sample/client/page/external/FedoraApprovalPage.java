package org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgang.belay.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page object representing Fedora's OpenID approval page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class FedoraApprovalPage extends BasePage {

    @FindBy(id = "yes") private WebElement approveButton;

    protected FedoraApprovalPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Approve OpenID Request?";
    }

    @Override
    public boolean isPageLoaded() {
        return approveButton.isDisplayed();
    }

    public FedoraApprovalPage approve() {
        approveButton.click();
        return this;
    }
}
