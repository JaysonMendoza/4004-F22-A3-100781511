package ca.jkmconsulting.crazyEightsCountdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @BeforeEach
    void setUp() {

    }

    @Test
    public void I1_testDeckBuildingHasAllCards() {
        //Case 1 : Standard deck
        ArrayList<Card> allCards = new ArrayList<>(Arrays.asList(Card.values()));
        ArrayList<Card> test1 = new ArrayList<>(allCards);
        assertEquals(allCards.size(),test1.size());
        Deck deck = new Deck();
        for(int i=0;i<allCards.size();++i) {
            Card c = deck.drawCard();
            assertTrue(test1.remove(c));
        }
        assertEquals(0,test1.size());

        //Case 2: Deck built with a specific order for testing
        ArrayList<Card> test2 = new ArrayList<>();
        test2.add(Card.CLUBS_4);
        test2.add(Card.DIAMONDS_ACE);
        test2.add(Card.CLUBS_JACK);
        test2.add(Card.HEARTS_QUEEN);
        test2.add(Card.SPADES_KING);
        test2.add(Card.SPADES_QUEEN);
        test2.add(Card.HEARTS_2);
        test2.add(Card.HEARTS_3);
        test2.add(Card.HEARTS_4);
        test2.add(Card.HEARTS_5);
        test2.add(Card.HEARTS_6);
        test2.add(Card.HEARTS_7);
        test2.add(Card.HEARTS_8);
        test2.add(Card.HEARTS_9);
        test2.add(Card.HEARTS_10);
        test2.add(Card.HEARTS_JACK);
        test2.add(Card.HEARTS_KING);
        test2.add(Card.HEARTS_ACE);
        int lastIndex = test2.size()-1;
        deck = new Deck(test2);
        allCards.forEach(c ->{
           if(!test2.contains(c)) {
               test2.add(c);
           }
        });
        assertEquals(allCards.size(),test2.size());

        for(int i=0;i<test2.size();++i) {
            Card c = deck.drawCard();
            if(i<=lastIndex) {
                assertEquals(test2.get(i),c);
            }
            assertTrue(allCards.remove(c));
        }
        assertNull(deck.drawCard(),"Empty deck should not reshuffle. Only round changes cause reshuffle");
        assertEquals(0,allCards.size());
    }
}