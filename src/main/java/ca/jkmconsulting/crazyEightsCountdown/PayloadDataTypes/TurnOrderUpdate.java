package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

import java.util.ArrayList;

public record TurnOrderUpdate(
        ArrayList<PlayerTurnInfoData> turnSequence,
        boolean isPlayReversed,
        int round
) {
}
