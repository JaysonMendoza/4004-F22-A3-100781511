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
    private boolean undoPointSaved;
    private Suit undoSuit;
    private Card undoCard;


    public Deck(ArrayList<Card> fixedOrder) {
        drawDeck = new Stack<>();
        cardsIssued = new ArrayList<>();
        discardPile = new Stack<>();
        observers = new HashSet<>();
        undoCard = null;
        undoSuit = null;
        undoPointSaved = false;
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

    public Card getTopDiscardCard() {
        if(discardPile.size()==0) {
            return null;
        }
        return discardPile.peek();
    }

    public void setActiveSuit(Suit newActiveSuit) {
        this.LOG.info("Active suit set to '{}'",newActiveSuit);

        activeSuit = newActiveSuit;
    }
    public void buildDeck(ArrayList<Card> cardOrder,HashSet<Card> exludeFromDeck, Card topCard) {
        drawDeck.clear();
        discardPile.clear();
        cardsIssued.clear();
        clearUndoPoint();
        activeSuit=null;
        ArrayList<Card> allCards = new ArrayList<>(Arrays.asList(Card.values()));
        if(exludeFromDeck!=null) {
            allCards.removeAll(exludeFromDeck);
            cardsIssued.addAll(exludeFromDeck);
        }
        if(topCard!=null) {
            allCards.remove(topCard);
            discardPile.push(topCard);
            activeSuit = topCard.getSuit();
            activeCardRank = topCard.getRank();
        }
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

    public void buildDeck(ArrayList<Card> cardOrder) {
        buildDeck(cardOrder,null,null);
    }

    public int getNumCardsInDeck() {
        return drawDeck.size();
    }

    public void captureUndoPoint() {
        undoCard = discardPile.peek();
        undoSuit = activeSuit;
        undoPointSaved = true;
        this.LOG.info("Undo Point captured with suit '{}' and card '{}'",undoSuit,undoCard);
    }

    public ArrayList<Card> restoreToUndoPoint() {
        if(!undoPointSaved) {
            this.LOG.error("Undo Point cannot be restored because it has yet to be saved! undoSuit '{}' and undoCard '{}'",undoSuit,undoCard);
            return null;
        }
        else if( discardPile.size()< 1 || (undoCard!=null && !discardPile.contains(undoCard)) ) {
            this.LOG.warn("Deck undo point could not be restored because the card no longer exists in the discardPile. Perhaps there was a new round?");
            clearUndoPoint();
            return null;
        }

        ArrayList<Card> cardsRemoved = new ArrayList<>();
        while(discardPile.size() > 0 && discardPile.peek()!=undoCard ) {
            Card c = discardPile.pop();
            cardsRemoved.add(c);
            cardsIssued.add(c);
            this.LOG.info("Undo Point CARD UNDONE '{}'",c);
        }
        activeSuit = undoSuit;
        Collections.reverse(cardsRemoved); //Make sure the first card in the array is first card played so controller can replay turn
        this.LOG.info("Undo Point RESTORED with suit '{}' and card '{}'",undoSuit,undoCard);
        notifyDeckUpdated();
        return cardsRemoved;
    }

    public void clearUndoPoint() {
        undoSuit = null;
        undoCard = null;
        undoPointSaved=false;
        this.LOG.info("Undo Point CLEARED.");
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
