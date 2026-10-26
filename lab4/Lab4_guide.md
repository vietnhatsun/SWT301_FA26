# Lab 4 – Hướng dẫn chi tiết: Kiểm thử tự động với Selenium WebDriver + JUnit 5 (Java)

> Môn: SWT301 – Software Testing
> Website test: https://the-internet.herokuapp.com
> Ngôn ngữ: Java 17 · Maven · JUnit 5 · Selenium 4

Tài liệu gồm 4 phần:

1. **Lý thuyết nền tảng** – những gì cần hiểu trước khi code.
2. **Kiến thức nâng cao** – những gì giúp test ổn định, dễ bảo trì, chạy tự động bằng 1 lệnh.
3. **Thực hành step-by-step** – TODO 1 → 6 của lab gốc, giải thích từng bước và các lỗi hay gặp.
4. **Bài tập mở rộng (TODO 7 → 11)** – áp dụng kiến thức nâng cao.

---

# PHẦN 1 – LÝ THUYẾT NỀN TẢNG

## 1.1. Kiểm thử tự động (Automation Testing) là gì?

Kiểm thử tự động là dùng **script/công cụ** để thực thi test case, so sánh kết quả thực tế với kết quả mong đợi và báo cáo – thay vì tester thao tác bằng tay.

| Tiêu chí | Manual Testing | Automation Testing |
|---|---|---|
| Tốc độ chạy lại | Chậm, tốn công | Nhanh, chạy lại không giới hạn |
| Chi phí ban đầu | Thấp | Cao (viết script, dựng framework) |
| Phù hợp | Exploratory, UX, test 1 lần | Regression, smoke, data-driven, CI/CD |
| Độ chính xác | Phụ thuộc con người | Ổn định nếu script tốt |
| Phát hiện lỗi giao diện "lạ" | Tốt | Kém (chỉ kiểm điều được viết sẵn) |

**Khi nào nên tự động hóa?** Test lặp lại nhiều lần (regression), test nhiều bộ dữ liệu, luồng nghiệp vụ quan trọng ổn định (login, checkout), smoke test sau mỗi build.
**Không nên:** chức năng đang thay đổi liên tục, test chỉ chạy 1 lần, đánh giá cảm quan UI/UX.

### Kim tự tháp kiểm thử (Test Pyramid)

```
        /\        UI / E2E test (Selenium)   → ít, chậm, dễ vỡ
       /  \
      /----\      Service / API test         → vừa
     /      \
    /--------\    Unit test (JUnit)          → nhiều, nhanh, rẻ
```

Selenium nằm ở **đỉnh kim tự tháp**: giá trị cao vì kiểm luồng thật của người dùng, nhưng chậm và dễ "flaky" → chỉ tự động hóa các luồng quan trọng.

## 1.2. Selenium là gì?

Selenium là bộ công cụ mã nguồn mở để **tự động hóa trình duyệt**. Gồm:

