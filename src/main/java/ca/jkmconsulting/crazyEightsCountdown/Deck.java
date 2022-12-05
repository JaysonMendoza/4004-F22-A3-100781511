package ca.jkmconsulting.crazyEightsCountdown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Deck {
    private final Logger LOG = LoggerFactory.getLogger(Deck.class);
    final private Stack<Card> drawDeck;
    final private ArrayList<Card> cardsIssued;
    final private Stack<Card> discardPile;
    private final HashSet<DeckObserver> observers;

    public Deck(ArrayList<Card> fixedOrder) {
        drawDeck = new Stack<>();
        cardsIssued = new ArrayList<>();
        discardPile = new Stack<>();
        observers = new HashSet<>();
        buildDeck(fixedOrder);
        this.LOG.info("Deck created.");
    }

    public Deck() {
        this(null);
    }

    public List<Card> getDiscardPile() {
        return discardPile.stream().toList();
    }

    public Card getTopDiscardedCard() {
        return discardPile.peek();
    }

    private void buildDeck(ArrayList<Card> cardOrder) {
        drawDeck.clear();
        discardPile.clear();
        cardsIssued.clear();
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
        boolean rc = cardsIssued.remove(card);
        if(rc) {
            discardPile.push(card);
            notifyDeckUpdated();
        }
        return rc;
    }

    public boolean subscribeHandUpdates(DeckObserver subscriber) {
        return observers.add(subscriber);
    }

    public boolean unsubscribeHandUpdates(DeckObserver subscriber) {
        return observers.remove(subscriber);
    }

    private void notifyDeckUpdated() {
        HashSet<DeckObserver> obs = new HashSet<>(observers);
        GameBoardUpdate update = new GameBoardUpdate(
          drawDeck.size(),
          new ArrayList<>(discardPile)
        );
        for(DeckObserver ob : obs) {
            ob.handleDeckUpdated(update);
        }
        this.LOG.info("Deck updated.");
    }
}
