package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Suit;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.OtherPlayerHandUpdate;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.PlayerTurnInfoData;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.PlayerUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.PlatformLoggingMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Player {
    private final Logger LOG = LoggerFactory.getLogger(Player.class);
    private final String sessionID;
    private final String playerID;
    private String name;
    private final ArrayList<Card> hand;
    private final HashMap<Suit,ArrayList<Card>> cardsOfSuit;
    private final ArrayList<Card> wildCards;
    private final HashSet<PlayerHandObserver> observers;
    private int rank;
    private int score;
    private boolean isTurnSkipped;

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isTurnSkipped() {
        return isTurnSkipped;
    }

    public void setTurnSkipped(boolean turnSkipped) {
        isTurnSkipped = turnSkipped;
    }

    public Player(String name, String sessionID, String playerID) {
        this.sessionID = sessionID;
        this.playerID = playerID;
        this.name = name;
        hand = new ArrayList<>();
        wildCards = new ArrayList<>(4);
        cardsOfSuit = new HashMap<>();
        observers = new HashSet<>();
        for(Suit s : Suit.values()) {
            cardsOfSuit.put(s,new ArrayList<>());
        }
        LOG.info("Created Player '{}' with ID '{}' and session '{}'",name,playerID,sessionID);
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getPlayerID() {
        return playerID;
    }

    public String getName() {
        return name;
    }

    /**
     * Adds a card to a players hand and updates all observers.
     * Cards must not already exist in a players hand.
     * RULE: Crazy Eights uses a standard deck with no duplicate cards
     * @param card The card that will be added to a players hand
     * @return True if the card was successfully added, False if duplicate or card was null
     */
    public boolean addCard(Card card) {
        if(card==null) {
            this.LOG.error("Player::addCard: PlayerID '{}', Player '{}' cannot add a null card to hand",playerID,name);
            return false;
        }
        else if(hand.contains(card)) {
            this.LOG.error("Player::addCard: PlayerID '{}', Player '{}' cannot add '{}' to hand because it is already inside the hand",playerID,name,card);
            return false;
        }

        hand.add(card);
        cardsOfSuit.get(card.suit).add(card);
        if(card.isWildCard()) {
            wildCards.add(card);
        }
        notifyHandUpdated();
        return true;
    }

    /**
     * Removes a card from a players hand and updates all observers.
     * Cards must already exist in a players hand.
     * @param card The card that will be added to a players hand
     * @return True if the card was successfully removed, False if player didn't have the card in hand or card was null
     */
    public boolean removeCard(Card card) {
        if(card==null) {
            this.LOG.error("Player::removeCard: PlayerID '{}', Player '{}' cannot remove a null card to hand",playerID,name);
            return false;
        }
        else if(!hand.remove(card)) {
            this.LOG.error("Player::removeCard: PlayerID '{}', Player '{}' cannot remove card '{}' because its not in their hand.",playerID,name,card);
            return false;
        }

        cardsOfSuit.get(card.suit).remove(card);
        if(card.isWildCard()) {
            wildCards.remove(card);
        }
        notifyHandUpdated();
        return true;
    }

    public int endOfRound() {
        for(Card c : hand) {
            score+=switch(c.getRank()) {
                case EIGHT -> 50;
                case JACK,QUEEN,KING,ACE -> 10;
                default -> c.getRank().rank;
            };
        }
        hand.clear();
        isTurnSkipped=false;
        notifyHandUpdated();
        return score;
    }

    /**
     * Returns a list of playable cards.
     * A card is playable if it's the current suit or if its a wildcard.
     * In Crazy Eights only 8's are wild
     * @param currentSuit The suit that should be considered current
     * @return A list of cards in a players hand that can be played given the current suit
     */
    public ArrayList<Card> getPlayableCards(Suit currentSuit) {
        ArrayList<Card> playableCards = new ArrayList<>(cardsOfSuit.get(currentSuit));
        for(Card c : wildCards) {
            if(!playableCards.contains(c)) {
                playableCards.add(c);
            }
        }
        return playableCards;
    }

    public boolean subscribeHandUpdates(PlayerHandObserver subscriber) {
        return observers.add(subscriber);
    }

    public boolean unsubscribeHandUpdates(PlayerHandObserver subscriber) {
        return observers.remove(subscriber);
    }

    private void notifyHandUpdated() {
        PlayerUpdate pu = new PlayerUpdate(
                playerID,
                name,
                hand
        );
        OtherPlayerHandUpdate opu = new OtherPlayerHandUpdate(
          playerID,
          name,
          hand.size()
        );
        HashSet<PlayerHandObserver> obs = new HashSet<>(observers);

        for(PlayerHandObserver ob : obs) {
            ob.handlePlayerHandUpdate(this,pu,opu);
        }
    }
}
