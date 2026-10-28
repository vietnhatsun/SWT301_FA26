package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.Select;
import utils.ConfigReader;

public class DropdownPage extends BasePage {
    private final By dropdown = By.id("dropdown");

    public DropdownPage(WebDriver d) {
        super(d);
    }

    public DropdownPage open() {
        String baseUrl = ConfigReader.get("baseUrl", "https://the-internet.herokuapp.com");
        navigateTo(baseUrl + "/dropdown");
        return this;
    }

    public void select(String visibleText) {
        new Select(waitForVisibility(dropdown)).selectByVisibleText(visibleText);
    }

    public String getSelected() {
        return new Select(waitForVisibility(dropdown)).getFirstSelectedOption().getText();
    }
}
