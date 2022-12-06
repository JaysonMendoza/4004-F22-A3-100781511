package ca.jkmconsulting.crazyEightsCountdown;

import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.OtherPlayerHandUpdate;
import ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes.PlayerUpdate;

public interface PlayerHandObserver {
    void handlePlayerHandUpdate(Player player, PlayerUpdate updatePlayer, OtherPlayerHandUpdate updateOther);
}
