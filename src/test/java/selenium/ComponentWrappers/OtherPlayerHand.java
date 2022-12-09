package selenium.ComponentWrappers;

import ca.jkmconsulting.crazyEightsCountdown.Player;
import org.openqa.selenium.By;

public abstract class OtherPlayerHand {

    static public By byOtherHandComponent(Player player) {
        return By.name(String.format("otherHand_%s",player.getPlayerID()));
    }

    static public By byPlayerCardGroup(Player player) {
        return By.name(String.format("otherHandCardGroup_%s",player.getPlayerID()));
    }

    static public By byPlayerName(Player player) {
        return By.name(String.format("otherHandPlayerName_%s",player.getPlayerID()));
    }
}
