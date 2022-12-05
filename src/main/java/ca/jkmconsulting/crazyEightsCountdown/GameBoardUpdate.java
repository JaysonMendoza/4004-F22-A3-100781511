package ca.jkmconsulting.crazyEightsCountdown;

import java.util.ArrayList;

public record GameBoardUpdate(
        int numCardsDrawPile,
        ArrayList<Card> discardPile
) {
} //TODO: Add type to ArrayList declaration once cards are made
