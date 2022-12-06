package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

public record PlayerTurnInfoData(
        String playerID,
        String playerName,
        int rank,
        int score,
        boolean isCurrentTurn,
        boolean isTurnSkipped
) {
}
