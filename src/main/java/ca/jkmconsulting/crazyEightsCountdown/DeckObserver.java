package ca.jkmconsulting.crazyEightsCountdown;

public interface DeckObserver {
    void handleDeckUpdated(GameBoardUpdate updateData);
}
