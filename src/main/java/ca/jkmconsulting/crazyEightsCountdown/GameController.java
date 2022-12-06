package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.GameState;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Suit;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsJoinFailureException;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.GameBoardUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;

@Controller
public class GameController implements DeckObserver {
    public final int MAX_PLAYERS = 1;
    private GameState state;
    private final Logger LOG = LoggerFactory.getLogger(GameController.class);
    private final ArrayList<Player> players = new ArrayList<>();
    private Deck deck;

    @Autowired
    SimpMessagingTemplate message;

    public GameController() {
        this.LOG.info("GameController constructed.");
        state = GameState.OPEN;
    }

    public void setupGame() {
        deck = new Deck();

    }
    public void startGame() {
        state = GameState.RUNNING;
    }

    public void EndGame() {
        state = GameState.ENDED;
    }

    @Override
    public void handleDeckUpdated(GameBoardUpdate updateData) {
        this.LOG.info("Dispatch: Deck update sent to /topic/updateGameBoard with '{}' draw cards and '{}' in discard pile",updateData.numCardsDrawPile(),updateData.discardPile().size());
        message.convertAndSend("/topic/updateGameBoard",updateData);
    }

    public void joinGame(Player player) throws CrazyEightsJoinFailureException {
        if(state != GameState.OPEN) {
            String msg = String.format("Player %s with playerID %s request to join REJECTED. REASON: Incorrect Game state %s but must be RUNNING",player.getName(),player.getPlayerID(),state);
            this.LOG.warn(msg);
            throw new CrazyEightsJoinFailureException(msg);
        }
        else if(players.contains(player)) {
            this.LOG.warn("Player '{}' had a duplicate join request. Accepted without action");
            return;
        }
        players.add(player);

        if(players.size() >= MAX_PLAYERS) {
            state = GameState.RUNNING;
            setupGame();
        }
    }

    public void actionPlayerPlayCard(Player player, Card card) {

    }

    public void actionDrawCard(Player player) {

    }

    public void actionSelectSuit(Player player, Suit suit) {

    }
}
