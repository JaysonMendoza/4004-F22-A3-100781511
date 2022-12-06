package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.AlertTypes;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.GameState;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Suit;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.CrazyEightsJoinFailureException;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

@Controller
public class GameController implements DeckObserver {
    public final int MAX_PLAYERS = 1;
    public final int END_CONDITION_SCORE_THRESHOLD =100;
    public final int STARTING_HAND_SIZE = 5;
    private GameState state;
    private final Logger LOG = LoggerFactory.getLogger(GameController.class);
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<String,Player> playerIdToPlayer = new HashMap<>();
    private int idxTurnOrder;
    private int round;
    private boolean isReverseTurnOrder;
    private boolean isPickupTwo;
    private int cardsToPickup;
    private Player currentPlayer;
    private Deck deck;

    @Autowired
    SimpMessagingTemplate message;

    public GameController() {
        this.LOG.info("GameController constructed.");
        state = GameState.OPEN;
        isReverseTurnOrder=false;
        idxTurnOrder=0;
        cardsToPickup=0;
        isPickupTwo=false;
    }

    public void setupGame() {
        deck = new Deck();
        round=0;
        cardsToPickup=0;
        initTurnOrder();

    }
    public void startGame() {
        state = GameState.RUNNING;

        dealStartingHand();
        //send start game signal
        message.convertAndSend("/topic/startGame",(Object)null);
        sendGlobalMessage(null,String.format("The game begins with player %s!",currentPlayer.getName()));
        startTurn(true);
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
        startTurn(false);
    }

    private void startTurn(boolean isNewRound) {
        if(isNewRound) {
            sendAlert(currentPlayer,new AlertData(AlertTypes.NEUTRAL,"Your Turn : New Round","You get to off a new round. Play any card!",true));
        }
        if(isPickupTwo) {
            message.convertAndSendToUser(currentPlayer.getSessionID(),"/queue/alert",new AlertData(AlertTypes.BAD,"Your Turn : Pickup 2",String.format("You must either play 2 cards immediately, or draw 2 cards. You may play any EIGHT, or a card of the suit %s",deck.getTopDiscardedCard().suit),true));
            sendGlobalMessage(null,String.format("Player %s begins their turn and must pickup 2 cards!",currentPlayer.getName()));
            cardsToPickup=2;
        }
        else {
            message.convertAndSendToUser(currentPlayer.getSessionID(),"/queue/alert",new AlertData(AlertTypes.NEUTRAL,"Your Turn!",String.format("It's your turn. Play a card. You may play any EIGHT, or a card of the suit %s",deck.getTopDiscardedCard().suit),true));
            sendGlobalMessage(null,String.format("Player %s begins their turn!",currentPlayer.getName()));
        }
    }

    private void nextRound() {
        ++round;
        idxTurnOrder = (round-1) % players.size();
        isReverseTurnOrder = false;
        isPickupTwo = false;
        cardsToPickup=0;
        for(Player p : players) {
            p.endOfRound();
        }
        updateRanks();
        deck=new Deck();
        dealStartingHand();
        sendPlayerTurnOrderDataUpdate();
        checkEndGameConditions();
        startTurn(true);
    }

    public void endGame() {
        for(Player p: players) {
            if(p.getRank()==1) {
                message.convertAndSendToUser(p.getSessionID(),"/queue/gameEnded",new AlertData(AlertTypes.GOOD,"You Win The Game!",String.format("Congratulations, you are among the winners with %d points!",p.getScore()),false));
                this.LOG.info("Player '{}' with playerID '{}' has been declared a winner with a rank of '{}' and score of '{}'.",p.getName(),p.getPlayerID(),p.getRank(),p.getScore());
            }
            else {
                message.convertAndSendToUser(p.getSessionID(),"/queue/gameEnded",new AlertData(AlertTypes.BAD,"You Loose The Game!",String.format("You loose the game with %d points. Better luck next time!",p.getScore()),false));
            }
        }
        players.clear();
        playerIdToPlayer.clear();
        currentPlayer = null;
        deck = null;
        isReverseTurnOrder=false;
        idxTurnOrder=0;
        isPickupTwo=false;
        cardsToPickup=0;
        this.LOG.info("Game has been closed!");
    }

    /**
     * Game ends when END_CONDITION_SCORE_THRESHOLD is reached by any player
     */
    private void checkEndGameConditions() {
        for(Player p : players) {
            if(p.getScore()>=END_CONDITION_SCORE_THRESHOLD) {
                state = GameState.ENDED;
                break;
            }
        }
        if(state == GameState.ENDED) {
            endGame();
        }
    }

    /**
     * Round ends when the deck is empty or any one player has played their last card
     */
    private void checkEndRoundConditions() {
        boolean isRoundOver = deck.getNumCardsInDeck()==0;
        for(Player p : players) {
            if(isRoundOver || p.isHandEmpty()) {
                isRoundOver=true;
                break;
            }
        }
        if(isRoundOver) {
            nextRound();
        }
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
//        cardsToPickup
    }

    public void actionDrawCard(Player player) {

    }

    public void actionSelectSuit(Player player, Suit suit) {

    }

    private void dealStartingHand() {
        //Deal each player a new hand
        for(Player p : players) {
            for(int i=0;i<STARTING_HAND_SIZE;++i) {
                p.addCard(deck.drawCard());
            }
        }
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

    private void sendGlobalMessage(Player sender,String msg) {
        String senderName = sender==null ? "GAME" : sender.getName().toUpperCase(Locale.ROOT);
        message.convertAndSend("/topic/messageReceived",new MessageData(senderName,msg));
    }

}
