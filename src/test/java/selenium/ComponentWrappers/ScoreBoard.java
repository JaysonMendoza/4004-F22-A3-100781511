package selenium.ComponentWrappers;

import ca.jkmconsulting.crazyEightsCountdown.Player;
import org.openqa.selenium.By;

public abstract class ScoreBoard {
    static public final By byScoreBoard = By.name("scoreBoard");
    static public final By byH5CurrentPlayerTurn = By.name("h5CurrentTurnPlayer");
    static public final By byH5RoundNumber = By.name("h5RoundNumber");
    static public final By byH5DirectionOfPlay = By.name("h5DirectionOfPlay");

    static public By bytxtPlayerRank(Player player) {
        return By.name(String.format("rank_%s",player.getPlayerID()));
    }

    static public By bytxtPlayerScore(Player player) {
        return By.name(String.format("score_%s",player.getPlayerID()));
    }

    static public By bytxtPlayerName(Player player) {
        return By.name(String.format("pname_%s",player.getPlayerID()));
    }

    static public By bytxtPlayerListItem(Player player) {
        return By.name(String.format("listItem_%s",player.getPlayerID()));
    }

    public enum PLAY_DIRECTION {
        Normal,
        Reversed
    }

}
