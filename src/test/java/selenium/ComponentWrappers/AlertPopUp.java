package selenium.ComponentWrappers;

import org.openqa.selenium.By;

public abstract class AlertPopUp {
    static public final By byModalAlert = By.name("alertPop");
    static public final By byTxtTitle = By.name("alertTitle");
    static public final By byTxtAlertBody = By.name("alertBody");
    static public final By byBtnAlertClose = By.name("alertBtnClose");
}