| Thành phần | Vai trò |
|---|---|
| **Selenium WebDriver** | Thư viện điều khiển trình duyệt bằng code (Java, Python, C#, JS…) – *lab này dùng* |
| Selenium IDE | Extension ghi/phát lại thao tác (record & playback) |
| Selenium Grid | Chạy test song song trên nhiều máy / trình duyệt |

### Kiến trúc WebDriver (Selenium 4)

```
 Test code (Java)
      │  gọi API: driver.findElement(...).click()
      ▼
 Selenium Java client  ──HTTP (W3C WebDriver Protocol, JSON)──►  ChromeDriver.exe
                                                                       │
                                                                       ▼
                                                                Trình duyệt Chrome
```

- Code Java **không** điều khiển Chrome trực tiếp mà gửi lệnh HTTP theo chuẩn **W3C WebDriver** tới **driver** (chromedriver, geckodriver, msedgedriver).
- Driver chuyển lệnh thành thao tác thật trên trình duyệt và trả kết quả về.
- Vì vậy **phiên bản driver phải khớp phiên bản trình duyệt** → đó là lý do dùng **WebDriverManager** (hoặc Selenium Manager có sẵn từ Selenium 4.6).

## 1.3. Các thành phần của một test script Selenium

### a) `WebDriver` – đại diện cho trình duyệt

```java
WebDriver driver = new ChromeDriver();
driver.get("https://...");          // mở URL
driver.getTitle();                  // lấy tiêu đề trang
driver.getCurrentUrl();             // lấy URL hiện tại
driver.navigate().back();           // back / forward / refresh
driver.manage().window().maximize();
driver.quit();                      // đóng TẤT CẢ cửa sổ + kết thúc session
driver.close();                     // chỉ đóng cửa sổ hiện tại
```

> **`quit()` vs `close()`**: luôn dùng `quit()` ở cuối test, nếu không tiến trình `chromedriver` sẽ còn treo trong máy.

### b) `WebElement` – đại diện một phần tử HTML

```java
WebElement el = driver.findElement(By.id("username"));   // không thấy → NoSuchElementException
List<WebElement> list = driver.findElements(By.tagName("a")); // không thấy → list rỗng
el.sendKeys("tomsmith");   el.clear();   el.click();   el.submit();
el.getText();  el.getAttribute("value");  el.isDisplayed();  el.isEnabled();  el.isSelected();
```

### c) Locator – cách tìm phần tử (`By`)

| Locator | Ví dụ | Ghi chú |
|---|---|---|
| `By.id` | `By.id("username")` | **Ưu tiên số 1** – nhanh, duy nhất |
| `By.name` | `By.name("password")` | Tốt cho form |
| `By.cssSelector` | `By.cssSelector("button[type='submit']")` | **Ưu tiên số 2** – linh hoạt, nhanh |
| `By.xpath` | `By.xpath("//button[@type='submit']")` | Mạnh nhất (tìm theo text, đi ngược lên cha) nhưng dễ vỡ nếu viết tuyệt đối |
| `By.className` | `By.className("flash")` | Chỉ 1 class, không có dấu cách |
| `By.linkText` / `partialLinkText` | `By.linkText("Logout")` | Chỉ cho thẻ `<a>` |
| `By.tagName` | `By.tagName("h2")` | Hay dùng với `findElements` |

**Cheat-sheet CSS Selector:**

| Mẫu | Ý nghĩa |
|---|---|
| `#username` | id = username |
| `.flash.success` | có cả 2 class `flash` **và** `success` |
| `input[name='password']` | thẻ input có thuộc tính name = password |
| `a[href^='/log']` / `[href$='out']` / `[href*='og']` | bắt đầu / kết thúc / chứa |
| `form > button` | button là con trực tiếp của form |
| `#checkboxes input:nth-of-type(2)` | input thứ 2 |

**Cheat-sheet XPath:**

| Mẫu | Ý nghĩa |
|---|---|
| `//input[@id='username']` | theo thuộc tính |
| `//button[contains(.,'Login')]` | theo text chứa "Login" |
| `//h2[text()='Login Page']` | text chính xác |
| `//label[@for='username']/following-sibling::input` | anh em phía sau |
| `//i[@class='fa fa-2x fa-sign-in']/parent::button` | đi lên phần tử cha |

> ❌ Tránh XPath tuyệt đối kiểu `/html/body/div[2]/div/div/form/button` – chỉ cần sửa layout là vỡ.
> ✅ Thứ tự ưu tiên: `id` → `name` → `data-testid`/thuộc tính ổn định → CSS → XPath tương đối.

### d) Waits – chờ phần tử

Web hiện đại tải bất đồng bộ (AJAX, animation). Nếu tìm phần tử khi nó chưa xuất hiện → `NoSuchElementException`. Có 3 loại wait:

| Loại | Cách dùng | Đặc điểm |
|---|---|---|
| **Implicit Wait** | `driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10))` | Toàn cục, áp cho mọi `findElement`; chỉ chờ phần tử *tồn tại* |
| **Explicit Wait** ✅ | `new WebDriverWait(driver, Duration.ofSeconds(10)).until(ExpectedConditions...)` | Chờ **điều kiện cụ thể** (hiển thị, click được, có text…). **Nên dùng** |
| **Fluent Wait** | `new FluentWait<>(driver).withTimeout(..).pollingEvery(..).ignoring(..)` | Explicit wait tùy biến chu kỳ polling, bỏ qua exception |

> ⚠️ **Không trộn Implicit và Explicit wait** – thời gian chờ có thể cộng dồn khó lường (Selenium docs khuyến cáo).
> ⚠️ **Không dùng `Thread.sleep()`** – chậm (luôn chờ đủ thời gian) và vẫn flaky (máy chậm thì không đủ).

`ExpectedConditions` thường dùng:

```java
visibilityOfElementLocated(By)      // phần tử có trong DOM và hiển thị
elementToBeClickable(By)            // hiển thị và enabled
presenceOfElementLocated(By)        // có trong DOM (có thể ẩn)
invisibilityOfElementLocated(By)    // biến mất (vd: loading spinner)
textToBePresentInElementLocated(By, "text")
urlContains("/secure")   titleIs("...")
alertIsPresent()         frameToBeAvailableAndSwitchToIt(By)
numberOfWindowsToBe(2)
```

### e) Assertion – kiểm tra kết quả (JUnit 5)

```java
assertTrue(cond);  assertFalse(cond);  assertEquals(expected, actual);
assertNotNull(obj);  assertThrows(Exception.class, () -> ...);
assertAll("login", () -> assertTrue(...), () -> assertEquals(...)); // gộp nhiều assert
```

> Mọi assert đều nên có **message** để log dễ đọc: `assertTrue(cond, "Không thấy thông báo thành công")`.

## 1.4. JUnit 5 – các annotation cần biết

| Annotation | Ý nghĩa |
|---|---|
| `@Test` | Đánh dấu 1 test method |
| `@BeforeAll` / `@AfterAll` | Chạy **1 lần** trước/sau toàn bộ class (method phải `static`) |
| `@BeforeEach` / `@AfterEach` | Chạy trước/sau **mỗi** test |
| `@DisplayName("...")` | Tên hiển thị dễ đọc |
| `@TestMethodOrder(OrderAnnotation.class)` + `@Order(n)` | Ấn định thứ tự chạy |
| `@ParameterizedTest` | Test chạy nhiều lần với nhiều bộ dữ liệu |
| `@CsvSource` / `@CsvFileSource` / `@ValueSource` / `@MethodSource` | Nguồn dữ liệu cho test tham số |
| `@Disabled("lý do")` | Tạm bỏ qua test |
| `@Tag("smoke")` | Gắn nhãn để lọc khi chạy |
| `@ExtendWith(...)` | Gắn extension (vd: chụp màn hình khi fail) |

Vòng đời khi chạy 1 class:

```
@BeforeAll
   ├─ @BeforeEach → @Test #1 → @AfterEach
   ├─ @BeforeEach → @Test #2 → @AfterEach
   └─ ...
@AfterAll
```

> Với kế thừa: `@BeforeAll` của **lớp cha chạy trước** lớp con, `@AfterAll` của lớp cha chạy **sau** lớp con → đây là cơ sở để thiết kế `BaseTest` ở TODO 6.

## 1.5. Data-Driven Testing (DDT)

Tách **dữ liệu** khỏi **logic test**: 1 method test + N bộ dữ liệu = N test case.

| Nguồn | Khi dùng |
|---|---|
| `@ValueSource(strings = {...})` | 1 tham số đơn giản |
| `@CsvSource({...})` | Vài bộ dữ liệu nhỏ, viết ngay trong code |
| `@CsvFileSource(resources="/x.csv")` | Dữ liệu nhiều, tester/BA có thể tự sửa file |
| `@MethodSource("provider")` | Dữ liệu phức tạp, sinh bằng code, object |

Quy tắc chuỗi trong `@CsvSource`: `''` = chuỗi rỗng, ô trống hoàn toàn = `null`, dấu `'` dùng để bọc giá trị có dấu phẩy.

## 1.6. Page Object Model (POM)

**Vấn đề:** Nếu 50 test đều viết `driver.findElement(By.id("username"))`, khi dev đổi id → phải sửa 50 chỗ.

**Giải pháp POM:** mỗi trang web = 1 class chứa:
- **Locators** (private) – "ở đâu".
- **Actions** (public) – "làm gì": `login(user, pass)`, `logout()`.

Test class chỉ gọi action và assert → đọc như kịch bản nghiệp vụ.

```
 LoginTest  ──gọi──►  LoginPage  ──kế thừa──►  BasePage (wait, click, type, getText)
     │                    │
     └─ kế thừa BaseTest  └─ dùng driver từ DriverFactory
```

| Lớp | Trách nhiệm |
|---|---|
| `DriverFactory` | Tạo & cấu hình WebDriver (browser, options, headless) |
| `BasePage` | Các thao tác dùng chung, an toàn (có wait sẵn) |
| `XxxPage` | Locator + action của 1 trang |
| `BaseTest` | Vòng đời driver (khởi tạo / quit) |
| `XxxTest` | Chỉ chứa kịch bản và assertion |

**Nguyên tắc vàng:**
1. Test class **không** gọi `driver.findElement`.
2. Page class **không** chứa assertion (trả dữ liệu để test tự assert).
3. Action chuyển trang nên **trả về Page Object của trang mới** (fluent).
4. Không `Thread.sleep()`.

## 1.7. ChromeOptions thường dùng

```java
ChromeOptions o = new ChromeOptions();
o.addArguments("--incognito");            // ẩn danh, không cache/cookie cũ
o.addArguments("--headless=new");         // không mở cửa sổ – chạy nền, nhanh hơn
o.addArguments("--window-size=1920,1080"); // headless nên set kích thước
o.addArguments("--disable-notifications");
o.addArguments("--lang=en-US");
Map<String,Object> prefs = new HashMap<>();
prefs.put("profile.managed_default_content_settings.javascript", 2); // 2 = chặn JS
o.setExperimentalOption("prefs", prefs);
o.setPageLoadStrategy(PageLoadStrategy.NORMAL); // NORMAL | EAGER | NONE
```

> Lab gốc **chặn JavaScript** để minh họa prefs. Trang login của the-internet là form HTML thuần nên vẫn chạy. Nhưng các trang dùng JS (alert, dynamic loading…) sẽ **không hoạt động** khi chặn JS → ở Phần 4 ta làm `DriverFactory` cấu hình được.

## 1.8. Các Exception hay gặp & cách xử lý

| Exception | Nguyên nhân | Cách xử lý |
|---|---|---|
| `NoSuchElementException` | Locator sai / phần tử chưa xuất hiện / nằm trong iframe | Kiểm tra locator bằng DevTools (`Ctrl+F` trong tab Elements), thêm explicit wait, switch frame |
| `TimeoutException` | Hết thời gian chờ điều kiện | Kiểm tra điều kiện có đúng không, tăng timeout hợp lý |
| `StaleElementReferenceException` | Trang reload/DOM render lại, tham chiếu cũ không còn | Tìm lại phần tử sau khi trang thay đổi; lưu `By` thay vì `WebElement` |
| `ElementClickInterceptedException` | Phần tử bị che (popup, banner) | Chờ `elementToBeClickable`, đóng popup, scroll tới |
| `ElementNotInteractableException` | Phần tử ẩn / disabled | Chờ visible, kiểm tra đúng phần tử |
| `SessionNotCreatedException` | Driver không khớp version Chrome | Dùng WebDriverManager / Selenium Manager, cập nhật Chrome |
| `NoSuchFrameException`, `NoAlertPresentException` | Chưa có frame/alert | Dùng `ExpectedConditions.frameToBe.../alertIsPresent` |

---

# PHẦN 2 – KIẾN THỨC NÂNG CAO

## 2.1. Selenium Manager vs WebDriverManager

Từ **Selenium 4.6+**, Selenium có sẵn **Selenium Manager**: chỉ cần `new ChromeDriver()` là tự tải driver phù hợp, không cần thư viện ngoài.

```java
WebDriver driver = new ChromeDriver(); // Selenium >= 4.6: tự tìm/tải chromedriver
```

WebDriverManager (bonigarcia) vẫn hữu ích khi: cần chọn browser theo tên chuỗi (`WebDriverManager.getInstance("firefox")`), chạy trong Docker (`.browserInDocker()`), hoặc cấu hình proxy/cache nâng cao. Lab dùng WebDriverManager – cả hai đều đúng.

## 2.2. Cấu hình Maven đúng chuẩn để chạy `mvn test`

Lỗi kinh điển: chạy trong IntelliJ thì pass, nhưng `mvn test` báo **"Tests run: 0"**. Nguyên nhân: Maven dùng `maven-surefire-plugin` cũ không nhận JUnit 5. Cách khắc phục – khai báo surefire ≥ 3.x:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
        </plugin>
    </plugins>
</build>
```

Thêm `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>` để không lỗi font tiếng Việt trong comment/DisplayName.

Lệnh hữu ích:

```bash
mvn clean test                                  # chạy tất cả
mvn test -Dtest=LoginTest                       # 1 class
mvn test -Dtest=LoginTest#testLoginSuccess      # 1 method
mvn test -Dgroups=smoke                         # chỉ test có @Tag("smoke")
mvn test -Dbrowser=firefox -Dheadless=true      # truyền tham số cấu hình (xem 2.5)
```

## 2.3. Fluent Wait & custom ExpectedCondition

```java
Wait<WebDriver> fluent = new FluentWait<>(driver)
        .withTimeout(Duration.ofSeconds(15))
        .pollingEvery(Duration.ofMillis(500))
        .ignoring(NoSuchElementException.class)
        .ignoring(StaleElementReferenceException.class);

WebElement finish = fluent.until(d -> {
    WebElement el = d.findElement(By.id("finish"));
    return el.isDisplayed() ? el : null;   // trả null/false = chưa đạt, chờ tiếp
});
```

Bất kỳ lambda `Function<WebDriver, T>` nào cũng có thể dùng làm điều kiện chờ – rất hữu ích khi `ExpectedConditions` không có sẵn điều kiện bạn cần.

## 2.4. PageFactory & `@FindBy` (cách viết POM khác)

```java
public class LoginPageFactory {
    @FindBy(id = "username") private WebElement username;
    @FindBy(id = "password") private WebElement password;
    @FindBy(css = "button[type='submit']") private WebElement loginBtn;

    public LoginPageFactory(WebDriver driver) {
        PageFactory.initElements(driver, this);   // khởi tạo proxy lazy cho các field
    }
    public void login(String u, String p) { username.sendKeys(u); password.sendKeys(p); loginBtn.click(); }
}
```

| `By` locator (lab dùng) | `@FindBy` PageFactory |
|---|---|
| Tường minh, dễ kết hợp với `WebDriverWait` | Code gọn hơn |
| Không bị `StaleElementReference` vì luôn tìm lại | Có thể bị stale với trang render lại nhiều |
| Được Selenium team khuyên dùng hơn | Vẫn phổ biến trong các dự án cũ |

## 2.5. DriverFactory đa trình duyệt, headless, cấu hình qua tham số

Đọc cấu hình từ System property (truyền qua `-D...`) với giá trị mặc định:

```java
String browser  = System.getProperty("browser", "chrome");
boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
```

Có thể dùng file `src/test/resources/config.properties` cho `baseUrl`, `timeout`… (xem TODO 9).

## 2.6. Chụp màn hình khi test fail (JUnit 5 Extension)

Test UI fail mà không có ảnh thì rất khó điều tra. JUnit 5 cho phép viết extension `TestWatcher`:

```java
File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
Files.copy(src.toPath(), Path.of("target/screenshots/" + name + ".png"));
```

Triển khai đầy đủ ở TODO 8.

## 2.7. Các tương tác nâng cao

```java
// 1. Dropdown <select>
Select select = new Select(driver.findElement(By.id("dropdown")));
select.selectByVisibleText("Option 1");   // hoặc selectByValue("1"), selectByIndex(1)
select.getFirstSelectedOption().getText();

// 2. Alert / Confirm / Prompt (JavaScript)
Alert alert = wait.until(ExpectedConditions.alertIsPresent());
alert.getText();  alert.accept();  alert.dismiss();  alert.sendKeys("abc");

// 3. iFrame
driver.switchTo().frame("frame-top");     // theo name/id, index, hoặc WebElement
driver.switchTo().parentFrame();          // lên 1 cấp
driver.switchTo().defaultContent();       // về trang gốc

// 4. Nhiều cửa sổ / tab
String main = driver.getWindowHandle();
for (String h : driver.getWindowHandles()) if (!h.equals(main)) driver.switchTo().window(h);
driver.switchTo().newWindow(WindowType.TAB); // Selenium 4: mở tab mới

// 5. Actions – hover, kéo thả, double/right click, phím tắt
new Actions(driver).moveToElement(el).perform();
new Actions(driver).dragAndDrop(src, target).perform();
new Actions(driver).keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();

// 6. JavascriptExecutor – scroll, click khi bị che, đọc giá trị
JavascriptExecutor js = (JavascriptExecutor) driver;
js.executeScript("arguments[0].scrollIntoView(true);", el);
js.executeScript("return document.title;");

// 7. Upload file – sendKeys đường dẫn tuyệt đối vào <input type="file">
driver.findElement(By.id("file-upload")).sendKeys(Path.of("src/test/resources/a.txt").toAbsolutePath().toString());

// 8. Cookie
driver.manage().getCookies();  driver.manage().deleteAllCookies();

// 9. Relative Locators (Selenium 4)
import static org.openqa.selenium.support.locators.RelativeLocator.with;
driver.findElement(with(By.tagName("input")).below(By.id("username")));
```

## 2.8. Chạy song song & ThreadLocal driver

Khi bật chạy song song, các test **không được dùng chung 1 `static WebDriver`**. Giải pháp: `ThreadLocal<WebDriver>` – mỗi luồng 1 driver riêng.

```java
public class DriverManager {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();
    public static WebDriver get() { return DRIVER.get(); }
    public static void set(WebDriver d) { DRIVER.set(d); }
    public static void quit() { if (DRIVER.get() != null) { DRIVER.get().quit(); DRIVER.remove(); } }
}
```

`src/test/resources/junit-platform.properties`:

```properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=concurrent
```

(cấu hình trên: các **class** chạy song song, method trong 1 class chạy tuần tự – an toàn với `@Order`).

## 2.9. Viết test ổn định (chống flaky)

1. Mỗi test **độc lập**: tự `navigate()` tới trang cần test, không phụ thuộc test trước.
2. Luôn **wait điều kiện**, không sleep.
3. Locator ổn định (id, `data-testid`), tránh XPath tuyệt đối / index.
4. Assert **nội dung có ý nghĩa** (text, URL) thay vì chỉ "không exception".
5. Dọn trạng thái: `deleteAllCookies()` hoặc driver mới cho mỗi test khi cần.
6. Ảnh chụp + log khi fail.
7. Không hard-code dữ liệu môi trường (URL, tài khoản) rải rác – gom vào config.

## 2.10. Thiết kế test case cho form Login (kỹ thuật test)

Áp dụng **Equivalence Partitioning** và **Boundary Value Analysis**:

| # | Username | Password | Kỳ vọng | Kỹ thuật |
|---|---|---|---|---|
| 1 | tomsmith | SuperSecretPassword! | Login thành công | Lớp hợp lệ |
| 2 | wronguser | SuperSecretPassword! | "Your username is invalid!" | Username không hợp lệ |
| 3 | tomsmith | wrongpassword | "Your password is invalid!" | Password không hợp lệ |
| 4 | (rỗng) | (rỗng) | "Your username is invalid!" | Trường trống |
| 5 | TOMSMITH | SuperSecretPassword! | Lỗi (phân biệt hoa thường) | Case sensitivity |
| 6 | ` tomsmith ` | SuperSecretPassword! | Kiểm tra có trim không | Khoảng trắng |
| 7 | `' OR '1'='1` | `' OR '1'='1` | Lỗi, không đăng nhập được | Security – SQL Injection |
| 8 | `<script>alert(1)</script>` | abc | Lỗi, không thực thi script | Security – XSS |
| 9 | chuỗi 256 ký tự | abc | Lỗi, không crash | Boundary |

> Ưu tiên **negative test**: lỗi thực tế thường nằm ở dữ liệu sai, không phải dữ liệu đúng.

## 2.11. Chạy toàn bộ test bằng 1 lệnh & báo cáo HTML

Không cần server CI: dùng Maven chạy toàn bộ test ở chế độ headless, sinh báo cáo HTML bằng `maven-surefire-report-plugin` và gói vào file `run-tests.bat` – xem TODO 11.

---

# PHẦN 3 – THỰC HÀNH STEP-BY-STEP (TODO 1 → 6)

## Bước 0 – Chuẩn bị môi trường

1. **JDK 17**: cài Temurin/Oracle JDK 17. Kiểm tra: `java -version` → `17.x`.
2. **Maven**: IntelliJ có Maven tích hợp; nếu muốn chạy terminal: `mvn -v`.
3. **IntelliJ IDEA** (Community là đủ).
4. **Google Chrome** bản mới nhất.
5. (Tùy chọn) Plugin **CSV Editor**: `File → Settings → Plugins → "CSV Editor" → Install → Restart IDE`.
6. **Git** + tài khoản GitHub: `git --version`.
7. Mở thử https://the-internet.herokuapp.com/login, nhấn **F12** → tab **Elements** → quan sát:
   - `<input type="text" name="username" id="username">`
   - `<input type="password" name="password" id="password">`
   - `<button class="radius" type="submit">`
   - Sau khi login: `<div id="flash" class="flash success">You logged into a secure area! ×</div>`

> 💡 Mẹo kiểm tra locator: trong tab Elements nhấn `Ctrl+F`, gõ `#username` hoặc `//button[@type='submit']` – nếu hiện "1 of 1" là locator đúng và duy nhất.

---

## TODO 1 – Tạo project Maven `Exercise1_SeleniumBasic`

### Bước 1.1 – Tạo project

1. IntelliJ → `File → New → Project`.
2. Chọn **Maven** (hoặc *New Project* → Build system: **Maven**), JDK: **17**.
3. Name: `Exercise1_SeleniumBasic`, GroupId: tùy (vd `edu.fpt.swt301`), ArtifactId: `SeleniumDemo`.
4. Bỏ chọn "Add sample code" → **Create**.

### Bước 1.2 – Cấu hình `pom.xml`

Thay toàn bộ `pom.xml` (đã gộp sẵn `junit-jupiter-params` cho TODO 3 và surefire để chạy được `mvn test`):

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>edu.fpt.swt301</groupId>
    <artifactId>SeleniumDemo</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <selenium.version>4.21.0</selenium.version>
        <junit.version>5.10.2</junit.version>
    </properties>

    <dependencies>
        <!-- Selenium Java -->
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>${selenium.version}</version>
        </dependency>

        <!-- JUnit 5 engine -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-engine</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- JUnit 5 Parameterized Test (dùng từ TODO 3) -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-params</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- WebDriverManager: tự tải driver đúng version -->
        <dependency>
            <groupId>io.github.bonigarcia</groupId>
            <artifactId>webdrivermanager</artifactId>
            <version>6.1.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Bắt buộc để "mvn test" nhận diện JUnit 5 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
        </plugins>
    </build>
</project>
```

> Nếu muốn đúng y lab gốc ở TODO 1, có thể thêm `junit-jupiter-params` sau ở TODO 3 – kết quả như nhau.

### Bước 1.3 – Tải dependency

- Click biểu tượng **Maven** (chữ *m* nhấp nháy góc phải) hoặc chuột phải `pom.xml → Maven → Reload Project`.
- Kiểm tra: mở cửa sổ Maven → `Dependencies` thấy `selenium-java`, `junit-jupiter-engine`, `junit-jupiter-params`, `webdrivermanager`.
- Terminal: `mvn clean compile` → **BUILD SUCCESS**.

### Bước 1.4 – Khởi tạo Git & commit

```bash
git init
```

Tạo file `.gitignore`:

```gitignore
target/
.idea/
*.iml
out/
```

```bash
git add .
git commit -m "chore: init maven project with selenium, junit5 and webdrivermanager"
```

**✅ Checklist TODO 1**
- [ ] `mvn clean compile` thành công
- [ ] Có đủ dependency selenium-java, junit-jupiter-engine, webdrivermanager
- [ ] JDK 17 (`File → Project Structure → Project SDK`)

---

## TODO 2 – `LoginTest.java` cơ bản (2 test)

### Bước 2.1 – Tạo file

Chuột phải `src/test/java` → `New → Java Class` → `LoginTest`.

### Bước 2.2 – Hiểu luồng test trước khi code

```
@BeforeAll : setup driver (1 lần) ──► Test 1: mở trang → nhập đúng → submit → chờ .flash.success → assert
                                   └► Test 2: mở trang → nhập sai  → submit → chờ .flash.error   → assert
@AfterAll  : driver.quit()
```

### Bước 2.3 – Code

```java
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login Tests for the-internet.herokuapp.com")
public class LoginTest {
    static WebDriver driver;
    static WebDriverWait wait;

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();          // (1) tự tải chromedriver khớp Chrome

        ChromeOptions options = new ChromeOptions();       // (2) cấu hình trình duyệt
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.javascript", 2); // chặn JS
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--incognito");

        driver = new ChromeDriver(options);                // (3) mở Chrome
        wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // (4) explicit wait tối đa 10s
        driver.manage().window().maximize();
    }

    @Test
    @Order(1)
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        driver.get("https://the-internet.herokuapp.com/login");

        driver.findElement(By.id("username")).sendKeys("tomsmith");
        driver.findElement(By.id("password")).sendKeys("SuperSecretPassword!");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement successMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.success")));

        assertTrue(successMsg.getText().contains("You logged into a secure area!"),
                "Không thấy thông báo đăng nhập thành công");
    }

    @Test
    @Order(2)
    @DisplayName("Should display error when logging in with invalid credentials")
    void testLoginFail() {
        driver.get("https://the-internet.herokuapp.com/login");

        driver.findElement(By.id("username")).sendKeys("invalid");
        driver.findElement(By.id("password")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement errorMsg = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.error")));

        assertTrue(errorMsg.getText().contains("Your username is invalid!"),
                "Không thấy thông báo lỗi username");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }
}
```

**Giải thích các điểm quan trọng:**
- `static` cho `driver`/`wait` vì `@BeforeAll`/`@AfterAll` là static method.
- Test 1 login xong đã ở trang `/secure`; Test 2 gọi lại `driver.get(".../login")` nên vẫn độc lập.
- Thông báo thực tế có ký tự `×` ở cuối (nút đóng) → dùng `contains()` thay vì `equals()`.

### Bước 2.4 – Chạy test

- Bấm ▶ xanh cạnh tên class → **Run 'LoginTest'**. Chrome sẽ mở, tự gõ, tự đóng.
- Hoặc terminal: `mvn test -Dtest=LoginTest`.
- Kết quả mong đợi: `Tests run: 2, Failures: 0`.

### Bước 2.5 – Thử làm test fail (để hiểu báo cáo)

Đổi `"You logged into a secure area!"` thành `"Welcome"` → chạy lại → quan sát `AssertionFailedError` cùng message. Sau đó đổi lại.

**Commit:**
```bash
git add .
git commit -m "test: add basic login success and failure tests for the-internet.herokuapp.com"
```

**✅ Checklist TODO 2**
- [ ] Có `@TestMethodOrder`, `@Order`, `@DisplayName`
- [ ] Dùng `WebDriverWait` + `ExpectedConditions`, không `Thread.sleep()`
- [ ] 2 test pass, `driver.quit()` trong `@AfterAll`

---

## TODO 3 – `@ParameterizedTest` + `@CsvSource`

### Bước 3.1 – Dependency

Đã có `junit-jupiter-params` nếu bạn dùng `pom.xml` ở Bước 1.2. Nếu chưa, thêm vào rồi Reload Maven.

### Bước 3.2 – Import

```java
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
```

### Bước 3.3 – Thêm test vào `LoginTest`

```java
@Order(3)
@ParameterizedTest(name = "[{index}] user=''{0}'' pass=''{1}'' → {2}")
@CsvSource({
        "tomsmith,  SuperSecretPassword!, success",   // valid
        "wronguser, SuperSecretPassword!, error",     // invalid username
        "tomsmith,  wrongpassword,        error",     // invalid password
        "'',        '',                   error"      // empty credentials
})
@DisplayName("Multiple login attempts using @CsvSource")
void testLoginWithMultipleParameters(String username, String password, String expectedResult) {
    driver.get("https://the-internet.herokuapp.com/login");

    driver.findElement(By.id("username")).sendKeys(username);
    driver.findElement(By.id("password")).sendKeys(password);
    driver.findElement(By.cssSelector("button[type='submit']")).click();

    By messageLocator = expectedResult.equals("success")
            ? By.cssSelector(".flash.success")
            : By.cssSelector(".flash.error");

    WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(messageLocator));

    if (expectedResult.equals("success")) {
        assertTrue(message.getText().contains("You logged into a secure area!"));
    } else {
        assertTrue(message.getText().toLowerCase().contains("invalid"));
    }
}
```

**Giải thích:**
- `@CsvSource` tự **trim** khoảng trắng quanh giá trị không có dấu nháy → có thể căn cột cho dễ đọc.
- `''` là chuỗi rỗng; `{0}`, `{1}`, `{2}` là tham số; `{index}` là số thứ tự lần chạy.
- Trong `name`, muốn in dấu `'` phải viết `''`.

