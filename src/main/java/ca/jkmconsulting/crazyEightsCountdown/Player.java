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
        return false;
    }
}
