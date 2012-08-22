package org.jboss.pressgangccms.util.test.functional.webdriver;

import com.google.common.base.Optional;
import org.jboss.pressgangccms.util.test.functional.webdriver.page.BasePage;
import org.openqa.selenium.*;

import java.util.logging.Logger;

import static java.lang.System.currentTimeMillis;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * WebDriver utility methods.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class WebDriverUtils {

    public static final long ONE_SECOND = 1000;
    public static final long THREE_SECONDS = ONE_SECOND * 3;
    public static final long FIVE_SECONDS = ONE_SECOND * 5;
    public static final long TEN_SECONDS = ONE_SECOND * 10;
    public static final long TWENTY_SECONDS = TEN_SECONDS * 2;
    public static final long THIRTY_SECONDS = TEN_SECONDS * 3;
    public static final long ONE_MINUTE = THIRTY_SECONDS * 2;

    private static final Logger log = Logger.getLogger(WebDriverUtils.class.getName());

    public interface WaitCondition<T> {
        public Optional<T> checkWaitCondition();

        public T handleTimeout();
    }

    private static <T> T waitForCondition(long maxWaitMillis, WaitCondition<T> waitCondition) {
        final long maxTime = currentTimeMillis() + maxWaitMillis;
        do {
            Optional<T> result = waitCondition.checkWaitCondition();
            if (result.isPresent()) {
                return result.get();
            }
        } while (currentTimeMillis() < maxTime);
        return waitCondition.handleTimeout();
    }

    public static WebElement waitUntilElementPresent(final WebDriver driver, long maxWaitMillis, final By findBy) {
        return waitForCondition(maxWaitMillis, new WaitCondition<WebElement>() {
            @Override
            public Optional<WebElement> checkWaitCondition() {
                try {
                    return Optional.of(driver.findElement(findBy));
                } catch (NoSuchElementException e) {
                    return Optional.absent();
                }
            }

            @Override
            public WebElement handleTimeout() {
                log.warning("Timed out while looking for WebElement by: " + findBy);
                throw new NoSuchElementException("Element could not be found " + findBy);
            }
        });
    }

    public static Optional<WebElement> waitToSeeIfElementPresent(WebDriver driver, long maxWaitMillis, By findBy) {
        try {
            return Optional.of(waitUntilElementPresent(driver, maxWaitMillis, findBy));
        } catch (NoSuchElementException e) {
            return Optional.absent();
        }
    }

    public static <T extends BasePage> T waitUntilPageDisplayed(final WebDriver driver, final long maxWaitMillis, final T page) {
        return waitForCondition(maxWaitMillis, new WaitCondition<T>() {
            @Override
            public Optional<T> checkWaitCondition() {
                if (page.isPageLoaded()) {
                    return Optional.of(page);
                } else {
                    return Optional.absent();
                }
            }

            @Override
            public T handleTimeout() {
                log.warning("Page failed to load:" + page.getExpectedPageTitle());
                throw new WebDriverException("Page failed to load:" + page.getExpectedPageTitle());
            }
        });
    }

    public static Optional<BasePage> waitToSeeIfPageDisplayed(WebDriver driver, long maxWaitMillis, BasePage page) {
        try {
            return Optional.of(waitUntilPageDisplayed(driver, maxWaitMillis, page));
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    public static String waitUntilPopupPresent(final WebDriver driver, long maxWaitMillis, final String popupTitle) {
        return waitForCondition(maxWaitMillis, new WaitCondition<String>() {
            @Override
            public Optional<String> checkWaitCondition() {
                return getWindowHandleByTitle(driver, popupTitle);
            }

            @Override
            public String handleTimeout() {
                log.warning("Timed out while looking for popup with title: " + popupTitle);
                throw new WebDriverException("Could not find popup with title: " + popupTitle);
            }
        });
    }

    public static Alert waitUntilAlertPresent(final WebDriver driver, long maxWaitMillis) {
        return waitForCondition(maxWaitMillis, new WaitCondition<Alert>() {
            @Override
            public Optional<Alert> checkWaitCondition() {
                try {
                    return Optional.of(driver.switchTo().alert());
                } catch (NoAlertPresentException e) {
                    return Optional.absent();
                }
            }

            @Override
            public Alert handleTimeout() {
                log.warning("Timed out while waiting for alert");
                throw new WebDriverException("Alert could not be found");
            }
        });
    }

    public static void doWait(long waitMillis) {
        final long endTime = currentTimeMillis() + waitMillis;
        do {
            try {
                Thread.sleep(ONE_SECOND);
            } catch (java.lang.InterruptedException e) {
                log.warning("Wait interrupted");
                return;
            }
        } while (currentTimeMillis() < endTime);
    }

    /**
     * Used as workaround for WebDriver bug.
     */
    public static void verifyAlertInParallelThreadAfterWait(final WebDriver driver, final String windowHandle,
                                                            final long waitMillis, final long timeout,
                                                            final String verifyText) throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                doWait(waitMillis);
                log.info("Checking for alert in parallel thread");
                driver.switchTo().window(windowHandle);
                Alert alert = waitUntilAlertPresent(driver, timeout);
                String alertText = alert.getText();
                log.info("Alert text: " + alertText);
                alert.accept();
                assertThat(alertText, containsString(verifyText));

            }
        }).start();
    }

    public static Optional<String> getWindowHandleByTitle(WebDriver driver, String title) {
        for (String handle : driver.getWindowHandles()) {
            log.fine("Found window handle: " + handle);
            if (title.equals(driver.switchTo().window(handle).getTitle())) {
                return Optional.of(handle);
            }
        }
        return Optional.absent();
    }

    public static void setCheckbox(WebElement checkbox, boolean checked) {
        if (checkbox.isSelected() != checked) {
            checkbox.click();
        }
        assertThat(checkbox.isSelected(), is(checked));
    }

}