### Bước 3.4 – Chạy & quan sát

Cửa sổ Run hiển thị 1 node `Multiple login attempts using @CsvSource` với **4 node con** – mỗi bộ dữ liệu là 1 test.

> 🧪 Thử thêm: thêm 2 dòng `"TOMSMITH, SuperSecretPassword!, error"` và `"' OR '1'='1', ' OR '1'='1', error"` (xem bảng 2.10).

**Commit:**
```bash
git commit -am "test: add parameterized login test with @CsvSource for multiple credentials"
```

---

## TODO 4 – Đọc dữ liệu từ file CSV

### Bước 4.1 – Tạo thư mục resources

Chuột phải `src/test` → `New → Directory` → chọn gợi ý **`resources`** (IntelliJ tự đánh dấu *Test Resources Root* – thư mục có biểu tượng màu vàng-xanh). Nếu chưa được đánh dấu: chuột phải → `Mark Directory as → Test Resources Root`.

### Bước 4.2 – Tạo `src/test/resources/login-data.csv`

```csv
username,password,expectedResult
tomsmith,SuperSecretPassword!,success
wronguser,SuperSecretPassword!,error
tomsmith,wrongpassword,error
"","",error
```

> Ô rỗng có thể viết `""` hoặc để trống. Nếu để trống hoàn toàn, JUnit truyền **`null`** → phải xử lý null trong code (bên dưới). Không để dấu cách thừa nếu không muốn trim.

