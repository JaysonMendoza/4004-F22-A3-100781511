package ca.jkmconsulting.crazyEightsCountdown;

import java.util.ArrayList;

public record TurnOrderUpdate(
        ArrayList<PlayerTurnInfo> turnSequence
) {
}
