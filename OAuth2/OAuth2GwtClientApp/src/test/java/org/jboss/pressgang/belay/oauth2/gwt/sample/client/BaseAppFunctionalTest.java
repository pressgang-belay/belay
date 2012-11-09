package org.jboss.pressgang.belay.oauth2.gwt.sample.client;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.pressgang.belay.util.test.functional.webdriver.BaseFunctionalTest;
import org.jboss.pressgang.belay.util.test.functional.webdriver.ScreenshotTestRule;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

/**
 * Base test for {@link org.jboss.pressgang.belay.oauth2.gwt.sample.client.App}.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunAsClient
public class BaseAppFunctionalTest extends BaseFunctionalTest {

    private static Properties testProperties = new Properties();
    private static List<String> providers = asList("google", "yahoo", "fedora", "myOpenId", "redHat", "facebook");
    static final String WEBAPP_SRC = "src/main/webapp";
    static final String APP_NAME = "OAuth2GwtClientApp";
    static final String BASE_URL = "https://localhost:8443/" + APP_NAME;
    static Map<String, String> testUsers = newHashMap();

    @Deployment(name = "gwtapp")
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                        .importDirectory("target/gwt-gen").as(GenericArchive.class))
                .addAsWebResource(new File(WEBAPP_SRC, "index.html"))
                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/web.xml"));
    }

    @Deployment
    public static ZipImporter createAuthServerDeployment() {
        return ShrinkWrap.create(ZipImporter.class, "OAuth2AuthServer.war").importFrom(new File("target/OAuth2AuthServer.war"));
    }

    @Deployment
    public static ZipImporter createSecuredRestAppDeployment() {
        return ShrinkWrap.create(ZipImporter.class, "OAuth2SecuredRestApp.war").importFrom(new File("target/OAuth2SecuredRestApp.war"));
    }

    @Deployment
    public static ZipImporter createOpenIdProviderDeployment() {
        return ShrinkWrap.create(ZipImporter.class, "OpenIdProvider.war").importFrom(new File("target/OpenIdProvider.war"));
    }

    @Rule
    public ScreenshotTestRule screenshotTestRule = new ScreenshotTestRule(testProperties.get("projectBaseDir") + "/target/surefire-reports", true);

    @BeforeClass
    public static void initialize() throws IOException {
        // Load test properties file
        URL url = ClassLoader.getSystemResource("functionaltest.properties");
        testProperties.load(new FileInputStream(new File(url.getFile())));

        for (String provider : providers) {
            String user = (String) testProperties.get(provider + "User");
            String password = (String) testProperties.get(provider + "Password");
            if (user == null || password == null || user.isEmpty() || password.isEmpty()) {
                throw new WebDriverException("Properties could not be initialized: " + provider + " property missing");
            }
            testUsers.put(provider + "User", user);
            testUsers.put(provider + "Password", password);
        }
    }

    public void setUp() {
        // Set WebDriver
        if (getDriver() != null) {
            getDriver().quit();
        }
        setDriver(new FirefoxDriver());
        getDriver().manage().timeouts().pageLoadTimeout(5, TimeUnit.MINUTES);
        screenshotTestRule.setDriver(getDriver());
    }

    private static String getProperty(String propertyName) throws IOException {
        URL url = ClassLoader.getSystemResource("functionaltest.properties");
        testProperties.load(new FileInputStream(new File(url.getFile())));
        return (String) testProperties.get(propertyName);
    }

}
