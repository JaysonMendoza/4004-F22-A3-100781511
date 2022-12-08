package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;

import java.util.List;

public record GameBoardUpdate(
        int numCardsDrawPile,
        List<String> discardPile
) {
} //TODO: Add type to ArrayList declaration once cards are made
