package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;

import java.util.ArrayList;

public record PlayerUpdate(
        String playerID,
        String playerName,
        ArrayList<String> cards
) {
}
