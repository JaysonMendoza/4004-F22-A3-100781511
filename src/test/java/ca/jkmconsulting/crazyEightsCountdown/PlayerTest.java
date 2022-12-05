package ca.jkmconsulting.crazyEightsCountdown;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    public void I2_addCard() {
        Player p = new Player("Player1","kkk", UUID.randomUUID().toString());

        //Cannot add null cards
        assertFalse(p.addCard(null));

        //Add Cards
        for(Card c : Card.values()) {
            assertTrue(p.addCard(c));
        }
        //Add Duplicate Cards should Fail
        for(Card c : Card.values()) {
            assertFalse(p.addCard(c));
        }
    }

    @Test
    public void I3_removeCard() {
        Player p = new Player("Player1","kkk", UUID.randomUUID().toString());

        //Cannot remove null cards
        assertFalse(p.removeCard(null));

        //Cannot remove a card not in hand
        assertFalse(p.removeCard(Card.HEARTS_8));

        //Add Cards
        for(Card c : Card.values()) {
            assertTrue(p.addCard(c));
        }

        boolean initialDuplicateRemoveDone = false;
        for(Card c : Card.values()) {
            assertTrue(p.removeCard(c));
            if(!initialDuplicateRemoveDone) {
                assertFalse(p.removeCard(c));
                initialDuplicateRemoveDone=true;
            }
        }
    }

    /**
     * Returns a list of cards that are playable by the rules.
     * This will be wildcards and cards of the active suit
     */
    @Test
    public void I4_getListPlayableCards() {
        Player p = new Player("Player1","kkk", UUID.randomUUID().toString());
        ArrayList<Card> hand = new ArrayList<>();
        hand.add(Card.HEARTS_ACE);
        hand.add(Card.HEARTS_10);
        hand.add(Card.HEARTS_8);
        hand.add(Card.CLUBS_4);
        hand.add(Card.CLUBS_JACK);
        hand.add(Card.CLUBS_2);
        hand.add(Card.SPADES_7);
        hand.add(Card.SPADES_KING);
        hand.add(Card.SPADES_8);
        hand.add(Card.SPADES_3);

        assertNotNull(p.getPlayableCards(Suit.HEARTS),"should not return null on empty set");

        assertEquals(0,p.getPlayableCards(Suit.SPADES).size(),"Should return size zero with no cards");

        for(Card c : hand) {
            assertTrue(p.addCard(c));
        }

        //Hearts should have 2 regular hearts plus 2 wildcards (including the heart 8) = 4
        assertEquals(4,p.getPlayableCards(Suit.HEARTS).size(),"Hearts returned an incorrect size of playable cards");

        //Diamonds should have only 2 playable cards because of wildcards only
        assertEquals(2,p.getPlayableCards(Suit.DIAMONDS).size(),"Diamonds returned an incorrect size of playable cards");

        //Clubs should have 3 regular plus 2 wild = 5
        assertEquals(5,p.getPlayableCards(Suit.CLUBS).size(),"Clubs returned an incorrect size of playable cards");

        //Spades should have three playable cards plus two wildcards (including the spade 8) = 5
        assertEquals(5,p.getPlayableCards(Suit.SPADES).size(),"Spades returned an incorrect size of playable cards");
    }

    @Test
    public void I5_playerHandObserver() throws Exception {
        Player p = new Player("Player1","kkk", UUID.randomUUID().toString());
        HandObserverMock ob = new HandObserverMock(p); //This will subscribe and throw error if it fails
        assertFalse(p.subscribeHandUpdates(ob),"Should fail to sub twice");
        assertTrue(p.addCard(Card.HEARTS_8));
        assertTrue(ob.hasNext());
        assertTrue(ob.getNextHandUpdate().contains(Card.HEARTS_8));

        assertTrue(p.removeCard(Card.HEARTS_8));
        assertTrue(ob.hasNext());
        assertEquals(0,ob.getNextHandUpdate().size());

        assertTrue(p.unsubscribeHandUpdates(ob));
        assertFalse(p.unsubscribeHandUpdates(ob),"Should fail to unsub when not subbed");
        p.addCard(Card.CLUBS_JACK);
        assertFalse(ob.hasNext());
    }


}