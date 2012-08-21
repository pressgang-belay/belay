package org.jboss.pressgangccms.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgangccms.oauth2.gwt.sample.client.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page object representing Red Hat OpenID approval page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class RedHatApprovalPage extends BasePage {

    @FindBy(xpath = "html/body/form/input") private WebElement continueButton;

    protected RedHatApprovalPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "/OpenIdProvider/securepage.jsp";
    }

    @Override
    public boolean isPageLoaded() {
        return continueButton.isDisplayed();
    }

    public RedHatApprovalPage approve() {
        continueButton.click();
        return this;
    }
}
