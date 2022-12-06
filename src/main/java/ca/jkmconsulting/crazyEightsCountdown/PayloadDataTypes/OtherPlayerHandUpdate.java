package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

public record OtherPlayerHandUpdate(
        String playerID,
        String playerName,
        int numCards
) {
}
