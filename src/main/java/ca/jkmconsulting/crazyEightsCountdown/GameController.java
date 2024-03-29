package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.*;
import ca.jkmconsulting.crazyEightsCountdown.Exceptions.*;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.*;

@Controller
public class GameController implements DeckObserver {
    public final int PICKUP_TWO_INCREMENT = 2;
    public final int MAX_PLAYERS = 4;
    public final int END_CONDITION_SCORE_THRESHOLD =100;
    public final int STARTING_HAND_SIZE = 5;
    private GameState state;
    private final Logger LOG = LoggerFactory.getLogger(GameController.class);
    private final ArrayList<Player> players = new ArrayList<>();
    private final HashMap<String,Player> playerIdToPlayer = new HashMap<>();
    private int idxTurnOrder;
    private int round;
    private boolean isReverseTurnOrder;
    private boolean isNextPlayerPickupTwo;
    private boolean isWaitingOnSuitSelection;
    private boolean isTestMode;
    private int cardsDrawnInTurn = 0;
    private ArrayList<Card> testModeCardOrder;
    private Set<Card> pickupTwoPlayedCards;
    private int pickupTwoRequired;
    private Player currentPlayer;
    private final Deck deck = new Deck();

    @Autowired
    SimpMessagingTemplate message;

    public GameController() {
        this.LOG.info("GameController constructed.");
        state = GameState.OPEN;
        isReverseTurnOrder=false;
        idxTurnOrder=0;
        isNextPlayerPickupTwo =false;
        isWaitingOnSuitSelection=false;
        pickupTwoPlayedCards = null;
        isTestMode = false;
        testModeCardOrder=null;
        pickupTwoRequired=PICKUP_TWO_INCREMENT;
    }

    public void setupGame(ArrayList<Card> cardOrder) {
        deck.subscribeDeckpdates(this);
        deck.buildDeck(cardOrder);
        round=1;
        pickupTwoPlayedCards=null;
        isWaitingOnSuitSelection=false;
        initTurnOrder();
        startGame();
    }

    /**
     * use After Construction before game starts
     * @param cardOrder An array list with cards in order which they should be drawn from the top of the deck
     */
    public void activateTestMode(ArrayList<Card> cardOrder) {
        isTestMode = true;
        this.testModeCardOrder = cardOrder;
    }

    public Deck getDeck() {
        return deck;
    }

    public GameState getState() {
        return state;
    }

    public ArrayList<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    public HashMap<String, Player> getPlayerIdToPlayer() {
        return new HashMap<>(playerIdToPlayer);
    }

    public int getRound() {
        return round;
    }

    public boolean isReverseTurnOrder() {
        return isReverseTurnOrder;
    }

    public boolean isNextPlayerPickupTwo() {
        return isNextPlayerPickupTwo;
    }