### Bước 4.3 – Thêm test

```java
import org.junit.jupiter.params.provider.CsvFileSource;

@Order(4)
@ParameterizedTest(name = "CSV [{index}]: {0} / {1} → {2}")
@CsvFileSource(resources = "/login-data.csv", numLinesToSkip = 1)
@DisplayName("Login with data from external CSV file")
void testLoginWithCSV(String username, String password, String expectedResult) {
    driver.get("https://the-internet.herokuapp.com/login");

    // null → "" và trim() để tránh lỗi khoảng trắng khi copy/paste CSV
    username = (username == null) ? "" : username.trim();
    password = (password == null) ? "" : password.trim();

    driver.findElement(By.id("username")).sendKeys(username);
    driver.findElement(By.id("password")).sendKeys(password);
    driver.findElement(By.cssSelector("button[type='submit']")).click();

    By messageLocator = expectedResult.equals("success")
            ? By.cssSelector(".flash.success")
            : By.cssSelector(".flash.error");

    WebElement message = wait.until(ExpectedConditions.visibilityOfElementLocated(messageLocator));

    if (expectedResult.equals("success")) {
        assertTrue(message.getText().contains("You logged into a secure area!"));
    } else {
        assertTrue(message.getText().toLowerCase().contains("invalid"));
    }
}
```

