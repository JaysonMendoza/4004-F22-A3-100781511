package ca.jkmconsulting.crazyEightsCountdown.PayloadDataTypes;

import ca.jkmconsulting.crazyEightsCountdown.Enums.AlertTypes;

public record AlertData(
        AlertTypes type,
        String title,
        String message,
        boolean isClosable
) {
}
