package org.jboss.pressgangccms.util.test.functional.webdriver;

import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.net.URL;
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

    static final Logger log = Logger.getLogger(BaseWebDriverTest.class.getName());

    @Drone
    protected WebDriver driver;

    @ArquillianResource
    URL deploymentURL;

    @Test
    public void sanityCheck() {
        // Requires a test to prevent initialisation error
        assertThat(true, is(true));
    }

    public WebDriver getDriver() {
        return driver;
    }
}