**Giải thích:**
- `resources = "/login-data.csv"` – dấu `/` nghĩa là gốc classpath (`src/test/resources`).
- `numLinesToSkip = 1` bỏ dòng header.
- `sendKeys(null)` sẽ ném `IllegalArgumentException` → đó là lý do phải đổi `null` thành `""`.

**Lỗi hay gặp:**
| Lỗi | Nguyên nhân |
|---|---|
| `PreconditionViolationException: Classpath resource [/login-data.csv] does not exist` | File không nằm trong `src/test/resources` hoặc sai tên/hoa thường |
| `ParameterResolutionException` | Số cột CSV ≠ số tham số method |
| `IllegalArgumentException: Keys to send should be a not null CharSequence` | Quên xử lý `null` |

**Commit:**
```bash
git add .
git commit -m "test: add data-driven login test reading from external csv file"
```

---

## TODO 5 – Tái cấu trúc theo Page Object Model (`Exercise3_POM`)

### Bước 5.1 – Tạo project mới từ project cũ

1. Đóng project trong IntelliJ.
2. Copy thư mục `Exercise1_SeleniumBasic` → đổi tên `Exercise3_POM` (bỏ thư mục `.idea`, `target` nếu muốn sạch).
3. Mở `Exercise3_POM` trong IntelliJ, sửa `<artifactId>` trong `pom.xml` (vd `Exercise3_POM`), Reload Maven.

### Bước 5.2 – Tạo package

Chuột phải `src/test/java` → `New → Package` lần lượt: `pages`, `tests`, `utils`. Kéo `LoginTest.java` vào `tests` (IntelliJ tự thêm `package tests;`).

### Bước 5.3 – `utils/DriverFactory.java`

Gom toàn bộ phần cấu hình trình duyệt vào 1 chỗ:

```java
package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class DriverFactory {
    public static WebDriver createDriver() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.managed_default_content_settings.javascript", 2);
        options.setExperimentalOption("prefs", prefs);
        options.addArguments("--incognito");

        return new ChromeDriver(options);
    }
}
```

### Bước 5.4 – `pages/LoginPage.java`

```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage {
    private final WebDriver driver;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    // ===== Locators (private – test không được biết) =====
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton   = By.cssSelector("button[type='submit']");
    private final By successMsg    = By.cssSelector(".flash.success");
    private final By errorMsg      = By.cssSelector(".flash.error");

    // ===== Actions =====
    public void navigate() {
        driver.get("https://the-internet.herokuapp.com/login");
    }

    public void login(String username, String password) {
        driver.findElement(usernameField).sendKeys(username);
        driver.findElement(passwordField).sendKeys(password);
        driver.findElement(loginButton).click();
    }

    public By getSuccessLocator() { return successMsg; }
    public By getErrorLocator()   { return errorMsg; }
}
```

### Bước 5.5 – `tests/LoginTest.java` dùng Page Object

```java
package tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import pages.LoginPage;
import utils.DriverFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login Tests using Page Object Model")
public class LoginTest {
    static WebDriver driver;
    static WebDriverWait wait;
    static LoginPage loginPage;

    @BeforeAll
    static void setUp() {
        driver = DriverFactory.createDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();
        loginPage = new LoginPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        loginPage.navigate();
        loginPage.login("tomsmith", "SuperSecretPassword!");
        WebElement success = wait.until(ExpectedConditions.visibilityOfElementLocated(loginPage.getSuccessLocator()));
        assertTrue(success.getText().contains("You logged into a secure area!"));
    }

    @Test
    @Order(2)
    @DisplayName("Should show error for invalid credentials")
    void testLoginFail() {
        loginPage.navigate();
        loginPage.login("wronguser", "wrongpassword");
        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(loginPage.getErrorLocator()));
        assertTrue(error.getText().toLowerCase().contains("invalid"));
    }

    @ParameterizedTest(name = "CSV Inline: {0} / {1}")
    @Order(3)
    @CsvSource({
            "tomsmith, SuperSecretPassword!, success",
            "wronguser, SuperSecretPassword!, error",
            "tomsmith, wrongpassword, error",
            "'', '', error"
    })
    void testLoginCsvInline(String username, String password, String expected) {
        verifyLogin(username, password, expected);
    }

    @ParameterizedTest(name = "CSV File: {0} / {1}")
    @Order(4)
    @CsvFileSource(resources = "/login-data.csv", numLinesToSkip = 1)
    void testLoginFromCSV(String username, String password, String expected) {
        verifyLogin(username, password, expected);
    }

    // Helper dùng chung cho 2 test tham số – tránh lặp code (DRY)
    private void verifyLogin(String username, String password, String expected) {
        loginPage.navigate();
        username = (username == null) ? "" : username.trim();
        password = (password == null) ? "" : password.trim();

        loginPage.login(username, password);
        By resultLocator = expected.equals("success") ? loginPage.getSuccessLocator() : loginPage.getErrorLocator();
        WebElement result = wait.until(ExpectedConditions.visibilityOfElementLocated(resultLocator));

        if (expected.equals("success")) {
            assertTrue(result.getText().contains("You logged into a secure area!"));
        } else {
            assertTrue(result.getText().toLowerCase().contains("invalid"));
        }
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }
}
```

> So với lab gốc, mình tách phần lặp của Order 3 & 4 thành `verifyLogin()` – hành vi giống hệt, code ngắn hơn. Bạn có thể giữ nguyên bản gốc nếu giảng viên yêu cầu.

### Bước 5.6 – Chạy & commit từng file

Chạy `LoginTest` → cả 4 nhóm test pass (tổng 2 + 4 + 4 = **10** lần chạy).

```bash
git add src/test/java/utils/DriverFactory.java
git commit -m "feat: add DriverFactory for shared webdriver configuration"
git add src/test/java/pages/LoginPage.java
git commit -m "feat: add LoginPage page object with locators and actions"
git add .
git commit -m "refactor: migrate login tests to page object model"
```

**✅ Checklist TODO 5**
- [ ] Đúng cấu trúc `pages/`, `tests/`, `utils/`
- [ ] `LoginTest` không còn `driver.findElement`
- [ ] Locator `private` trong `LoginPage`
- [ ] Order 1→4 đều pass

---

## TODO 6 – `BasePage` và `BaseTest` (`Exercise4_BasePage`)

### Bước 6.1 – Copy project

Copy `Exercise3_POM` → `Exercise4_BasePage`, sửa `artifactId`, Reload Maven.

### Bước 6.2 – `pages/BasePage.java`

Đây là nơi "đóng gói" wait: mọi thao tác đều tự chờ phần tử hiển thị trước.

```java
package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected WebElement waitForVisibility(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        waitForVisibility(locator).click();
    }

    protected void type(By locator, String text) {
        WebElement element = waitForVisibility(locator);
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitForVisibility(locator).getText();
    }

    public void navigateTo(String url) {
        driver.get(url);
    }

    protected boolean isElementVisible(By locator) {
        try {
            return waitForVisibility(locator).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }
}
```

> **Vì sao `protected`?** Chỉ các Page con được dùng các thao tác cấp thấp; test class chỉ thấy action nghiệp vụ (`login`, `navigate`).
> **Cải tiến tùy chọn:** `click()` nên dùng `ExpectedConditions.elementToBeClickable(locator)` thay vì visibility – chính xác hơn với nút bị disable.

### Bước 6.3 – `pages/LoginPage.java` kế thừa `BasePage`

```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton   = By.cssSelector("button[type='submit']");
    private final By successMsg    = By.cssSelector(".flash.success");
    private final By errorMsg      = By.cssSelector(".flash.error");

    public void navigate() {
        navigateTo("https://the-internet.herokuapp.com/login");
    }

    public void login(String username, String password) {
        type(usernameField, username);
        type(passwordField, password);
        click(loginButton);
    }

    public By getSuccessLocator() { return successMsg; }
    public By getErrorLocator()   { return errorMsg; }

    public String getMessageText(By locator) {
        return getText(locator);
    }
}
```

### Bước 6.4 – `tests/BaseTest.java` (abstract)

```java
package tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import utils.DriverFactory;

public abstract class BaseTest {
    protected static WebDriver driver;

    @BeforeAll
    public static void setUpBase() {
        driver = DriverFactory.createDriver();
        driver.manage().window().maximize();
    }

    @AfterAll
    public static void tearDownBase() {
        if (driver != null) {
            driver.quit();
        }
    }
}
```