    public boolean isWaitingOnSuitSelection() {
        return isWaitingOnSuitSelection;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void startGame() {
        state = GameState.RUNNING;

        dealStartingHand();
        //send start game signal
        message.convertAndSend("/topic/startGame","");
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
        sendPlayerTurnOrderDataUpdate();
    }

    private void nextTurn() {
        pickupTwoPlayedCards=null;
        isWaitingOnSuitSelection=false;
        cardsDrawnInTurn = 0;
        deck.clearUndoPoint();
        if(state != GameState.RUNNING) {
            this.LOG.error("Game is in invalid state. Should be '{}' but is instead '{}'.",GameState.RUNNING,state);
        }
        if(checkEndRoundConditions()) {
            return;
        }
        int increment = isReverseTurnOrder ? -1 : 1;
        while(players.size()!=1 && currentPlayer.equals(players.get(idxTurnOrder)) || players.get(idxTurnOrder).isTurnSkipped()) {
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
        if(isNextPlayerPickupTwo) {
            startPickupTwoEvent();
        }
        else {
            message.convertAndSendToUser(currentPlayer.getSessionID(),"/queue/alert",new AlertData(AlertTypes.NEUTRAL,"Your Turn!",String.format("It's your turn. Play a card. You may play any EIGHT, or a card of the suit %s",deck.getActiveSuit()),true));
            sendGlobalMessage(null,String.format("Player %s begins their turn!",currentPlayer.getName()));
        }
    }

    private void nextRound() {
        ++round;
        idxTurnOrder = (round-1) % players.size();
        isReverseTurnOrder = false;
        isNextPlayerPickupTwo = false;
        isWaitingOnSuitSelection=false;
        pickupTwoPlayedCards=null;
        for(Player p : players) {
            p.endOfRound();
        }
        updateRanks();
        deck.buildDeck(null);
        dealStartingHand();
        sendPlayerTurnOrderDataUpdate();
        if(!checkEndGameConditions())
        {
            startTurn(true);
        }
    }

    public void endGame() {
        String winners = "";
        for(Player p: players) {
            if(p.getRank()==1) {
                sendAlert(p,new AlertData(AlertTypes.GOOD,"You Win The Game!",String.format("Congratulations, you are among the winners with %d points!",p.getScore()),false));
                winners+=String.format(",%s",p.getName());
                this.LOG.info("Player '{}' with playerID '{}' has been declared a winner with a rank of '{}' and score of '{}'.",p.getName(),p.getPlayerID(),p.getRank(),p.getScore());
            }
            else {
                sendAlert(p,new AlertData(AlertTypes.BAD,"You Loose The Game!",String.format("You loose the game with %d points. Better luck next time!",p.getScore()),false));
            }
        }
        winners = winners.substring(1);
        players.clear();
        playerIdToPlayer.clear();
        currentPlayer = null;
        deck.unsubscribeDeckUpdates(this);
        isReverseTurnOrder=false;
        isWaitingOnSuitSelection=false;
        idxTurnOrder=0;
        isNextPlayerPickupTwo =false;
        pickupTwoPlayedCards=null;
        sendGlobalMessage(null,String.format("Player(s) %s have won the game!",winners));
        message.convertAndSend("/topic/gameEnded","");
        this.LOG.info("Game has been closed!");
    }

    /**
     * Game ends when END_CONDITION_SCORE_THRESHOLD is reached by any player
     */
    private boolean checkEndGameConditions() {
        for(Player p : players) {
            if(p.getScore()>=END_CONDITION_SCORE_THRESHOLD) {
                state = GameState.ENDED;
                break;
            }
        }
        if(state == GameState.ENDED) {
            endGame();
            return true;
        }
        return false;
    }

    /**
     * Round ends when the deck is empty or any one player has played their last card
     */
    private boolean checkEndRoundConditions() {
        boolean isRoundOver = deck.getNumCardsInDeck()==0;
        for(Player p : players) {
            if(isRoundOver || p.getHandSize()==0) {
                isRoundOver=true;
                break;
            }
        }
        if(isRoundOver) {
            nextRound();
            return true;
        }
        return false;
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
            setupGame(testModeCardOrder);
        }
    }

    public void actionPlayerPlayCard(Player player, Card card) throws CrazyEightsIllegalCardException, CrazyEightsInvalidPlayerException {
        this.LOG.info("actionPlayerPlayCard: PlayerID '{}' sends action to play the '{}' card.",player.getPlayerID(),card);
        if(!players.contains(player)) {
            this.LOG.error("Player '{}' attempted to play but is not part of the game!");
            throw new CrazyEightsInvalidPlayerException(String.format("Player %s attempted to play but is not part of the game!",player.getPlayerID()));
        }
        else if(!currentPlayer.equals(player)) { //Case: Not players turn
            this.LOG.warn("Player '{}' attempted to play a card out of turn and was rejected.",player.getPlayerID());
            sendAlert(player,new AlertData(AlertTypes.BAD,"Play Card : Not Your Turn!","You cannot play a card unless it is your turn.",true));
            return;
        }
        else if(!player.hasCardInHand(card)) { //Case: Player doesn't have the card
            this.LOG.warn("Player '{}' attempted to play a '{}' which they do not have and was rejected.",player.getPlayerID(),card);
            sendAlert(player,new AlertData(AlertTypes.BAD,"Play Card : Card Not In Hand",String.format("You attempted to play %s but it's not in your hand. Choose a valid card.",card),true));
            return;
        }

        if(!deck.discardCard(card)) {
            this.LOG.warn("Player '{}' attempted to play an invalid card '{}'.",player.getPlayerID(),card);
            sendAlert(player,new AlertData(AlertTypes.BAD,"Play Card : Invalid Card",String.format("The card %s is of an invalid suit or Rank and/or is not a wild card. You must choose to play an 8 wildcard, a card of the %s suit, or a %s card.",card,deck.getActiveSuit(),deck.getActiveCardRank()),true));
            return;
        }

        if(!player.removeCard(card)) {
            this.LOG.error(String.format("PlayerID %s attempted to play the card %s. However, it was not in their hand. This should not have happened, please check the logic.",player.getPlayerID(),card));
            throw new CrazyEightsIllegalCardException(String.format("PlayerID %s attempted to play the card %s. However, it was not in their hand. This should not have happened, please check the logic.",player.getPlayerID(),card));
        }

        //Handle Pickup 2 if in progress
        if(pickupTwoPlayedCards!=null && !pickupTwoPlayedCards.add(card)) {
            this.LOG.error("Player played a card that was already played for pickup 2. Please check logic.");
            throw new CrazyEightsIllegalCardException("Player played a card that was already played for pickup 2. Please check logic.");
        }

        //Handle Special Cards played
        switch(card) {
            case DIAMONDS_8,SPADES_8,HEARTS_8,CLUBS_8 -> handleWildCardPlayed();
            case CLUBS_ACE,SPADES_ACE,DIAMONDS_ACE,HEARTS_ACE -> handleAcePlayed();
            case HEARTS_QUEEN,SPADES_QUEEN,CLUBS_QUEEN,DIAMONDS_QUEEN -> handleQueenPlayed();
            case DIAMONDS_2,SPADES_2,HEARTS_2,CLUBS_2 -> {
                boolean twoPlayed = false;

                if(pickupTwoPlayedCards!= null) {
                    for(Card c : pickupTwoPlayedCards) {
                        if(c.getRank()== CardRank.TWO) {
                            twoPlayed = true;
                            break;
                        }
                    }
                }

                pickupTwoRequired = twoPlayed ? pickupTwoRequired+PICKUP_TWO_INCREMENT : PICKUP_TWO_INCREMENT;
                isNextPlayerPickupTwo = true;
            }
        }

        sendGlobalMessage(player,String.format("%s plays a %s.",player.getName(),card));
        checkIfPickupTwoEventCompleted(false);

        if(state == GameState.PICKUP_TWO_ACTIVE || isWaitingOnSuitSelection) {
            return;
        }
        nextTurn();
    }

    public void actionDrawCard(Player player) throws CrazyEightsIllegalCardException, CrazyEightsInvalidPlayerException {
        this.LOG.info("actionDrawCard: PlayerID '{}' sends action to draw card.",player.getPlayerID());
        int pickup = 1;
        if(!players.contains(player)) {
            this.LOG.error("Player '{}' attempted to play but is not part of the game!");
            throw new CrazyEightsInvalidPlayerException(String.format("Player %s attempted to draw but is not part of the game!",player.getPlayerID()));
        }
        else if(!currentPlayer.equals(player)) { //Case: Not players turn
            this.LOG.warn("Player '{}' attempted to draw a card out of turn and was rejected.",player.getPlayerID());
            sendAlert(player,new AlertData(AlertTypes.BAD,"Draw Card : Not Your Turn!","You cannot draw a card unless it is your turn.",true));
            return;
        }
        else if(pickupTwoPlayedCards!=null && pickupTwoPlayedCards.size() > 0) {
            //User already choose to play instead of pickup and cannot draw.
            this.LOG.warn("Player '{}' attempted to draw a card for pickup 2, but has already committed to playing.");
            sendAlert(player,new AlertData(AlertTypes.BAD,"Draw Card : Pickup Two - Cannot Draw","You have chosen to play cards for the pickup two event and cannot draw. If you would like to draw instead, please select the 'REDO TURN' button.",true));

        }
        else if(state==GameState.PICKUP_TWO_ACTIVE)
        {
            pickup = pickupTwoRequired;
            cardsDrawnInTurn = cardsDrawnInTurn - pickupTwoRequired;
        }
        ArrayList<Card> drawnCards = new ArrayList<>();
        for(int i=0;i<pickup;++i) {
            drawnCards.add(deck.drawCard());
            sendIndividualMessage(null,currentPlayer,String.format("You draw a %s",drawnCards.get(drawnCards.size()-1)));
            ++cardsDrawnInTurn;
            if(drawnCards.get(drawnCards.size()-1) == null) {
                break;
            }
            if(!player.addCard(drawnCards.get(drawnCards.size()-1))) {
                this.LOG.error("PlayerID '{}' tried to add a drawn card '{}' but it failed because they already have it. This should never happen.",player.getPlayerID(),drawnCards.get(drawnCards.size()-1));
                throw new CrazyEightsIllegalCardException(String.format("PlayerID %s drew the %s card that they already had. This should not be possible. Verify the logic.",player.getPlayerID(),drawnCards.get(drawnCards.size()-1)));
            }
        }
        sendGlobalMessage(player,String.format("%s draws %d cards!",player.getName(),pickup));
        checkIfPickupTwoEventCompleted(true);
        if(cardsDrawnInTurn >= 3) {
            if(deck.checkIfCardCanBePlayed(drawnCards.get(drawnCards.size()-1))) {
                actionPlayerPlayCard(player,drawnCards.get(drawnCards.size()-1));
            }
            else {
                nextTurn();
            }
        }
    }

    public void actionSelectSuit(Player player, Suit suit) throws CrazyEightsInvalidPlayerException {
        //If pickup 2 then they must choose a suit where they have at least one other playable cards
        this.LOG.info("actionSelectSuit : PlayerID '{}' sends action to select suit '{}'",player.getPlayerID(),suit);
        if(!players.contains(player)) {
            this.LOG.error("Player '{}' attempted to select suit but is not part of the game!");
            throw new CrazyEightsInvalidPlayerException(String.format("Player %s attempted to play but is not part of the game!",player.getPlayerID()));
        }
        else if(!currentPlayer.equals(player)) { //Case: Not players turn
            this.LOG.warn("Player '{}' attempted to select suit out of turn and was rejected.",player.getPlayerID());
            sendAlert(player,new AlertData(AlertTypes.BAD,"Select Suit : Not Your Turn!","You cannot play a card unless it is your turn.",true));
            return;
        }
        else if(!isWaitingOnSuitSelection) {
            this.LOG.warn("Player '{}' attempted to select suit but this is not an active event.",player.getPlayerID());
            sendAlert(player,new AlertData(AlertTypes.BAD,"Select Suit : Not Active!","You need to play a wildcard before you select a suit.",true));
            return;
        }

        deck.setActiveSuit(suit);
        isWaitingOnSuitSelection=false;
        sendGlobalMessage(player,String.format("%s changed active suit to %s.",player.getName(),suit));
        AlertData payload = new AlertData(AlertTypes.NEUTRAL,"Active Suit Changed by Wildcard",String.format("%s has changed the active suit to %s.",player.getName(),suit),true);
        for(Player p: players) {
            sendAlert(p,payload);
        }

        checkIfPickupTwoEventCompleted(false);
        if(state == GameState.RUNNING) {
            nextTurn();
        }
    }

    public void actionRedoTurn(Player player) throws CrazyEightsInvalidPlayerException, CrazyEightsPickupTwoUnexpectedStateException {
        this.LOG.info("actionRedoTurn : PlayerID '{}' sends action to redo turn",player.getPlayerID());
        if(!players.contains(player)) {
            this.LOG.error("Player '{}' attempted to select suit but is not part of the game!");
            throw new CrazyEightsInvalidPlayerException(String.format("Player %s attempted to redo turn but is not part of the game!",player.getPlayerID()));
        }
        else if(!currentPlayer.equals(player)) { //Case: Not players turn
            this.LOG.warn("Player '{}' attempted to select suit out of turn and was rejected.",player.getPlayerID());
            sendAlert(player,new AlertData(AlertTypes.BAD,"Redo Turn : Pickup Two - Not Your Turn!","You cannot redo your turn because it is not your turn.",true));
            return;
        }
        else if(state!=GameState.PICKUP_TWO_ACTIVE) {
            this.LOG.warn("Player '{}' attempted to redo their turn and was rejected.",player.getPlayerID());
            sendAlert(player,new AlertData(AlertTypes.BAD,"Redo Turn : Pickup Two Not Active!","You cannot redo your turn because the pickup two event is not active.",true));
            return;
        }

        ArrayList<Card> cardsReturned = deck.restoreToUndoPoint(); //This returns cards in the order they were played
        if(cardsReturned==null) {
            this.LOG.error("The game failed to restore an undo point for PlayerID '{}'. This should never happen!",currentPlayer.getPlayerID());
            throw new CrazyEightsPickupTwoUnexpectedStateException(String.format("The game failed to restore an undo point for PlayerID %s. This should never happen!",currentPlayer.getPlayerID()));
        }
        //Undo special card effects
        for(Card c : cardsReturned) {
            switch(c) {
                case CLUBS_ACE,SPADES_ACE,DIAMONDS_ACE,HEARTS_ACE -> {
                    isReverseTurnOrder = !isReverseTurnOrder;
                    sendGlobalMessage(null,"Undo ACE : Order of Play Restored.");
                    sendPlayerTurnOrderDataUpdate();
                }
                case HEARTS_QUEEN,SPADES_QUEEN,CLUBS_QUEEN,DIAMONDS_QUEEN -> {
                    int idx = isReverseTurnOrder ? idxTurnOrder-1 : idxTurnOrder+1;
                    if(idx >= players.size()) {
                        idx=0;
                    }
                    else if(idx < 0) {
                        idx=players.size()-1;
                    }
                    players.get(idx).setTurnSkipped(false);
                    sendGlobalMessage(null,String.format("Undo QUEEN : Player %s's turn is no longer skipped.",players.get(idx).getName()));
                }
                case DIAMONDS_2,SPADES_2,HEARTS_2,CLUBS_2 -> isNextPlayerPickupTwo=false;
            }
            currentPlayer.addCard(c);
        }
        pickupTwoPlayedCards.clear();
        sendIndividualMessage(null,currentPlayer,"You redo your turn!");
        sendAlert(currentPlayer,new AlertData(AlertTypes.NEUTRAL,"Redo Turn : Pickup Two","Your turn has been undone. Please try again.",true));
    }

    private void handleWildCardPlayed() {
        if(currentPlayer.getHandSize()<1) {
            return;
        }
        isWaitingOnSuitSelection=true;
        sendGlobalMessage(null,String.format("%s played a wildcard and is picking a new suit.",currentPlayer.getName()));
        message.convertAndSendToUser(currentPlayer.getPlayerID(),"/queue/selectSuit","");
    }

    private void handleQueenPlayed() {
        int idx = isReverseTurnOrder ? idxTurnOrder-1 : idxTurnOrder+1;
        if(idx >= players.size()) {
            idx=0;
        }
        else if(idx < 0) {
            idx=players.size()-1;
        }
        players.get(idx).setTurnSkipped(true);
        sendGlobalMessage(null,String.format("%s played a queen and player will need to skip their next turn.",currentPlayer.getName(),players.get(idx).getName()));
        sendPlayerTurnOrderDataUpdate();
    }

    private void startPickupTwoEvent() {
        state = GameState.PICKUP_TWO_ACTIVE;
        pickupTwoPlayedCards=new HashSet<>(pickupTwoRequired); //This starts the event
        isNextPlayerPickupTwo =false;
        deck.captureUndoPoint();
        message.convertAndSendToUser(currentPlayer.getSessionID(),"/queue/alert",new AlertData(AlertTypes.BAD,"Your Turn : Pickup 2",String.format("You must either play 2 cards immediately, or draw 2 cards. You may play any EIGHT, or a card of the suit %s",deck.getActiveSuit()),true));
        sendGlobalMessage(null,String.format("Player %s begins their turn and must pickup %d cards!",currentPlayer.getName(),pickupTwoRequired));
        message.convertAndSendToUser(currentPlayer.getPlayerID(),"/queue/pickUpTwoStart","");
    }

    private void checkIfPickupTwoEventCompleted(boolean playerDrewCards) {
        if(state!=GameState.PICKUP_TWO_ACTIVE) {
            return;
        }
        else if(!playerDrewCards && pickupTwoPlayedCards!=null && pickupTwoPlayedCards.size() < pickupTwoRequired) {
            return;
        }

        state = GameState.RUNNING;
        sendGlobalMessage(null,"Pickup two event has ended.");
        message.convertAndSendToUser(currentPlayer.getPlayerID(),"/queue/pickUpTwoEnd","");
    }

    private void handleAcePlayed() {
        isReverseTurnOrder = !isReverseTurnOrder;
        sendGlobalMessage(null,String.format("%s played an Ace. The order of play has been reversed!",currentPlayer.getName()));
        sendPlayerTurnOrderDataUpdate();
    }

    private void dealStartingHand() {
        //Deal each player a new hand
        for(int i=0;i<STARTING_HAND_SIZE;++i) {
            for(int j=0;j<players.size();++j) {
                players.get(j).addCard(deck.drawCard());
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
        message.convertAndSendToUser(player.getPlayerID(),"/queue/alert",data);
    }

    private void sendGlobalMessage(Player sender,String msg) {
        String senderName = sender==null ? "GAME" : sender.getName().toUpperCase(Locale.ROOT);
        message.convertAndSend("/topic/messageReceived",new MessageData(senderName,msg));
    }

    private void sendIndividualMessage(Player sender,Player receiver,String msg) {
        String senderName = sender==null ? "GAME" : sender.getName().toUpperCase(Locale.ROOT);
        message.convertAndSendToUser(receiver.getPlayerID(),"/queue/messageReceived",new MessageData(senderName,msg));
    }

}
