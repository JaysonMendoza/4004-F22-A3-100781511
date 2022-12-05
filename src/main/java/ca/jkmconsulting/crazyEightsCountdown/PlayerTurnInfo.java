package ca.jkmconsulting.crazyEightsCountdown;

public record PlayerTurnInfo(
        String playerID,
        String playerName,
        int rank,
        int score,
        boolean isCurrentTurn,
        boolean isTurnSkipped
) {
}
