package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.ConfigReader;

import java.util.List;

public class CheckboxesPage extends BasePage {
    private final By boxes = By.cssSelector("#checkboxes input[type='checkbox']");

    public CheckboxesPage(WebDriver d) {
        super(d);
    }

    public CheckboxesPage open() {
        String baseUrl = ConfigReader.get("baseUrl", "https://the-internet.herokuapp.com");
        navigateTo(baseUrl + "/checkboxes");
        return this;
    }

    private List<WebElement> all() {
        waitForVisibility(boxes);
        return driver.findElements(boxes);
    }

    public void check(int index) {
        WebElement b = all().get(index);
        if (!b.isSelected()) b.click();
    }

    public void uncheck(int index) {
        WebElement b = all().get(index);
        if (b.isSelected()) b.click();
    }

    public boolean isChecked(int index) {
        return all().get(index).isSelected();
    }
}
