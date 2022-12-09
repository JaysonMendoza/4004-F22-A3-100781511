package selenium;

import ca.jkmconsulting.crazyEightsCountdown.CrazyEightsCountdownDedicatedServer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.util.ArrayList;

import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;


@SpringBootTest
public abstract class AbstractSeleniumTest {
    protected final Logger LOG = LoggerFactory.getLogger(AbstractSeleniumTest.class);
    protected ArrayList<WebDriver> userInstances = new ArrayList<>();
    public final String NODE_REACTJS_TEST_SERVER_URL = "http://localhost:3000";



    public WebDriver createUserInstance(String url) {
        final WebDriver wd = new ChromeDriver();
        userInstances.add(wd);
        wd.get(url);
        return wd;
    }

    public WebDriver createUserInstance() {
        return createUserInstance(NODE_REACTJS_TEST_SERVER_URL);
    }

    public void disconnectUsers() {
        for(WebDriver wd : userInstances) {
            wd.quit();
        }
    }

    public WebElement waitElementDisplayed(final String id, final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, Duration.ofSeconds(60)).until(visibilityOf(wd.findElement(By.id(id))));
    }


    public WebElement waitElementDisplayed(final WebElement element,final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, Duration.ofSeconds(60)).until(visibilityOf(element));
    }

    public boolean waitEleementHidden(final String id, final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, Duration.ofSeconds(60)).until(not(visibilityOf(wd.findElement(By.id(id)))));
    }

    public boolean waitEleementHidden(final WebElement element, final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, Duration.ofSeconds(60)).until(not(visibilityOf(element)));
    }

    public void waitAndClickElement(final WebElement element,final int userIndex) {
        new WebDriverWait(this.userInstances.get(userIndex),Duration.ofSeconds(60)).until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    public void waitAndSendKeys(String keysToSend,final WebElement element,final int userIndex) {
        new WebDriverWait(this.userInstances.get(userIndex),Duration.ofSeconds(60)).until(ExpectedConditions.elementToBeClickable(element));
        element.sendKeys(keysToSend);
    }
}
