package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.ConfigReader;

public class DynamicLoadingPage extends BasePage {
    private final By startBtn = By.cssSelector("#start button");
    private final By finish   = By.cssSelector("#finish h4");

    public DynamicLoadingPage(WebDriver d) {
        super(d);
    }

    public DynamicLoadingPage open(int example) {
        String baseUrl = ConfigReader.get("baseUrl", "https://the-internet.herokuapp.com");
        navigateTo(baseUrl + "/dynamic_loading/" + example);
        return this;
    }

    /** Bấm Start rồi chờ (loading ~5s) đến khi chữ kết quả hiện ra */
    public String startAndGetResult() {
        click(startBtn);
        return getText(finish);
    }
}
