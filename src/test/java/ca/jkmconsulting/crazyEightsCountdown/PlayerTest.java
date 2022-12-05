package ca.jkmconsulting.crazyEightsCountdown;

import org.junit.jupiter.api.Test;

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



}