package org.jboss.pressgangccms.oauth2.gwt.sample.client;

import com.google.common.base.Optional;
import org.openqa.selenium.*;

import java.util.logging.Logger;

/**
 * WebDriver utility methods.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
public class WebDriverUtils {

    public static final long TEN_SECONDS = 10000;

    public static WebElement waitUntilElementPresent(WebDriver driver, Logger log, long maxWaitMillis, By findBy) {
        final long maxTime = System.currentTimeMillis() + maxWaitMillis;
        do {
            try {
                return driver.findElement(findBy);
            } catch (NoSuchElementException e) {
                // Do nothing
            }
        } while (System.currentTimeMillis() < maxTime);
        log.warning("Timed out while looking for WebElement by: " + findBy);
        throw new NoSuchElementException("Element could not be found by: " + findBy);
    }

    public static Optional<WebElement> waitToSeeIfElementPresent(WebDriver driver, Logger log, long maxWaitMillis, By findBy) {
        try {
            return Optional.of(waitUntilElementPresent(driver, log, maxWaitMillis, findBy));
        }
        catch (NoSuchElementException e) {
            return Optional.absent();
        }
    }

    public static String waitUntilPopupPresent(WebDriver driver, Logger log, long maxWaitMillis, String popupTitle) {
        final long maxTime = System.currentTimeMillis() + maxWaitMillis;
        do {
            Optional<String> windowHandle = getWindowHandleByTitle(driver, log, popupTitle);
            if (windowHandle.isPresent()) {
                return windowHandle.get();
            }
        } while (System.currentTimeMillis() < maxTime);
        log.warning("Timed out while looking for popup with title: " + popupTitle);
        throw new WebDriverException("Could not find popup with title: " + popupTitle);
    }

    public static Optional<String> getWindowHandleByTitle(WebDriver driver, Logger log, String title) {
        for (String handle : driver.getWindowHandles()) {
            log.info("Found window handle: " + handle);
            if (title.equals(driver.switchTo().window(handle).getTitle())) {
                return Optional.of(handle);
            }
        }
        return Optional.absent();
    }
}
