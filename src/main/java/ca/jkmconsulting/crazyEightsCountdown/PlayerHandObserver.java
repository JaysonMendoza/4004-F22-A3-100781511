package ca.jkmconsulting.crazyEightsCountdown;

import java.util.ArrayList;

public interface PlayerHandObserver {
    void handlePlayerHandUpdate(ArrayList<Card> newHand);
}
