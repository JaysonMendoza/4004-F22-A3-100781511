package ca.jkmconsulting.crazyEightsCountdown;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

public class Deck {
    final private Stack<Card> drawDeck;
    final private ArrayList<Card> cardsIssued;
    final private ArrayList<Card> discardPile;

    public Deck(ArrayList<Card> fixedOrder) {
        drawDeck = new Stack<>();
        cardsIssued = new ArrayList<>();
        discardPile = new ArrayList<>();
        buildDeck(fixedOrder);
    }

    public Deck() {
        this(null);
    }

    private void buildDeck(ArrayList<Card> cardOrder) {
        drawDeck.clear();
        discardPile.clear();
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
        else if(!cardsIssued.isEmpty()) { //If a reshuffle, then rebuild deck with the exclusion of issued cards
            for(Card c : allCards) {
                if(!cardsIssued.contains(c)) {
                    drawDeck.push(c);
                }
            }
        }
        else {
            drawDeck.addAll(allCards);
        }
    }

    public int getNumCardsInDeck() {
        return -1;
    }

    public Card drawCard() {
        if(drawDeck.isEmpty()) {
            buildDeck(null);
        }
        Card c = drawDeck.pop();
        cardsIssued.add(c);
        return c;
    }
}
