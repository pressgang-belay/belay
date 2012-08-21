package org.jboss.pressgangccms.oauth2.gwt.sample.client;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.join;

/**
 * Rule to take a screenshot when a test fails.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class ScreenshotTestRule implements TestRule {

    static final Logger log = Logger.getLogger(ScreenshotTestRule.class.getName());
    static WebDriver driver;
    static String screenshotDir;

    public ScreenshotTestRule(String projectBaseDir) {
        super();
        screenshotDir = projectBaseDir + "/target/surefire-reports/screenshots/";
        log.info("Screenshots will be saved to: " + screenshotDir);
    }

    public void setDriver(WebDriver driver) {
        ScreenshotTestRule.driver = driver;
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } catch (Throwable t) {
                    captureScreenshot(description.getClassName() + "." + description.getMethodName());
                    throw t;
                }
            }
        };
    }

    public void captureScreenshot(String testName) {
        try {
            File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            String screenshotPath = screenshotDir + "screenshot-" + testName + ".png";
            FileUtils.copyFile(screenshot, new File(screenshotPath));
            log.info("Screenshot saved to: " + screenshotPath);
        } catch (Throwable t) {
            log.warning("Screenshot attempt failed during test " + testName + ": " + t.getClass()
                    + " " + t.getMessage() + "\n" + join(t.getStackTrace(), '\n'));
        }
    }
}
