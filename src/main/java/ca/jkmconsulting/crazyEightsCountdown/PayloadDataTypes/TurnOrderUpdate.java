package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

import java.util.ArrayList;

public record TurnOrderUpdate(
        ArrayList<PlayerTurnInfo> turnSequence,
        boolean isPlayReversed,
        int round
) {
}
