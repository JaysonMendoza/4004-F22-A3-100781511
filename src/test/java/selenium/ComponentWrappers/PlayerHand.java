package selenium.ComponentWrappers;

import ca.jkmconsulting.crazyEightsCountdown.Enums.Card;
import org.openqa.selenium.By;

public abstract class PlayerHand {
    static final public By byBtnRedoTurn = By.name("btnRedoTurn");
    static final public  By byBtnPlayCard = By.name("btnPlayCard");
    static final public  By byGrpHandCards = By.name("grpHandCards");
    static final public  By byH2TxtPlayerName = By.name("txtHandPlayerName");
    static final public By byH5CurrentTurnPlayer = By.name("h5CurrentTurnPlayer");

    static public By byCardEnum(Card card) {
        return By.name(card.toString());
    }

}