> `abstract` để JUnit/IDE không chạy nhầm `BaseTest` như một test class.

### Bước 6.5 – `tests/LoginTest.java` kế thừa `BaseTest`

```java
package tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import pages.LoginPage;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Login Tests using Page Object Model + BasePage/BaseTest")
public class LoginTest extends BaseTest {
    static LoginPage loginPage;

    @BeforeAll
    static void initPage() {               // chạy SAU BaseTest.setUpBase() → driver đã sẵn sàng
        loginPage = new LoginPage(driver);
    }

    @Test
    @Order(1)
    @DisplayName("Should login successfully with valid credentials")
    void testLoginSuccess() {
        loginPage.navigate();
        loginPage.login("tomsmith", "SuperSecretPassword!");
        assertTrue(loginPage.getMessageText(loginPage.getSuccessLocator())
                .contains("You logged into a secure area!"));
    }

    @Test
    @Order(2)
    @DisplayName("Should show error for invalid credentials")
    void testLoginFail() {
        loginPage.navigate();
        loginPage.login("wronguser", "wrongpassword");
        assertTrue(loginPage.getMessageText(loginPage.getErrorLocator())
                .toLowerCase().contains("invalid"));
    }

    @ParameterizedTest(name = "CSV Inline: {0} / {1}")
    @Order(3)
    @CsvSource({
            "tomsmith, SuperSecretPassword!, success",
            "wronguser, SuperSecretPassword!, error",
            "tomsmith, wrongpassword, error",
            "'', '', error"
    })
    void testLoginCsvInline(String username, String password, String expected) {
        verifyLogin(username, password, expected);
    }

    @ParameterizedTest(name = "CSV File: {0} / {1}")
    @Order(4)
    @CsvFileSource(resources = "/login-data.csv", numLinesToSkip = 1)
    void testLoginFromCSV(String username, String password, String expected) {
        verifyLogin(username, password, expected);
    }

    private void verifyLogin(String username, String password, String expected) {
        loginPage.navigate();
        username = (username == null) ? "" : username.trim();
        password = (password == null) ? "" : password.trim();
        loginPage.login(username, password);

        if (expected.equals("success")) {
            assertTrue(loginPage.getMessageText(loginPage.getSuccessLocator())
                    .contains("You logged into a secure area!"));
        } else {
            assertTrue(loginPage.getMessageText(loginPage.getErrorLocator())
                    .toLowerCase().contains("invalid"));
        }
    }
    // Không cần @AfterAll – BaseTest.tearDownBase() đã quit driver
}
```

> Ở bản này test class **không còn `WebDriverWait`** – việc chờ đã được `BasePage.getText()` lo. Đây là lợi ích thực sự của BasePage. (Lab gốc vẫn giữ `wait` trong test – cũng chấp nhận được.)

### Bước 6.6 – Chạy & commit

```bash
git add src/test/java/pages/BasePage.java
git commit -m "feat: add BasePage with reusable safe element interactions"
git add src/test/java/pages/LoginPage.java
git commit -m "refactor: make LoginPage extend BasePage"
git add src/test/java/tests/BaseTest.java
git commit -m "feat: add abstract BaseTest managing webdriver lifecycle"
git add src/test/java/tests/LoginTest.java
git commit -m "refactor: make LoginTest extend BaseTest"
```

### Bước 6.7 – Đẩy lên GitHub

1. Tạo repo trống trên GitHub (không tick README).
2. ```bash
   git branch -M main
   git remote add origin https://github.com/<tai-khoan>/<ten-repo>.git
   git push -u origin main
   ```
3. Kiểm tra `git log --oneline` – các commit đúng chuẩn Conventional Commits.

**✅ Checklist TODO 6**
- [ ] `BasePage` có: waitForVisibility, click, type, getText, navigateTo, isElementVisible
- [ ] `LoginPage extends BasePage`, không tự tạo wait riêng
- [ ] `BaseTest` là `abstract`, quản lý vòng đời driver
- [ ] `LoginTest extends BaseTest`, tất cả test pass

---

# PHẦN 4 – BÀI TẬP MỞ RỘNG (NÂNG CAO)

> Làm trên project `Exercise4_BasePage` (hoặc copy thành `Exercise5_Advanced`).
> Cấu trúc đích:

```
src/test/java
├── pages
│   ├── BasePage.java
│   ├── LoginPage.java
│   ├── SecureAreaPage.java        # TODO 7
│   ├── DropdownPage.java          # TODO 10
│   ├── CheckboxesPage.java        # TODO 10
│   ├── JsAlertsPage.java          # TODO 10
│   └── DynamicLoadingPage.java    # TODO 10
├── tests
│   ├── BaseTest.java
│   ├── LoginTest.java
│   ├── LoginFlowTest.java         # TODO 7
│   └── WidgetsTest.java           # TODO 10
└── utils
    ├── DriverFactory.java         # TODO 9 (nâng cấp)
    ├── ConfigReader.java          # TODO 9
    └── ScreenshotExtension.java   # TODO 8
src/test/resources
├── login-data.csv
└── config.properties              # TODO 9
run-tests.bat                      # TODO 11 (gốc project, cạnh pom.xml)
```

---

## TODO 7 – Fluent Page Object: Login → Secure Area → Logout

**Mục tiêu:** action chuyển trang trả về Page Object của trang đích; test đọc như kịch bản.

### Bước 7.1 – `pages/SecureAreaPage.java`

```java
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SecureAreaPage extends BasePage {
    private final By heading      = By.cssSelector("h2");
    private final By flashMessage = By.id("flash");
    private final By logoutButton = By.cssSelector("a[href='/logout']");

    public SecureAreaPage(WebDriver driver) {
        super(driver);
        wait.until(ExpectedConditions.urlContains("/secure")); // xác nhận đã vào đúng trang
    }

    public String getHeading()      { return getText(heading); }
    public String getFlashMessage() { return getText(flashMessage); }

    public LoginPage logout() {
        click(logoutButton);
        return new LoginPage(driver);
    }
}
```

### Bước 7.2 – Bổ sung vào `LoginPage`

```java
private final By flashMessage = By.id("flash");

/** Đăng nhập với tài khoản hợp lệ, trả về trang Secure Area */
public SecureAreaPage loginAs(String username, String password) {
    login(username, password);
    return new SecureAreaPage(driver);
}

public String getFlashMessage() { return getText(flashMessage); }

public boolean isAt() {
    return driver.getCurrentUrl().endsWith("/login");
}
```

### Bước 7.3 – `tests/LoginFlowTest.java`

```java
package tests;

import org.junit.jupiter.api.*;
import pages.LoginPage;
import pages.SecureAreaPage;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("End-to-end login → logout flow")
class LoginFlowTest extends BaseTest {

    @Test
    @Tag("smoke")
    @DisplayName("User can login and then logout")
    void loginThenLogout() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.navigate();

        SecureAreaPage secure = loginPage.loginAs("tomsmith", "SuperSecretPassword!");
        assertAll("Secure area",
                () -> assertEquals("Secure Area", secure.getHeading()),
                () -> assertTrue(secure.getFlashMessage().contains("You logged into a secure area!")));

        LoginPage afterLogout = secure.logout();
        assertAll("After logout",
                () -> assertTrue(afterLogout.isAt(), "Không quay về trang /login"),
                () -> assertTrue(afterLogout.getFlashMessage().contains("You logged out of the secure area!")));
    }

    @Test
    @DisplayName("Secure page cannot be accessed without login")
    void secureRequiresLogin() {
        driver.manage().deleteAllCookies();
        driver.get("https://the-internet.herokuapp.com/secure");
        LoginPage loginPage = new LoginPage(driver);
        assertTrue(loginPage.getFlashMessage().contains("You must login to view the secure area!"));
    }
}
```

> Kiểm tra lại text thực tế trên trang bằng DevTools trước khi chạy; nếu website thay đổi nội dung, chỉnh lại chuỗi expected.

**Commit:**
```
feat: add SecureAreaPage with logout action
test: add end-to-end login logout flow test
```

---

## TODO 8 – Chụp màn hình tự động khi test fail

### Bước 8.1 – Cho `BaseTest` một getter

```java
public static WebDriver getDriver() { return driver; }
```

### Bước 8.2 – `utils/ScreenshotExtension.java`

```java
package utils;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import tests.BaseTest;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotExtension implements TestWatcher {

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        WebDriver driver = BaseTest.getDriver();
        if (driver == null) return;
        try {
            Path dir = Paths.get("target", "screenshots");
            Files.createDirectories(dir);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String name = (context.getRequiredTestClass().getSimpleName() + "_"
                    + context.getDisplayName()).replaceAll("[^a-zA-Z0-9._-]", "_");
            Path file = dir.resolve(name + "_" + time + ".png");

            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.write(file, png);
            System.out.println("📸 Screenshot saved: " + file.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Cannot save screenshot: " + e.getMessage());
        }
    }
}
```

