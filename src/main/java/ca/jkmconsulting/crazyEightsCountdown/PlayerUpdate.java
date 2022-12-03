package ca.jkmconsulting.crazyEightsCountdown;

import java.util.ArrayList;

public record PlayerUpdate(
        String playerID,
        String playerName,
        ArrayList<Object> cards
) {
}
