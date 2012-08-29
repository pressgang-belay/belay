package org.jboss.pressgangccms.util.test.functional.webdriver;

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
    private WebDriver driver;
    private String screenshotDir;
    private boolean quitWebDriverAfterTest;

    public ScreenshotTestRule(String screenshotDir, boolean quitWebDriverAfterEachTest) {
        super();
        this.screenshotDir = screenshotDir;
        log.info("Screenshots will be saved to: " + screenshotDir);
        this.quitWebDriverAfterTest = quitWebDriverAfterEachTest;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public Statement apply(final Statement statement, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                    if (quitWebDriverAfterTest) driver.quit();
                } catch (Throwable t) {
                    captureScreenshot(description.getClassName(), description.getClassName() + "." + description.getMethodName());
                    if (quitWebDriverAfterTest) driver.quit();
                    throw t;
                }
            }
        };
    }

    public void captureScreenshot(String className, String testName) {
        try {
            File screenshot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
            String screenshotPath = screenshotDir + "/" + className + "/screenshot-" + testName + ".png";
            FileUtils.copyFile(screenshot, new File(screenshotPath));
            log.info("Screenshot saved to: " + screenshotPath);
        } catch (Throwable t) {
            log.warning("Screenshot attempt failed during test " + testName + ": " + t.getClass()
                    + " " + t.getMessage() + "\n" + join(t.getStackTrace(), '\n'));
        }
    }
}