### Bước 8.3 – Gắn extension vào `BaseTest`

```java
import org.junit.jupiter.api.extension.ExtendWith;
import utils.ScreenshotExtension;

@ExtendWith(ScreenshotExtension.class)
public abstract class BaseTest { ... }
```

> `testFailed` được gọi **trước** `@AfterAll`, nên driver vẫn còn sống để chụp.

### Bước 8.4 – Kiểm chứng

Cố ý sửa expected trong 1 test thành sai → chạy → mở `target/screenshots/` thấy file `.png`. Sửa lại.

**Commit:** `feat: capture screenshot automatically on test failure`

---

## TODO 9 – Cấu hình ngoài: đa trình duyệt, headless, bật/tắt JS

### Bước 9.1 – `src/test/resources/config.properties`

```properties
baseUrl=https://the-internet.herokuapp.com
browser=chrome
headless=false
javascriptEnabled=true
timeoutSeconds=10
```

### Bước 9.2 – `utils/ConfigReader.java`

```java
package utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigReader {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConfigReader.class.getResourceAsStream("/config.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Cannot load config.properties", e);
        }
    }

    private ConfigReader() {}

    /** Ưu tiên: -Dkey=value trên command line > config.properties > giá trị mặc định */
    public static String get(String key, String defaultValue) {
        return System.getProperty(key, PROPS.getProperty(key, defaultValue));
    }

    public static boolean getBool(String key, boolean def) { return Boolean.parseBoolean(get(key, String.valueOf(def))); }
    public static int getInt(String key, int def)          { return Integer.parseInt(get(key, String.valueOf(def))); }
}
```

### Bước 9.3 – Nâng cấp `utils/DriverFactory.java`

```java
package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.Map;

public class DriverFactory {

    public static WebDriver createDriver() {
        String browser   = ConfigReader.get("browser", "chrome").toLowerCase();
        boolean headless = ConfigReader.getBool("headless", false);
        boolean jsOn     = ConfigReader.getBool("javascriptEnabled", true);

        switch (browser) {
            case "firefox": {
                WebDriverManager.firefoxdriver().setup();
                FirefoxOptions o = new FirefoxOptions();
                if (headless) o.addArguments("-headless");
                if (!jsOn) o.addPreference("javascript.enabled", false);
                return new FirefoxDriver(o);
            }
            case "edge": {
                WebDriverManager.edgedriver().setup();
                EdgeOptions o = new EdgeOptions();
                if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
                return new EdgeDriver(o);
            }
            case "chrome":
            default: {
                WebDriverManager.chromedriver().setup();
                ChromeOptions o = new ChromeOptions();
                o.addArguments("--incognito");
                if (headless) o.addArguments("--headless=new", "--window-size=1920,1080");
                if (!jsOn) {
                    Map<String, Object> prefs = new HashMap<>();
                    prefs.put("profile.managed_default_content_settings.javascript", 2);
                    o.setExperimentalOption("prefs", prefs);
                }
                return new ChromeDriver(o);
            }
        }
    }
}
```

Thay URL hard-code trong các Page bằng `ConfigReader.get("baseUrl", "https://the-internet.herokuapp.com") + "/login"`, và timeout trong `BasePage` bằng `ConfigReader.getInt("timeoutSeconds", 10)`.

### Bước 9.4 – Chạy thử

```bash
mvn test                                   # theo config.properties
mvn test -Dheadless=true                   # không mở cửa sổ
mvn test -Dbrowser=firefox                 # cần cài Firefox
mvn test -DjavascriptEnabled=false -Dtest=LoginTest   # tái hiện cấu hình lab gốc
```

> Trong IntelliJ: `Run → Edit Configurations → VM options: -Dheadless=true`.

**Commit:**
```
feat: add ConfigReader for external test configuration
refactor: support multi-browser and headless mode in DriverFactory
```

---

## TODO 10 – Kiểm thử các widget phổ biến

> ⚠️ Các trang dưới đây cần **bật JavaScript** (`javascriptEnabled=true`).

### Bước 10.1 – Page Objects

```java
// pages/DropdownPage.java
package pages;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

public class DropdownPage extends BasePage {
    private final By dropdown = By.id("dropdown");
    public DropdownPage(WebDriver d) { super(d); }
    public DropdownPage open() { navigateTo("https://the-internet.herokuapp.com/dropdown"); return this; }
    public void select(String visibleText) { new Select(waitForVisibility(dropdown)).selectByVisibleText(visibleText); }
    public String getSelected() { return new Select(waitForVisibility(dropdown)).getFirstSelectedOption().getText(); }
}
```

```java
// pages/CheckboxesPage.java
package pages;
import org.openqa.selenium.*;
import java.util.List;

public class CheckboxesPage extends BasePage {
    private final By boxes = By.cssSelector("#checkboxes input[type='checkbox']");
    public CheckboxesPage(WebDriver d) { super(d); }
    public CheckboxesPage open() { navigateTo("https://the-internet.herokuapp.com/checkboxes"); return this; }
    private List<WebElement> all() { waitForVisibility(boxes); return driver.findElements(boxes); }
    public void check(int index)   { WebElement b = all().get(index); if (!b.isSelected()) b.click(); }
    public void uncheck(int index) { WebElement b = all().get(index); if (b.isSelected()) b.click(); }
    public boolean isChecked(int index) { return all().get(index).isSelected(); }
}
```

```java
// pages/JsAlertsPage.java
package pages;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class JsAlertsPage extends BasePage {
    private final By jsAlertBtn   = By.xpath("//button[text()='Click for JS Alert']");
    private final By jsConfirmBtn = By.xpath("//button[text()='Click for JS Confirm']");
    private final By jsPromptBtn  = By.xpath("//button[text()='Click for JS Prompt']");
    private final By result       = By.id("result");

    public JsAlertsPage(WebDriver d) { super(d); }
    public JsAlertsPage open() { navigateTo("https://the-internet.herokuapp.com/javascript_alerts"); return this; }

    private Alert waitAlert() { return wait.until(ExpectedConditions.alertIsPresent()); }

    public void acceptAlert()            { click(jsAlertBtn);   waitAlert().accept(); }
    public void dismissConfirm()         { click(jsConfirmBtn); waitAlert().dismiss(); }
    public void answerPrompt(String txt) { click(jsPromptBtn);  Alert a = waitAlert(); a.sendKeys(txt); a.accept(); }
    public String getResult()            { return getText(result); }
}
```

```java
// pages/DynamicLoadingPage.java
package pages;
import org.openqa.selenium.*;

public class DynamicLoadingPage extends BasePage {
    private final By startBtn = By.cssSelector("#start button");
    private final By finish   = By.cssSelector("#finish h4");
    public DynamicLoadingPage(WebDriver d) { super(d); }
    public DynamicLoadingPage open(int example) {
        navigateTo("https://the-internet.herokuapp.com/dynamic_loading/" + example); return this;
    }
    /** Bấm Start rồi chờ (loading ~5s) đến khi chữ kết quả hiện ra */
    public String startAndGetResult() { click(startBtn); return getText(finish); }
}
```

> Dynamic loading mất khoảng 5 giây – vượt quá implicit wait thông thường nhưng nằm trong explicit wait 10s của `BasePage`. Đây là ví dụ kinh điển cho thấy **vì sao phải dùng explicit wait thay vì `Thread.sleep()`**.

### Bước 10.2 – `tests/WidgetsTest.java`

```java
package tests;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pages.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Common web widgets")
class WidgetsTest extends BaseTest {

    @ParameterizedTest(name = "Select {0}")
    @ValueSource(strings = {"Option 1", "Option 2"})
    void dropdownSelect(String option) {
        DropdownPage page = new DropdownPage(driver).open();
        page.select(option);
        assertEquals(option, page.getSelected());
    }

    @Test
    void checkboxToggle() {
        CheckboxesPage page = new CheckboxesPage(driver).open();
        page.check(0);
        page.uncheck(1);
        assertAll(() -> assertTrue(page.isChecked(0)), () -> assertFalse(page.isChecked(1)));
    }

    @Test
    void jsAlerts() {
        JsAlertsPage page = new JsAlertsPage(driver).open();

        page.acceptAlert();
        assertEquals("You successfully clicked an alert", page.getResult());

        page.dismissConfirm();
        assertEquals("You clicked: Cancel", page.getResult());

        page.answerPrompt("SWT301");
        assertEquals("You entered: SWT301", page.getResult());
    }

    @ParameterizedTest(name = "Dynamic loading example {0}")
    @ValueSource(ints = {1, 2})
    void dynamicLoading(int example) {
        String text = new DynamicLoadingPage(driver).open(example).startAndGetResult();
        assertEquals("Hello World!", text);
    }
}
```

### Bước 10.3 – Tự làm thêm (gợi ý)

