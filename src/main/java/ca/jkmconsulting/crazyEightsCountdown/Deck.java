package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import ca.jkmconsulting.crazyEightsCountdown.Enums.CardRank;
import ca.jkmconsulting.crazyEightsCountdown.Enums.Suit;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.GameBoardUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Deck {
    private final Logger LOG = LoggerFactory.getLogger(Deck.class);
    final private Stack<Card> drawDeck;
    final private ArrayList<Card> cardsIssued;
    final private Stack<Card> discardPile;
    private final HashSet<DeckObserver> observers;
    private Suit activeSuit;
    private CardRank activeCardRank;


    public Deck(ArrayList<Card> fixedOrder) {
        drawDeck = new Stack<>();
        cardsIssued = new ArrayList<>();
        discardPile = new Stack<>();
        observers = new HashSet<>();
        buildDeck(fixedOrder);
        this.LOG.info("Deck created.");
        activeSuit=null;
    }

    public Deck() {
        this(null);
    }

    public CardRank getActiveCardRank() {
        return activeCardRank;
    }

    public List<Card> getDiscardPile() {
        return discardPile.stream().toList();
    }

    public Suit getActiveSuit() {
        return activeSuit;
    }

    public void setActiveSuit(Suit newActiveSuit) {
        activeSuit = newActiveSuit;
    }
    private void buildDeck(ArrayList<Card> cardOrder) {
        drawDeck.clear();
        discardPile.clear();
        cardsIssued.clear();
        activeSuit=null;
        ArrayList<Card> allCards = new ArrayList<>(Arrays.asList(Card.values()));
        Collections.shuffle(allCards);
//        ArrayList<Card> order = cardOrder;
        //Add all cards to draw deck that are not included in stacked deck portion
        if(cardOrder!=null) {
            for(Card c : allCards) {
                if(!cardOrder.contains(c)) {
                    drawDeck.push(c);
                }
            }
            //Add stacked cards in proper order on top of shuffled deck (We draw from the end of the array)
            for(int i=cardOrder.size()-1;i>=0;--i) {
                drawDeck.push(cardOrder.get(i));
            }
        }
        else {
            drawDeck.addAll(allCards);
        }
        notifyDeckUpdated();
    }

    public int getNumCardsInDeck() {
        return drawDeck.size();
    }

    public Card drawCard() {
        if(drawDeck.isEmpty()) {
            return null;
        }
        Card c = drawDeck.pop();
        cardsIssued.add(c);
        notifyDeckUpdated();
        return c;
    }

    public boolean discardCard(Card card) {
        if( !(activeSuit==null || card.isWildCard() || card.getSuit()==activeSuit || card.getRank()== activeCardRank) ) {
            return false;
        }
        boolean rc = cardsIssued.remove(card);
        if(rc) {
            discardPile.push(card);
            activeSuit = card.suit;
            activeCardRank = card.getRank();
            notifyDeckUpdated();
        }
        return rc;
    }

    public boolean subscribeDeckpdates(DeckObserver subscriber) {
        return observers.add(subscriber);
    }

    public boolean unsubscribeDeckUpdates(DeckObserver subscriber) {
        return observers.remove(subscriber);
    }

    private void notifyDeckUpdated() {
        HashSet<DeckObserver> obs = new HashSet<>(observers);
        ArrayList<String> cards = new ArrayList<>();
        discardPile.forEach(c -> cards.add(c.toString()));
        GameBoardUpdate update = new GameBoardUpdate(
          drawDeck.size(),
          cards
        );
        this.LOG.info("Deck updated.");
        for(DeckObserver ob : obs) {
            ob.handleDeckUpdated(update);
        }
    }
}
