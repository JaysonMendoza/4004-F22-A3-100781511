package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

public record PlayerTurnInfo(
        String playerID,
        String playerName,
        int rank,
        int score,
        boolean isCurrentTurn,
        boolean isTurnSkipped
) {
}