| Trang | URL | Kỹ năng |
|---|---|---|
| Nested Frames | `/nested_frames` | `switchTo().frame("frame-top")` → `frame("frame-middle")` → đọc text `MIDDLE` |
| Multiple Windows | `/windows` | Click "Click Here", chuyển window, assert `h3` = "New Window" |
| Hovers | `/hovers` | `Actions.moveToElement` rồi assert caption hiện ra |
| File Upload | `/upload` | `sendKeys` đường dẫn file vào `#file-upload`, click `#file-submit` |
| Add/Remove Elements | `/add_remove_elements/` | Click "Add Element" N lần, đếm `findElements` |

**Commit:**
```
feat: add page objects for dropdown, checkboxes, js alerts and dynamic loading
test: add widget interaction tests
```

---

## TODO 11 – Chạy toàn bộ test bằng 1 lệnh & xuất báo cáo HTML (không cần CI)

**Mục tiêu:** không cần server CI, chỉ dùng Maven trên máy mình để chạy toàn bộ test ở chế độ headless, giữ ảnh chụp lỗi, và sinh **báo cáo HTML** để nộp/chia sẻ.

### Bước 11.1 – Thêm plugin báo cáo vào `pom.xml`

Trong `<build><plugins>` (cạnh `maven-surefire-plugin` đã có), thêm:

```xml
<!-- Sinh báo cáo HTML từ kết quả surefire -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-report-plugin</artifactId>
    <version>3.2.5</version>
</plugin>
```

Reload Maven.

### Bước 11.2 – Chạy thử bằng terminal

```bash
# 1) Chạy toàn bộ test, headless; KHÔNG dừng build khi có test fail (để vẫn sinh được báo cáo)
mvn clean test -Dheadless=true -Dmaven.test.failure.ignore=true

# 2) Sinh báo cáo HTML từ kết quả vừa chạy (không chạy lại test)
mvn surefire-report:report-only -DlinkXRef=false
```

Mở báo cáo:
- `target/reports/surefire.html` (plugin 3.x), hoặc `target/site/surefire-report.html` (plugin bản cũ).
- Nếu báo cáo hiển thị thiếu CSS, chạy thêm: `mvn site -DgenerateReports=false`.

Kết quả thô (XML/TXT) luôn có ở `target/surefire-reports/`; ảnh chụp lỗi ở `target/screenshots/` (TODO 8).

### Bước 11.3 – Gói thành script `run-tests.bat` (Windows)

Tạo file `run-tests.bat` ở gốc project (cạnh `pom.xml`):

```bat
@echo off
chcp 65001 >nul
echo ===== Running Selenium tests (headless) =====
call mvn clean test -Dheadless=true -Dmaven.test.failure.ignore=true %*

echo ===== Generating HTML report =====
call mvn surefire-report:report-only -DlinkXRef=false -q

if exist target\reports\surefire.html (
    start "" target\reports\surefire.html
) else (
    start "" target\site\surefire-report.html
)
if exist target\screenshots (
    echo Screenshots of failed tests: target\screenshots
)
```

> Phải dùng `call mvn ...` trong file `.bat`, nếu không script sẽ dừng sau lệnh mvn đầu tiên.
> `%*` cho phép truyền thêm tham số, ví dụ:

```bat
run-tests.bat -Dtest=LoginTest          :: chỉ chạy LoginTest
run-tests.bat -Dgroups=smoke            :: chỉ chạy test gắn @Tag("smoke")
run-tests.bat -Dbrowser=firefox         :: đổi trình duyệt
```

Trong IntelliJ: mở tab **Terminal** gõ `run-tests.bat`, hoặc double-click file trong Explorer.

### Bước 11.4 – (Tùy chọn) Tạo bộ smoke test nhanh

Gắn `@Tag("smoke")` cho các test quan trọng nhất (vd `LoginFlowTest.loginThenLogout`, `LoginTest.testLoginSuccess`). Trước khi commit/push, chạy nhanh:

```bash
mvn test -Dgroups=smoke -Dheadless=true
```

→ đây chính là "CI thủ công": luôn chạy smoke test trước khi `git push`.

### Bước 11.5 – Commit

Thêm vào `.gitignore` (nếu chưa có) để không đẩy báo cáo/ảnh lên GitHub:

```gitignore
target/
```

```bash
git add pom.xml run-tests.bat
git commit -m "chore: add run-tests script and html test report"
git push
```

**✅ Checklist TODO 11**
- [ ] `run-tests.bat` chạy hết test ở chế độ headless
- [ ] Báo cáo HTML được sinh và tự mở sau khi chạy
- [ ] Có test fail thì vẫn sinh báo cáo + ảnh chụp màn hình
- [ ] `mvn test -Dgroups=smoke` chỉ chạy các test smoke

---

## Phụ lục A – Quy ước commit (Conventional Commits)

Cú pháp: `<type>(scope?): <mô tả ngắn, động từ nguyên mẫu, viết thường, không dấu chấm cuối>`

| Type | Dùng khi |
|---|---|
| `feat` | Thêm class/chức năng mới (Page Object, DriverFactory, BasePage…) |
| `test` | Thêm/sửa test case |
| `refactor` | Tái cấu trúc, không đổi hành vi |
| `chore` | Cấu hình project, pom.xml, dependency |
| `fix` | Sửa bug (locator sai, test flaky…) |
| `docs` | Tài liệu, README |

Luồng commit đầy đủ (TODO 1 → 11):

```
chore: init maven project with selenium, junit5 and webdrivermanager
test: add basic login success and failure tests for the-internet.herokuapp.com
test: add parameterized login test with @CsvSource for multiple credentials
test: add data-driven login test reading from external csv file
feat: add DriverFactory for shared webdriver configuration
feat: add LoginPage page object with locators and actions
refactor: migrate login tests to page object model
feat: add BasePage with reusable safe element interactions
refactor: make LoginPage extend BasePage
feat: add abstract BaseTest managing webdriver lifecycle
refactor: make LoginTest extend BaseTest
feat: add SecureAreaPage with logout action
test: add end-to-end login logout flow test
feat: capture screenshot automatically on test failure
feat: add ConfigReader for external test configuration
refactor: support multi-browser and headless mode in DriverFactory
feat: add page objects for dropdown, checkboxes, js alerts and dynamic loading
test: add widget interaction tests
chore: add run-tests script and html test report
```

## Phụ lục B – Troubleshooting nhanh

| Triệu chứng | Cách xử lý |
|---|---|
| `mvn test` báo `Tests run: 0` | Thêm `maven-surefire-plugin` 3.x (mục 2.2) |
| `SessionNotCreatedException: This version of ChromeDriver only supports Chrome version …` | Cập nhật Chrome; xóa cache `~/.cache/selenium` hoặc `~/.cache/wdm`; dùng bản WebDriverManager/Selenium mới |
| Import đỏ `org.junit.jupiter.params` | Thiếu `junit-jupiter-params` hoặc chưa Reload Maven |
| Chrome mở rồi đóng ngay, test fail ở `findElement` | Thiếu wait, hoặc locator sai – kiểm tra bằng DevTools `Ctrl+F` |
| Trang alert/dynamic không phản hồi | JavaScript đang bị chặn bởi prefs = 2 |
| Không tìm thấy `login-data.csv` | Chưa đánh dấu `resources` là *Test Resources Root*, sai đường dẫn |
| Test chạy riêng thì pass, chạy cả bộ thì fail | Test phụ thuộc trạng thái nhau (cookie, đang ở trang khác) → mỗi test tự `navigate()`, cân nhắc `deleteAllCookies()` |
| Còn nhiều tiến trình `chromedriver.exe` trong Task Manager | Quên `driver.quit()` hoặc test bị dừng giữa chừng – kill thủ công |
| Lỗi font tiếng Việt khi build | Thêm `project.build.sourceEncoding=UTF-8` |

## Phụ lục C – Câu hỏi ôn tập

1. Phân biệt `findElement` và `findElements` khi không tìm thấy phần tử.
2. Vì sao không nên dùng `Thread.sleep()`? Nêu 3 loại wait và khi nào dùng.
3. Vì sao không nên kết hợp implicit wait và explicit wait?
4. `driver.close()` khác `driver.quit()` thế nào?
5. Thứ tự ưu tiên chọn locator? Vì sao XPath tuyệt đối là thói quen xấu?
6. Trong `@CsvSource`, `''` và ô để trống khác nhau thế nào? Ảnh hưởng gì tới `sendKeys`?
7. Nêu 4 nguyên tắc của Page Object Model. Vì sao Page class không nên chứa assertion?
8. Thứ tự chạy `@BeforeAll` giữa `BaseTest` và `LoginTest`?
9. `StaleElementReferenceException` xảy ra khi nào, xử lý ra sao?
10. Muốn chạy test song song thì vì sao không thể dùng `static WebDriver`? Giải pháp?
11. Headless mode là gì? Ưu và nhược điểm khi chạy test ở chế độ headless?
12. Thiết kế thêm 5 negative test case cho form login dựa trên EP/BVA.
