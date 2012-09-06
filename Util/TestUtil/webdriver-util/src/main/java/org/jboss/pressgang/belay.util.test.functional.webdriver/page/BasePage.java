package org.jboss.pressgang.belay.util.test.functional.webdriver.page;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

/**
 * Base page object for functional testing with WebDriver.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public abstract class BasePage {

    private final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    public WebDriver getDriver() {
        return driver;
    }

    public String getCurrentPageTitle() {
        return driver.getTitle();
    }

    public abstract String getExpectedPageTitle();

    public abstract boolean isPageLoaded();
}
