package ca.jkmconsulting.crazyEightsCountdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public Player(String name,String sessionID,String playerID) {
        this.sessionID = sessionID;
        this.playerID = playerID;
        this.name = name;
        hand = new ArrayList<>();
        wildCards = new ArrayList<>(4);
        cardsOfSuit = new HashMap<>();
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
        return false;
    }

    private void notifyHandUpdated() {

    }
}
