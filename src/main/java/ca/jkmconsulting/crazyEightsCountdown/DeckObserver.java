package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.GameBoardUpdate;

public interface DeckObserver {
    void handleDeckUpdated(GameBoardUpdate updateData);
}
