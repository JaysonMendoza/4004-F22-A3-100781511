package cucumber;


import ca.jkmconsulting.crazyEightsCountdown.CrazyEightsCountdownDedicatedServer;
import ca.jkmconsulting.crazyEightsCountdown.GameController;
import ca.jkmconsulting.crazyEightsCountdown.PlayerManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import selenium.AbstractSeleniumTest;

import java.time.Duration;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = CrazyEightsCountdownDedicatedServer.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AcceptanceTest extends AbstractSeleniumTest {

    @Autowired
    PlayerManager pm;

    @Autowired
    GameController gc;

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }


    @Test
    public void basicRegistrationTest() throws InterruptedException {
        this.createUserInstance();
        sleep(Duration.ofSeconds(5).toMillis());
        WebElement txtName = this.waitElementDisplayed("formPlayerName",0);
        WebElement btnRegister = this.waitElementDisplayed("btnRegister",0);
        this.waitAndSendKeys("Player 1",txtName,0);
        this.waitAndClickElement(btnRegister,0);
        sleep(Duration.ofSeconds(1).toMillis());
        WebElement txtPopTitle = this.waitElementDisplayed("popTitle",0);
        assertEquals("Waiting for Game Start",txtPopTitle.getText());
        this.disconnectUsers();
    }

}
