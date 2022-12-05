package ca.jkmconsulting.crazyEightsCountdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameController implements DeckObserver {
    private final Logger LOG = LoggerFactory.getLogger(GameController.class);
    private Deck deck;
    @Autowired
    SimpMessagingTemplate message;

    public GameController() {

    }

    public void setupGame() {
        deck = new Deck();

    }
    public void startGame() {

    }

    @Override
    public void handleDeckUpdated(GameBoardUpdate updateData) {
        this.LOG.info("Dispatch: Deck update sent to /topic/updateGameBoard with '{}' draw cards and '{}' in discard pile",updateData.numCardsDrawPile(),updateData.discardPile().size());
        message.convertAndSend("/topic/updateGameBoard",updateData);
    }
}
