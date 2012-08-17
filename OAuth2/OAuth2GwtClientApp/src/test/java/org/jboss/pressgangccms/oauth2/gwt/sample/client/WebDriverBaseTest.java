package org.jboss.pressgangccms.oauth2.gwt.sample.client;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Base class for WebDriver tests.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(Arquillian.class)
public class WebDriverBaseTest {

    static final Logger log = Logger.getLogger(AppTest.class.getName());
    static final String WEBAPP_SRC = "src/main/webapp";
    static final String APP_NAME = "OAuth2GwtClientApp";
    static final String BASE_URL = "https://localhost:8443/" + APP_NAME;
    static Properties testProperties = new Properties();

    // testable = false causes Arquillian to run in client mode
    @Deployment(testable = false)
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, APP_NAME + ".war")
                .merge(ShrinkWrap.create(GenericArchive.class).as(ExplodedImporter.class)
                        .importDirectory("target/gwt-gen").as(GenericArchive.class))
                .addAsWebResource(new File(WEBAPP_SRC, "index.html"))
                .addAsWebInfResource(new File(WEBAPP_SRC, "WEB-INF/web.xml"))
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Drone
    WebDriver driver;

    // Hopefully can use this when url/contextRoot can be defined in config
    @ArquillianResource
    URL deploymentURL;

    @Before
    public void setUp() throws IOException {
        // Load test properties file
        URL url = ClassLoader.getSystemResource("functionaltest.properties");
        testProperties.load(new FileInputStream(new File(url.getFile())));
    }

    @Test
    public void sanity() {
        // Requires a test to prevent initialisation error
        assert(true);
    }
}


