package selenium.ComponentWrappers;

import org.openqa.selenium.By;

public abstract class PopUpMessage {
    static final public By byModal = By.name("popMsg");
    static final public By byTitle = By.name("popTitle");
    static final public By byBody = By.name("popMessage");
    static final public By byBtnClose = By.name("btnPopClose");
}
