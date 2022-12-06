package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.AlertTypes;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.GameState;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Suit;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsJoinFailureException;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.AlertData;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.GameBoardUpdate;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.PlayerTurnInfoData;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.TurnOrderUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
public class GameController implements DeckObserver {
    public final int MAX_PLAYERS = 1;
    private GameState state;
    private final Logger LOG = LoggerFactory.getLogger(GameController.class);
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<String,Player> playerIdToPlayer = new HashMap<>();
    private int idxTurnOrder;
    private int round;
    private boolean isReverseTurnOrder;
    private boolean isPickupTwo;
    private Player currentPlayer;
    private Deck deck;

    @Autowired
    SimpMessagingTemplate message;

    public GameController() {
        this.LOG.info("GameController constructed.");
        state = GameState.OPEN;
        isReverseTurnOrder=false;
        idxTurnOrder=0;
        isPickupTwo=false;
    }

    public void setupGame() {
        deck = new Deck();
        round=0;
        initTurnOrder();

    }
    public void startGame() {
        state = GameState.RUNNING;

    }

    /**
     * Initial turn order is order players joined
     */
    private void initTurnOrder() {
        isReverseTurnOrder=false;
        currentPlayer = players.get(0);
        idxTurnOrder=0;
        for(Player p : players) {
            p.setRank(1);
            p.setScore(0);
            p.setTurnSkipped(false);
        }
    }

    private void nextTurn() {
        int increment = isReverseTurnOrder ? -1 : 1;
        while(currentPlayer.equals(players.get(idxTurnOrder)) || players.get(idxTurnOrder).isTurnSkipped()) {
            Player p = players.get(idxTurnOrder);

            //Check if turn skipped
            if(p.isTurnSkipped()) {
                sendAlert(p,new AlertData(AlertTypes.BAD,"Turn Skipped","Your turn was skipped because a queen was played.",true));
                p.setTurnSkipped(false);
            }

            //Find next player
            idxTurnOrder+=increment;
            if(idxTurnOrder<0) {
                idxTurnOrder = players.size()-1;
            }
            else if(idxTurnOrder>=players.size())
            {
                idxTurnOrder=0;
            }
        }
        currentPlayer = players.get(idxTurnOrder);
        sendPlayerTurnOrderDataUpdate();
    }

    private void nextRound() {
        ++round;
        idxTurnOrder = (round-1) % players.size();
        isReverseTurnOrder = false;
        isPickupTwo = false;
        for(Player p : players) {
            p.endOfRound();
        }
        updateRanks();

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
        playerIdToPlayer.put(player.getPlayerID(),player);

        if(players.size() >= MAX_PLAYERS) {
            this.LOG.info("Max players reached, starting game...");
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

    public void notifyInitialGameState() {

    }

    private void sendPlayerTurnOrderDataUpdate() {
        ArrayList<PlayerTurnInfoData> info = new ArrayList<>();
        int increment = isReverseTurnOrder ? -1 : 1;
        int idx = idxTurnOrder;
        for(int i=0;i<players.size();++i) {
            Player p = players.get(idx);
            info.add(new PlayerTurnInfoData(p.getPlayerID(),p.getName(),p.getRank(),p.getScore(),p.equals(currentPlayer),p.isTurnSkipped()));
            //Find next player
            idx+=increment;
            if(idx<0) {
                idx = players.size()-1;
            }
            else if(idx>=players.size())
            {
                idx=0;
            }
        }
        TurnOrderUpdate payload = new TurnOrderUpdate(
                info,
                isReverseTurnOrder,
                round
        );
        message.convertAndSend("/topic/updateTurnOrder",payload);
    }

    private void updateRanks() {
        ArrayList<Player> tmp = new ArrayList<>(players);
        for(int rank=1; rank <= players.size();++rank) {
            if(tmp.isEmpty()) {
                break;
            }
            ArrayList<Player> top = new ArrayList<>();
            top.add(tmp.get(0));
            for(int i=1;i<tmp.size();++i) {

                if(tmp.get(i).getScore()<top.get(0).getScore()) {
                    //Now top player found
                    top.clear();
                    top.add(tmp.get(i));
                }
                else if(tmp.get(i).getScore()==top.get(0).getScore()) {
                    //tie gets same rank
                    top.add(tmp.get(i));
                }
            }
            //We should have all the top ranking people here now and can assign new ranks
            for(Player p : top) {
                p.setRank(rank);
                tmp.remove(p);
            }
        }
    }

    public void sendAlert(Player player, AlertData data) {
        this.LOG.info("PlayerID '{}' sent alert of type '{}' and message '{}'",player.getPlayerID(),data.type(),data.message());
        message.convertAndSendToUser(player.getSessionID(),"/queue/alert",data);
    }
}
