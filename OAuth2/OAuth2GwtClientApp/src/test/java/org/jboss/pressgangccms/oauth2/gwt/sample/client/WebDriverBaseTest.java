package org.jboss.pressgangccms.oauth2.gwt.sample.client;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

/**
 * Base class for WebDriver tests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(Arquillian.class)
public class WebDriverBaseTest {

    private static Properties testProperties = new Properties();
    private static List<String> providers = asList("google", "yahoo", "fedora", "myOpenId", "redHat", "facebook");
    static final Logger log = Logger.getLogger(AppTest.class.getName());
    static final String WEBAPP_SRC = "src/main/webapp";
    static final String APP_NAME = "OAuth2GwtClientApp";
    static final String BASE_URL = "https://localhost:8443/" + APP_NAME;
    static Map<String, String> testUsers = newHashMap();

    // testable = false causes Arquillian to run in client mode
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                        .importDirectory("target/gwt-gen").as(GenericArchive.class))
                .addAsWebResource(new File(WEBAPP_SRC, "index.html"))
                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/web.xml"));
    }

    @Drone
    FirefoxDriver driver;

    // Hopefully can use this when url/contextRoot can be defined in config
    @ArquillianResource
    URL deploymentURL;

    @Rule
    public ScreenshotTestRule screenshotTestRule = new ScreenshotTestRule((String) testProperties.get("projectBaseDir"));

    @BeforeClass
    public static void initialise() throws IOException {
        // Load test properties file
        URL url = ClassLoader.getSystemResource("functionaltest.properties");
        testProperties.load(new FileInputStream(new File(url.getFile())));

        for (String provider : providers) {
            String user = (String) testProperties.get(provider + "User");
            String password = (String) testProperties.get(provider + "Password");
            if (user == null || password == null || user.isEmpty() || password.isEmpty()) {
                throw new WebDriverException("Properties could not be initialised: " + provider + " property missing");
            }
            testUsers.put(provider + "User", user);
            testUsers.put(provider + "Password", password);
        }
    }

    @Before
    public void setUp() {
        // Set WebDriver
        screenshotTestRule.setDriver(driver);
    }

    @Test
    public void sanity() {
        // Requires a test to prevent initialisation error
        assert (true);
    }

    public WebDriver getDriver() {
        return driver;
    }
}


