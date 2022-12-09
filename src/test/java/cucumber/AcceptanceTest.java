package cucumber;


import ca.jkmconsulting.crazyEightsCountdown.*;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.GameState;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.ThreadUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import selenium.AbstractSeleniumTest;
import selenium.ComponentWrappers.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CrazyEightsCountdownDedicatedServer.class,webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AcceptanceTest extends AbstractSeleniumTest {

    @Autowired
    private PlayerManager pm;

    @Autowired
    private GameController gc;

    private Deck deck;

    private Map<String, Player> playerIdToPlayer;

    private ArrayList<Player> players;

    public AcceptanceTest() {
        super(LoggerFactory.getLogger(AcceptanceTest.class));
    }

    @BeforeAll
    static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @AfterEach
    private void tearDown() {
        this.disconnectUsers();
    }

    private void setupGame(ArrayList<Card> cardsOnTop) throws InterruptedException {
        gc.activateTestMode(cardsOnTop);
        registerPlayers();
        deck = gc.getDeck();
    }

    /**
     * This depends on the order of registeration == order of browser creation
     * @throws InterruptedException
     */
    private void registerPlayers() throws InterruptedException {
        for(int i=0;i<gc.MAX_PLAYERS;++i) {
            this.createUserInstance();
        }
//        ThreadUtils.sleep(Duration.ofSeconds(5));
        for(int i=0;i<gc.MAX_PLAYERS;++i) {
            this.waitAndSendKeys(String.format("Player %d",i+1), Registration.byFormPlayerName,i);
            this.waitAndClickElement(Registration.byBtnRegister,i);
            if(i<gc.MAX_PLAYERS-1) {
                WebElement we = this.waitElementDisplayed(PopUpMessage.byTitle,i);
                assertEquals("Waiting for Game Start",we.getText());
            }
        }
//        ThreadUtils.sleep(Duration.ofSeconds(5));
        players = gc.getPlayers();
        playerIdToPlayer=pm.getPlayerIdToPlayer();
        assertEquals(gc.MAX_PLAYERS,players.size());
        assertEquals(gc.MAX_PLAYERS,playerIdToPlayer.size());
        assertEquals(GameState.RUNNING,gc.getState());

        //clear pop up on player who starts
        waitAndClickElement(AlertPopUp.byBtnAlertClose,0);
    }


    @Test
    public void basicRegistrationTest() throws InterruptedException {
        this.createUserInstance();
        sleep(Duration.ofSeconds(5).toMillis());
        this.waitAndSendKeys("Player 1",Registration.byFormPlayerName,0);
        this.waitAndClickElement(Registration.byBtnRegister,0);
        sleep(Duration.ofSeconds(1).toMillis());
        WebElement txtPopTitle = this.waitElementDisplayed(PopUpMessage.byTitle,0);
        assertEquals("Waiting for Game Start",txtPopTitle.getText());
        this.disconnectUsers();
    }

    @Test
    public void A41() throws InterruptedException {
        ArrayList<Card> cardList = new ArrayList<>();
        cardList.add(Card.CLUBS_3);
        setupGame(cardList);

        WebElement cardTray = waitElementDisplayed(PlayerHand.byGrpHandCards,0);
        Point handLoc = waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_3),0).getLocation();
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);
        Point newLoc = waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_3),0).getLocation();
        assertNotEquals(handLoc,newLoc);
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
    }

    @Test
    public void A43() throws InterruptedException {
        ArrayList<Card> cardList = new ArrayList<>();
        // P1 -> 1H
        // P4 -> 7H
        cardList.add(Card.HEARTS_ACE); //P1
        cardList.add(Card.CLUBS_3);
        cardList.add(Card.CLUBS_4);
        cardList.add(Card.HEARTS_7); //P4
        setupGame(cardList);
        assertTrue(waitElementDisplayed(ScoreBoard.byH5DirectionOfPlay,0).getText().contains(ScoreBoard.PLAY_DIRECTION.Normal.toString()));
        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_ACE),0).getLocation();
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);
        assertTrue(waitElementDisplayed(ScoreBoard.byH5DirectionOfPlay,0).getText().contains(ScoreBoard.PLAY_DIRECTION.Reversed.toString()));
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(3).getName()));
    }
}
