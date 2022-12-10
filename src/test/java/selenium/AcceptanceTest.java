package selenium;


import ca.jkmconsulting.crazyEightsCountdown.*;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.CardRank;
import ca.jkmconsulting.crazyEightsCountdown.Enums.GameState;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Suit;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import selenium.ComponentWrappers.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
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

    @Test
    public void A44() throws InterruptedException {
        ArrayList<Card> cardList = new ArrayList<>();
        cardList.add(Card.CLUBS_QUEEN);
        // P1 -> QC
        setupGame(cardList);
        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_QUEEN),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //This will cause alert for skipped player to clear
        assertTrue(waitElementDisplayed(AlertPopUp.byTxtTitle,1).getText().contains("Turn Skipped"));
        waitAndClickElement(AlertPopUp.byBtnAlertClose,1);
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(2).getName()));

    }

    @Test
    public void A45() throws InterruptedException {
        ArrayList<Card> cardList = new ArrayList<>();
        //Card play sequence to start from p4
        cardList.add(Card.HEARTS_5); //P1
        cardList.add(Card.HEARTS_4); //P2
        cardList.add(Card.HEARTS_3); //P3
        cardList.add(Card.CLUBS_3); //P4
        setupGame(cardList);

        //Run Turns 1 to 4
        for(int i=0;i<cardList.size();++i) {
            waitAndClickElement(PlayerHand.byCardEnum(cardList.get(i)),i);
            waitAndClickElement(PlayerHand.byBtnPlayCard,i);
        }

        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(eleCurrentPlayerName.getText().contains(players.get(0).getName()));
    }

    @Test
    public void A47() throws InterruptedException {
        ArrayList<Card> cardList = new ArrayList<>();
        // P4 -> 1H
        // P3 -> 7H
        //Card play sequence to start from p4
        cardList.add(Card.HEARTS_5); //P1
        cardList.add(Card.HEARTS_4); //P2
        cardList.add(Card.HEARTS_3); //P3
        cardList.add(Card.HEARTS_ACE); //P4
        cardList.add(Card.SPADES_3); // P1 Not to be played
        cardList.add(Card.SPADES_7); // P2 Not to be played
        cardList.add(Card.HEARTS_7); //P3
        setupGame(cardList);

        //Verify direction is normal
        assertTrue(waitElementDisplayed(ScoreBoard.byH5DirectionOfPlay,0).getText().contains(ScoreBoard.PLAY_DIRECTION.Normal.toString()));
        //Run Turns 1 to 4 so P4 is current
        for(int i=0;i<4;++i) {
            waitAndClickElement(PlayerHand.byCardEnum(cardList.get(i)),i);
            waitAndClickElement(PlayerHand.byBtnPlayCard,i);
        }

        //verify direction is reversed
        assertTrue(waitElementDisplayed(ScoreBoard.byH5DirectionOfPlay,0).getText().contains(ScoreBoard.PLAY_DIRECTION.Reversed.toString()));

        //Ensure player 3 is current player
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(eleCurrentPlayerName.getText().contains(players.get(2).getName()));

        //Player 3 plays 7 card
        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_7),2);
        waitAndClickElement(PlayerHand.byBtnPlayCard,2);

        //Verify its player 2's turn
        eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,1);
        assertTrue(eleCurrentPlayerName.getText().contains(players.get(1).getName()));

    }

    @Test
    public void A48() throws InterruptedException {
        ArrayList<Card> cardList = new ArrayList<>();
        //Card play sequence to start from p4
        cardList.add(Card.CLUBS_3); //P1
        cardList.add(Card.CLUBS_4); //P2
        cardList.add(Card.CLUBS_5); //P3
        cardList.add(Card.CLUBS_QUEEN); //P4
        setupGame(cardList);

        //Run Turns 1 to 4
        for(int i=0;i<cardList.size();++i) {
            waitAndClickElement(PlayerHand.byCardEnum(cardList.get(i)),i);
            waitAndClickElement(PlayerHand.byBtnPlayCard,i);
        }

        //This will cause alert for skipped player 1 to clear
        assertTrue(waitElementDisplayed(AlertPopUp.byTxtTitle,0).getText().contains("Turn Skipped"));
        waitAndClickElement(AlertPopUp.byBtnAlertClose,0);

        //Verify that it's player2's turn
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,1);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
    }

    @Test
    public void A51() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_KING);
        p1.add(Card.SPADES_7);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);
        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());
        deck.buildDeck(null,cardsToExcludeFromDeck,Card.CLUBS_KING);

        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_KING),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);
        assertFalse(players.get(0).getHand().contains(Card.HEARTS_KING));
        assertEquals(Card.HEARTS_KING,deck.getTopDiscardCard());
    }

    @Test
    public void A52() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_KING);
        p1.add(Card.CLUBS_7);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);
        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());
        deck.buildDeck(null,cardsToExcludeFromDeck,Card.CLUBS_KING);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_7),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);
        assertFalse(players.get(0).getHand().contains(Card.CLUBS_7));
        assertEquals(Card.CLUBS_7,deck.getTopDiscardCard());
    }

    @Test
    public void A53() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_8);
        p1.add(Card.CLUBS_7);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);
        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());
        deck.buildDeck(null,cardsToExcludeFromDeck,Card.CLUBS_KING);

        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_8),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //Handle Suit Select
        waitElementDisplayed(SuitSelect.byCardSelectDiamonds,0);
        waitAndClickElement(SuitSelect.byCardSelectDiamonds,0);

        assertFalse(players.get(0).getHand().contains(Card.HEARTS_8));
        assertEquals(Card.HEARTS_8,deck.getTopDiscardCard());
        assertEquals(Suit.DIAMONDS,deck.getActiveSuit());
    }

    @Test
    public void A54() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.SPADES_5);
        p1.add(Card.CLUBS_7);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);
        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());
        deck.buildDeck(null,cardsToExcludeFromDeck,Card.CLUBS_KING);

        waitAndClickElement(PlayerHand.byCardEnum(Card.SPADES_5),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //Handle Error Message
        WebElement we = waitElementDisplayed(AlertPopUp.byTxtTitle,0);
        assertEquals("Play Card : Invalid Card",we.getText());
        waitAndClickElement(AlertPopUp.byBtnAlertClose,0);

        assertTrue(players.get(0).getHand().contains(Card.SPADES_5));
    }

    @Test
    public void A58() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> drawOrder = new ArrayList<>();
        final Card topCard = Card.CLUBS_7;
        cardsToExcludeFromDeck.add(topCard);

        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_3);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        drawOrder.add(Card.CLUBS_6);

        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,topCard);

        assertEquals(1,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.CLUBS_6));

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_6),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        assertEquals(Card.CLUBS_6,deck.getTopDiscardCard());

    }

    @Test
    public void A59() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> drawOrder = new ArrayList<>();
        final Card topCard = Card.CLUBS_7;
        cardsToExcludeFromDeck.add(topCard);

        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_3);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        drawOrder.add(Card.DIAMONDS_6);
        drawOrder.add(Card.CLUBS_5);

        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,topCard);

        assertEquals(1,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.DIAMONDS_6));
        assertEquals(2,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.CLUBS_5));
        assertEquals(3,players.get(0).getHand().size());

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_5),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        assertEquals(Card.CLUBS_5,deck.getTopDiscardCard());
    }

    @Test
    public void A60() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> drawOrder = new ArrayList<>();
        final Card topCard = Card.CLUBS_7;
        cardsToExcludeFromDeck.add(topCard);

        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_3);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        drawOrder.add(Card.DIAMONDS_6);
        drawOrder.add(Card.SPADES_5);
        drawOrder.add(Card.HEARTS_7);

        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,topCard);

        assertEquals(1,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.DIAMONDS_6));
        assertEquals(2,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.SPADES_5));
        assertEquals(3,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        sleep(Duration.ofSeconds(2).toMillis());
        //Since it was the third draw and was valid to be played, it should have been automatically played
        assertEquals(Card.HEARTS_7,deck.getTopDiscardCard());
    }

    @Test
    public void A61() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> drawOrder = new ArrayList<>();
        final Card topCard = Card.CLUBS_7;
        cardsToExcludeFromDeck.add(topCard);

        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_3);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        drawOrder.add(Card.DIAMONDS_6);
        drawOrder.add(Card.SPADES_5);
        drawOrder.add(Card.HEARTS_4);

        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,topCard);

        assertEquals(1,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.DIAMONDS_6));
        assertEquals(2,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.SPADES_5));
        assertEquals(3,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);

        assertEquals(topCard,deck.getTopDiscardCard());
        //Turn auto ended, check current player is 2
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
        assertEquals(players.get(1),gc.getCurrentPlayer());
    }

    @Test
    public void A62() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> drawOrder = new ArrayList<>();
        final Card topCard = Card.CLUBS_7;
        cardsToExcludeFromDeck.add(topCard);

        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_3);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        drawOrder.add(Card.DIAMONDS_6);
        drawOrder.add(Card.HEARTS_8);

        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,topCard);

        assertEquals(1,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.DIAMONDS_6));
        assertEquals(2,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.HEARTS_8));

        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_8),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //Suit Select - Diamonds
        waitElementDisplayed(SuitSelect.byCardSelectDiamonds,0);
        waitAndClickElement(SuitSelect.byCardSelectDiamonds,0);
        assertEquals(Card.HEARTS_8,deck.getTopDiscardCard());
        assertEquals(Suit.DIAMONDS,deck.getActiveSuit());

        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
        assertEquals(players.get(1),gc.getCurrentPlayer());
    }

    @Test
    public void A63() throws InterruptedException {
        setupGame(null);
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        ArrayList<Card> drawOrder = new ArrayList<>();
        final Card topCard = Card.CLUBS_7;
        cardsToExcludeFromDeck.add(topCard);

        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.CLUBS_3);
        p1.add(Card.SPADES_KING);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        cardsToExcludeFromDeck.addAll(players.get(1).getHand());
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        drawOrder.add(Card.CLUBS_6);

        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,topCard);

        assertEquals(2,players.get(0).getHand().size());
        waitAndClickElement(DeckArea.byBtnDrawCard,0);
        assertTrue(players.get(0).getHand().contains(Card.CLUBS_6));


        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_6),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);
        assertFalse(players.get(0).getHand().contains(Card.CLUBS_6));
        assertEquals(Card.CLUBS_6,deck.getTopDiscardCard());

        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
        assertEquals(players.get(1),gc.getCurrentPlayer());
    }

    @Test
    public void A67() throws InterruptedException {
        setupGame(null);
        //Sequence of Play
        //P1 -> 2C
        //P2 -> Must draw because only has 4H (No clubs or 2), Draws 6C 9D, then plays 6C
        ArrayList<Card> drawOrder = new ArrayList<>();
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        drawOrder.add(Card.CLUBS_6); //P2
        drawOrder.add(Card.DIAMONDS_9); //P2

        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.CLUBS_2);
        p1.add(Card.CLUBS_4);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.HEARTS_4);
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //Other Players Keep their hands
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,null);


        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_2),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //Click on deck to draw 2 cards instead of play
        assertEquals(1,players.get(1).getHandSize());
        WebElement we = waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        sleep(Duration.ofSeconds(2).toMillis());
        assertEquals(3,players.get(1).getHandSize());

        // Play 6C
        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_6),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

    }

    @Test
    public void A68() throws InterruptedException {
        setupGame(null);
        Card topCard = null;
        ArrayList<Card> drawOrder = new ArrayList<>();
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        drawOrder.add(Card.SPADES_6); //P2
        drawOrder.add(Card.DIAMONDS_9); //P2
        drawOrder.add(Card.HEARTS_9);
        drawOrder.add(Card.CLUBS_6);

        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.CLUBS_2);
        p1.add(Card.CLUBS_4);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.HEARTS_4);
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //Other Players Keep their hands
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,null);


        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_2),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //Click on deck to draw 2 cards instead of play
        assertEquals(1,players.get(1).getHandSize());
        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        sleep(Duration.ofSeconds(2).toMillis());
        assertEquals(3,players.get(1).getHandSize());

        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        assertEquals(4,players.get(1).getHandSize());

        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        assertEquals(5,players.get(1).getHandSize());

        // Play 6C
        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_6),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        assertEquals(Card.CLUBS_6,deck.getTopDiscardCard());
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(2).getName()));
        assertEquals(players.get(2),gc.getCurrentPlayer());

    }

    @Test
    public void A69() throws InterruptedException {
        setupGame(null);
        Card topCard = null;
        ArrayList<Card> drawOrder = new ArrayList<>();
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        drawOrder.add(Card.SPADES_6); //P2
        drawOrder.add(Card.DIAMONDS_9); //P2
        drawOrder.add(Card.HEARTS_9);
        drawOrder.add(Card.SPADES_7);
        drawOrder.add(Card.HEARTS_5);

        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.CLUBS_2);
        p1.add(Card.CLUBS_4);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.HEARTS_4);
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //Other Players Keep their hands
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,null);


        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_2),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //Click on deck to draw 2 cards instead of play
        assertEquals(1,players.get(1).getHandSize());
        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        sleep(Duration.ofSeconds(2).toMillis());
        assertEquals(3,players.get(1).getHandSize());

        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        assertEquals(4,players.get(1).getHandSize());

        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        assertEquals(5,players.get(1).getHandSize());

        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        assertEquals(6,players.get(1).getHandSize());


        assertEquals(Card.CLUBS_2,deck.getTopDiscardCard());
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(2).getName()));
        assertEquals(players.get(2),gc.getCurrentPlayer());

    }

    @Test
    public void A71() throws InterruptedException {
        setupGame(null);
        Card topCard = null;
        ArrayList<Card> drawOrder = new ArrayList<>();
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();
        drawOrder.add(Card.HEARTS_2); //P2
        drawOrder.add(Card.DIAMONDS_9); //P2
        drawOrder.add(Card.SPADES_5); //P3
        drawOrder.add(Card.DIAMONDS_6); //P3
        drawOrder.add(Card.HEARTS_6); //P3
        drawOrder.add(Card.CLUBS_7); //P3


        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.CLUBS_2);
        p1.add(Card.CLUBS_4);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.HEARTS_4);
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //P3 Setup
        ArrayList<Card> p3 = new ArrayList<>();
        p3.add(Card.DIAMONDS_7);
        players.get(2).__fixHand(p3);
        cardsToExcludeFromDeck.addAll(p3);

        //Other Players Keep their hands
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(drawOrder,cardsToExcludeFromDeck,null);


        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_2),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        //Click on deck to draw 2 cards instead of play
        assertEquals(1,players.get(1).getHandSize());
        waitElementDisplayed(DeckArea.byBtnDrawCard,1);
        waitAndClickElement(DeckArea.byBtnDrawCard,1);
        sleep(Duration.ofSeconds(2).toMillis());
        assertEquals(3,players.get(1).getHandSize());

        //Plays 2H to trigger pick 4
        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_2),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);
        assertEquals(Card.HEARTS_2,deck.getTopDiscardCard());

        //Player 3 turn
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(2).getName()));
        assertEquals(players.get(2),gc.getCurrentPlayer());

        assertEquals(1,players.get(2).getHandSize());
        waitElementDisplayed(DeckArea.byBtnDrawCard,2);
        waitAndClickElement(DeckArea.byBtnDrawCard,2);
