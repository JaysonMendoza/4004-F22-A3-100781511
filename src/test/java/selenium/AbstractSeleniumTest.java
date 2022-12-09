package selenium;

import ca.jkmconsulting.crazyEightsCountdown.CrazyEightsCountdownDedicatedServer;
import ca.jkmconsulting.crazyEightsCountdown.Player;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import selenium.ComponentWrappers.PlayerHand;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.not;
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;


//@SpringBootTest(classes = CrazyEightsCountdownDedicatedServer.class)
public abstract class AbstractSeleniumTest {
    protected final Logger LOG;
    protected ArrayList<WebDriver> userInstances = new ArrayList<>();
    public final String NODE_REACTJS_TEST_SERVER_URL = "http://localhost:3000";
    static final public int MAX_WAIT_SECONDS = 60;
    static final public Duration MAX_WAIT = Duration.ofSeconds(MAX_WAIT_SECONDS);

    public ArrayList<WebDriver> getUserInstances() {
        return userInstances;
    }

    public WebDriver getUserInstance(int idx) {
        return userInstances.get(idx);
    }

    public AbstractSeleniumTest(Logger log) {
        this.LOG = log;
    }

    public AbstractSeleniumTest() {
        this(LoggerFactory.getLogger(AbstractSeleniumTest.class));
    }


    public WebDriver createUserInstance(String url) {
        final WebDriver wd = new ChromeDriver();
        wd.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
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
        return new WebDriverWait(wd, MAX_WAIT).until(visibilityOf(wd.findElement(By.id(id))));
    }

    public WebElement waitElementDisplayed(By targetElement, final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, MAX_WAIT).until(visibilityOf(wd.findElement(targetElement)));
    }

    public WebElement waitElementDisplayed(final WebElement element,final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, MAX_WAIT).until(visibilityOf(element));
    }

    public boolean waitEleementHidden(final String id, final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, MAX_WAIT).until(not(visibilityOf(wd.findElement(By.id(id)))));
    }

    public boolean waitEleementHidden(final WebElement element, final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, MAX_WAIT).until(not(visibilityOf(element)));
    }

    public boolean waitEleementHidden(final By targetElement, final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        return new WebDriverWait(wd, MAX_WAIT).until(not(visibilityOf(wd.findElement(targetElement))));
    }

    public void waitAndClickElement(final WebElement element,final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        new WebDriverWait(wd,MAX_WAIT).until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    public void waitAndSendKeys(String keysToSend,final WebElement element,final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
        new WebDriverWait(wd,MAX_WAIT).until(ExpectedConditions.elementToBeClickable(element));
        element.sendKeys(keysToSend);
    }

    public WebElement waitAndClickElement(final By targetElement,final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
//        WebElement element = waitElementDisplayed(targetElement,userIndex);
        WebElement element = new WebDriverWait(wd,MAX_WAIT).until((ExpectedConditions.elementToBeClickable(wd.findElement(targetElement))));
        element.click();
        return element;
    }

    public WebElement waitAndSendKeys(String keysToSend,By targetElement,final int userIndex) {
        WebDriver wd = this.userInstances.get(userIndex);
//        WebElement element = waitElementDisplayed(targetElement,userIndex);
        WebElement element = new WebDriverWait(wd,MAX_WAIT).until((ExpectedConditions.elementToBeClickable(wd.findElement(targetElement))));
        element.sendKeys(keysToSend);
        return element;
    }
}
