package ca.jkmconsulting.crazyEightsCountdown;

import java.util.ArrayList;

public interface PlayerHandObserver {
    void handlePlayerHandUpdate(Player player,PlayerUpdate updatePlayer, OtherPlayerHandUpdate updateOther);
}