//        assertEquals(5,players.get(2).getHandSize());

        //Plays 6H
        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_6),2);
        waitAndClickElement(PlayerHand.byBtnPlayCard,2);
        assertEquals(Card.HEARTS_6,deck.getTopDiscardCard());

        eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(3).getName()));
        assertEquals(players.get(3),gc.getCurrentPlayer());
    }

    @Test
    public void A72() throws InterruptedException {
        setupGame(null);
        Card topCard = null;
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();


        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.CLUBS_2);
        p1.add(Card.CLUBS_3);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.CLUBS_4);
        p2.add(Card.CLUBS_6);
        p2.add(Card.DIAMONDS_9);
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //Other Players Keep their hands
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(null,cardsToExcludeFromDeck,null);



        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_2),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        assertEquals(Card.CLUBS_2,deck.getTopDiscardCard());
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
        assertEquals(players.get(1),gc.getCurrentPlayer());

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_4),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_6),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        assertEquals(Card.CLUBS_6,deck.getTopDiscardCard());
        eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(2).getName()));
        assertEquals(players.get(2),gc.getCurrentPlayer());
    }

    @Test
    public void A73() throws InterruptedException {
        setupGame(null);
        Card topCard = null;
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();


        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.CLUBS_2);
        p1.add(Card.CLUBS_3);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.CLUBS_4);
        p2.add(Card.CLUBS_6);
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //Other Players Keep their hands
        cardsToExcludeFromDeck.addAll(players.get(2).getHand());
        cardsToExcludeFromDeck.addAll(players.get(3).getHand());

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(null,cardsToExcludeFromDeck,null);

        assertEquals(1,gc.getRound());
        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_2),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        assertEquals(Card.CLUBS_2,deck.getTopDiscardCard());
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
        assertEquals(players.get(1),gc.getCurrentPlayer());

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_4),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_6),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);


        eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
        assertEquals(players.get(1),gc.getCurrentPlayer());

        assertEquals(2,gc.getRound());
    }

    @Test
    public void A78() throws InterruptedException {
        setupGame(null);
        Card topCard = null;
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();

        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.SPADES_ACE);
        p1.add(Card.CLUBS_3); //To play
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.CLUBS_4); //To Play
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //P3 Setup
        ArrayList<Card> p3 = new ArrayList<>();
        p3.add(Card.HEARTS_8);
        p3.add(Card.HEARTS_JACK);
        p3.add(Card.HEARTS_6);
        p3.add(Card.HEARTS_KING);
        p3.add(Card.SPADES_KING);
        players.get(2).__fixHand(p3);
        cardsToExcludeFromDeck.addAll(p3);

        //P1 Setup
        ArrayList<Card> p4 = new ArrayList<>();
        p4.add(Card.CLUBS_8);
        p4.add(Card.DIAMONDS_8);
        p4.add(Card.DIAMONDS_2);
        players.get(3).__fixHand(p4);
        cardsToExcludeFromDeck.addAll(p4);

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(null,cardsToExcludeFromDeck,null);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_3),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        assertEquals(Card.CLUBS_3,deck.getTopDiscardCard());
        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
        assertEquals(players.get(1),gc.getCurrentPlayer());

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_4),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        assertEquals("You Win The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,1).getText());
        assertEquals("You Loose The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,0).getText());
        assertEquals("You Loose The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,2).getText());
        assertEquals("You Loose The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,3).getText());
        assertEquals(GameState.ENDED,gc.getState());
        assertEquals("1", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(0)), 0).getText());
        assertEquals("0", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(1)), 1).getText());
        assertEquals("86", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(2)), 2).getText());
        assertEquals("102", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(3)), 3).getText());
    }

    @Test
    public void A80() throws InterruptedException {
        setupGame(null);
        Card topCard = Card.DIAMONDS_4;
        HashSet<Card> cardsToExcludeFromDeck = new HashSet<>();

        //P1 Setup
        ArrayList<Card> p1 = new ArrayList<>();
        p1.add(Card.HEARTS_4);
        p1.add(Card.SPADES_7);
        p1.add(Card.DIAMONDS_5);
        p1.add(Card.DIAMONDS_6);
        p1.add(Card.DIAMONDS_9);
        players.get(0).__fixHand(p1);
        cardsToExcludeFromDeck.addAll(p1);

        //P2 Setup
        ArrayList<Card> p2 = new ArrayList<>();
        p2.add(Card.SPADES_4);
        p2.add(Card.SPADES_6);
        p2.add(Card.CLUBS_KING);
        p2.add(Card.HEARTS_8);
        p2.add(Card.DIAMONDS_10);
        players.get(1).__fixHand(p2);
        cardsToExcludeFromDeck.addAll(p2);

        //P3 Setup
        ArrayList<Card> p3 = new ArrayList<>();
        p3.add(Card.SPADES_9);
        p3.add(Card.CLUBS_6);
        p3.add(Card.CLUBS_9);
        p3.add(Card.DIAMONDS_JACK);
        p3.add(Card.HEARTS_3);
        players.get(2).__fixHand(p3);
        cardsToExcludeFromDeck.addAll(p3);

        //P1 Setup
        ArrayList<Card> p4 = new ArrayList<>();
        p4.add(Card.DIAMONDS_7);
        p4.add(Card.HEARTS_JACK);
        p4.add(Card.HEARTS_QUEEN);
        p4.add(Card.HEARTS_KING);
        p4.add(Card.CLUBS_5);
        players.get(3).__fixHand(p4);
        cardsToExcludeFromDeck.addAll(p4);

        ArrayList<Card> cardOrder = new ArrayList<>();
        cardOrder.add(Card.CLUBS_2);
        cardOrder.add(Card.CLUBS_3);
        cardOrder.add(Card.CLUBS_4);
        cardOrder.add(Card.CLUBS_10);
        cardOrder.add(Card.CLUBS_JACK);
        cardOrder.add(Card.CLUBS_7);

        // Stack the deck with the cards not in players hands. Everything no specified in draw order is random
        deck.buildDeck(cardOrder,cardsToExcludeFromDeck,topCard);

        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_4),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        waitAndClickElement(PlayerHand.byCardEnum(Card.SPADES_4),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        waitAndClickElement(PlayerHand.byCardEnum(Card.SPADES_9),2);
        waitAndClickElement(PlayerHand.byBtnPlayCard,2);

        waitElementDisplayed(DeckArea.byBtnDrawCard,3);
        waitAndClickElement(DeckArea.byBtnDrawCard,3);

        waitElementDisplayed(DeckArea.byBtnDrawCard,3);
        waitAndClickElement(DeckArea.byBtnDrawCard,3);

        waitElementDisplayed(DeckArea.byBtnDrawCard,3);
        waitAndClickElement(DeckArea.byBtnDrawCard,3);

        waitAndClickElement(PlayerHand.byCardEnum(Card.SPADES_7),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        waitAndClickElement(PlayerHand.byCardEnum(Card.SPADES_6),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_6),2);
        waitAndClickElement(PlayerHand.byBtnPlayCard,2);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_2),3);
        waitAndClickElement(PlayerHand.byBtnPlayCard,3);

        waitElementDisplayed(DeckArea.byBtnDrawCard,0);
        waitAndClickElement(DeckArea.byBtnDrawCard,0);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_JACK),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_KING),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_9),2);
        waitAndClickElement(PlayerHand.byBtnPlayCard,2);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_3),3);
        waitAndClickElement(PlayerHand.byBtnPlayCard,3);

        waitElementDisplayed(DeckArea.byBtnDrawCard,0);
        waitAndClickElement(DeckArea.byBtnDrawCard,0);

        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_7),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        waitAndClickElement(PlayerHand.byCardEnum(Card.HEARTS_8),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        waitElementDisplayed(SuitSelect.byCardSelectDiamonds,1);
        waitAndClickElement(SuitSelect.byCardSelectDiamonds,1);

        for(int i=0; i<players.size();++i) {
            waitAndClickElement(AlertPopUp.byBtnAlertClose,i);
        }

        waitAndClickElement(PlayerHand.byCardEnum(Card.DIAMONDS_JACK),2);
        waitAndClickElement(PlayerHand.byBtnPlayCard,2);

        waitAndClickElement(PlayerHand.byCardEnum(Card.DIAMONDS_7),3);
        waitAndClickElement(PlayerHand.byBtnPlayCard,3);

        waitAndClickElement(PlayerHand.byCardEnum(Card.DIAMONDS_9),0);
        waitAndClickElement(PlayerHand.byBtnPlayCard,0);

        waitAndClickElement(PlayerHand.byCardEnum(Card.DIAMONDS_10),1);
        waitAndClickElement(PlayerHand.byBtnPlayCard,1);

        assertEquals("21", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(0)), 0).getText());
        assertEquals("0", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(1)), 1).getText());
        assertEquals("3", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(2)), 2).getText());
        assertEquals("39", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(3)), 3).getText());

//        assertEquals(Card.CLUBS_3,deck.getTopDiscardCard());
//        WebElement eleCurrentPlayerName = waitElementDisplayed(PlayerHand.byH5CurrentTurnPlayer,0);
//        assertTrue(  eleCurrentPlayerName.getText().contains(players.get(1).getName()));
//        assertEquals(players.get(1),gc.getCurrentPlayer());
//
//        waitAndClickElement(PlayerHand.byCardEnum(Card.CLUBS_4),1);
//        waitAndClickElement(PlayerHand.byBtnPlayCard,1);
//
//        assertEquals("You Win The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,1).getText());
//        assertEquals("You Loose The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,0).getText());
//        assertEquals("You Loose The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,2).getText());
//        assertEquals("You Loose The Game!",waitElementDisplayed(AlertPopUp.byTxtTitle,3).getText());
//        assertEquals(GameState.ENDED,gc.getState());
//        assertEquals("1", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(0)), 0).getText());
//        assertEquals("0", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(1)), 1).getText());
//        assertEquals("86", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(2)), 2).getText());
//        assertEquals("102", waitElementDisplayed(ScoreBoard.bytxtPlayerScore(players.get(3)), 3).getText());
    }
}
