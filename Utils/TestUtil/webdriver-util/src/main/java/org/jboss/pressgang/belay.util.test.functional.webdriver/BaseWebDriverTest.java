package org.jboss.pressgang.belay.util.test.functional.webdriver;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Base class for WebDriver tests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(Arquillian.class)
public abstract class BaseWebDriverTest {

    protected static final Logger log = Logger.getLogger(BaseWebDriverTest.class.getName());

    @Drone
    WebDriver driver;

    @Test
    public void sanityCheck() {
        // Requires a test to prevent initialisation error
        assertThat(true, is(true));
    }

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriver setDriver(WebDriver driver) {
        this.driver = driver;
        return this.driver;
    }
}


