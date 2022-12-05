package ca.jkmconsulting.crazyEightsCountdown;

import java.util.List;

public record GameBoardUpdate(
        int numCardsDrawPile,
        List<Card> discardPile
) {
} //TODO: Add type to ArrayList declaration once cards are made
