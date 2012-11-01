package org.jboss.pressgang.belay.oauth2.gwt.sample.client.page.external;

import org.jboss.pressgang.belay.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * Page object representing Yahoo!'s OpenID approval page.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class YahooApprovalPage extends BasePage {

    @FindBy(id = "agree")
    private WebElement approveButton;

    protected YahooApprovalPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Sign in to Yahoo!";
    }

    @Override
    public boolean isPageLoaded() {
        return approveButton.isDisplayed();
    }

    public YahooApprovalPage approve() {
        approveButton.click();
        return this;
    }
}
