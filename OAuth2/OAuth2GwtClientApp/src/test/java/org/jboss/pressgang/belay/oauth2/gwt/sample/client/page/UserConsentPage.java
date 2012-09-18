package org.jboss.pressgang.belay.oauth2.gwt.sample.client.page;

import org.jboss.pressgang.belay.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class UserConsentPage extends BasePage {

    private WebElement approve;
    private WebElement deny;
    private WebElement submitButton;

    public UserConsentPage(WebDriver driver) {
        super(driver);
    }

    @Override
    public String getExpectedPageTitle() {
        return "Authorization Required";
    }

    @Override
    public boolean isPageLoaded() {
        return submitButton.isDisplayed();
    }

    public UserConsentPage makeConsentDecision(boolean giveConsent) {
        if (giveConsent) {
            approve.click();
        } else {
            deny.click();
        }
        return this;
    }

    public UserConsentPage submitDecision() {
        submitButton.click();
        return this;
    }
}
