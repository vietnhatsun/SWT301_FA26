# SWT301 – LAB 2 (90 phút) · LỜI GIẢI THAM KHẢO

> Dành cho giảng viên. Lời giải theo **phiên bản không dùng `Clock`** (xem mục 0 của `Lab2.md`). Code cài đặt đầy đủ cả phần bonus (`changePassword`, `requestPasswordReset`, `resetPassword`). Sinh viên làm bản 90 phút chỉ cần phần *core*; các `@Nested` được đánh dấu **[bonus]** dùng để chấm điểm cộng.

## 0. Tóm tắt điều chỉnh so với đề gốc

- `AccountService()` là constructor không tham số, **không có `Clock` / `MutableClock`**. Chỉ giữ 3 hằng `MAX_FAILED_ATTEMPTS = 5`, `PASSWORD_HISTORY_SIZE = 3`, `MIN_AGE = 18`.
- Sai mật khẩu 5 lần liên tiếp thì **khóa không thời hạn** (`Account.isLocked()`). Đang khóa thì mọi lần đăng nhập trả `ACCOUNT_LOCKED` và không tăng bộ đếm.
- Mở khóa: `unlockAccount(username)` (BR-ADM-03, mới) hoặc `resetPassword()` thành công. Cả hai đặt `failedAttempts = 0`.
- Token đặt lại mật khẩu **không hết hạn**, chỉ dùng một lần, yêu cầu mới vô hiệu token cũ. `ResultCode` bỏ `TOKEN_EXPIRED`.
- `Account` bỏ `lockedUntil`, `lastLogin`; thêm `boolean isLocked()`.
- Tuổi: `register()` dùng `LocalDate.now()`. Biên tuổi được kiểm tra chính xác bằng hàm thuần `calculateAge(dob, today)` với ngày cố định; test qua `register()` dùng ngày sinh tương đối so với hôm nay.

## 1. Kết quả mong đợi

| Chỉ số | Yêu cầu bản 90 phút | Lời giải |
|---|---|---|
| Phương thức test | ≥ 20 | **64** (core: 43 · bonus: 21) |
| `@ParameterizedTest` | ≥ 12 | **39** |
| Lượt chạy (invocation) | ≥ 60 | **215** |
| Kết quả | 0 fail | **215/215 pass** |
| Lỗi giả lập | ≥ 3 | **10/10 bị phát hiện** (mục 4) |
| Nguồn dữ liệu | Value / NullAndEmpty / Csv / Method | Đủ cả 4 + `@EnumSource` (bonus) |

Lệnh chạy:
```bash
mvn clean test                          # chạy toàn bộ + sinh báo cáo JaCoCo
mvn -Dtest=AccountServiceTest test      # chạy riêng một lớp
# Báo cáo: target/site/jacoco/index.html
```

## 2. Lịch sử commit mẫu

```
chore: init maven project with junit5, jacoco and provided classes
feat(validator): implement username, email, password, phone and age rules
test(validator): add parameterized EP and BVA tests for AccountValidator
feat(account): add Account entity and SHA-256 salted PasswordHasher
feat(register): implement BR-REG-01..10 with required validation order
test(register): cover BR-REG rules, priority order and age boundary
feat(login): implement BR-LOG rules with lock after 5 failed attempts
feat(service): add disableAccount, unlockAccount, findByUsername and isLocked
test(login): cover decision table, 4/5 failed-attempt boundary and admin unlock
test: add missing tests found by manual mutation testing
docs: add README with run guide, coverage, traceability and checklist
# --- bonus ---
feat(password): implement changePassword and reset flow (BR-CHG, BR-RST)
test(password): cover history of 3, single-use token and unlock on reset
```

## 3. Ma trận truy vết (Business rule → Test)

| BR | Test bảo vệ |
|---|---|
| REG-01 | `Register.register_UsernameNullEmptyBlank…`, `…EmailNullEmptyBlank…`, `…PasswordNullEmptyBlank…`, `invalidRegisterInputs[dob null]`, `register_AgeBoundary[0, 1]` (ngày sinh tương lai) |
| REG-02 | `AccountValidatorTest.Username.*`, `invalidRegisterInputs[username sai]` |
| REG-03 | `register_DuplicateUsernameIgnoreCase_ReturnsDuplicateUsername` |
| REG-04 | `AccountValidatorTest.Email.*`, `invalidRegisterInputs[email sai]` |
| REG-05 | `register_DuplicateEmailIgnoreCase_ReturnsDuplicateEmail` |
| REG-06 | `AccountValidatorTest.Password.*`, `invalidRegisterInputs[mật khẩu yếu / chứa username]` |
| REG-07 | `invalidRegisterInputs[confirm lệch]` |
| REG-08 | `calculateAge_Boundaries` (ngày cố định), `register_AgeBoundary` (ngày tương đối) |
| REG-09 | `AccountValidatorTest.Phone.*`, `register_PhoneNullOrEmpty_Success`, `invalidRegisterInputs[phone …]` |
| REG-10 | `register_ValidData_CreatesActiveAccountWithHashedPassword`, `register_UpperCaseEmail_StoredAsLowerCase`, `register_TwoAccountsSamePassword_HaveDifferentSaltAndHash` |
| Thứ tự REG | 6 dòng "thứ tự ưu tiên" trong `invalidRegisterInputs`, `register_DuplicateUsernameButInvalidEmail_ReturnsInvalidEmailFirst` |
| LOG-01 | `login_UsernameNullEmptyBlank…`, `login_PasswordNullEmptyBlank…` |
| LOG-02 | `login_UsernameIgnoreCase_Success`, `login_PasswordCaseSensitive_ReturnsInvalidCredentials` |
| LOG-03 | `login_UnknownUserAndWrongPassword_ReturnSameCode` |
| LOG-04 | `login_DisabledAccount_ReturnsAccountDisabled` |
| LOG-05 | `login_WrongPasswordLessThan5Times_IncrementsCounter`, `login_WrongPassword5thTime_LocksAccount`, `login_CorrectPasswordAfterNFailures[3, 4, 5, 6]` |
| LOG-06 | `login_WhileLocked_RejectsWithoutIncrement` |
| LOG-08 | `login_CorrectCredentials_Success`, `login_SuccessAfterFailures_ResetsCounter` |
| ADM-01/02 | `Admin.disableAccount_*`, `Admin.findByUsername_BlankOrUnknown_ReturnsEmpty` |
| ADM-03 (mới) | `login_AfterAdminUnlock_CounterRestartsAndCanLogin`, `Admin.unlockAccount_BlankOrUnknown_ReturnsUserNotFound` |
| CHG-01..08 *(bonus)* | `ChangePassword.*` |
| RST-01, 03..06 *(bonus)* | `ResetPassword.*` |

**Bảng quyết định login → test:** R1 `login_UnknownUserAndWrongPassword…` · R2 `login_DisabledAccount…` · R3 `login_WhileLocked…` · R4 `login_WrongPasswordLessThan5Times…` · R5 `login_WrongPassword5thTime…` · R6 `login_CorrectCredentials…`

## 4. Bảng mutation thủ công (đã chạy thực tế)

| # | Lỗi chèn | Test bị fail |
|---|---|---|
| M1 | `>= MAX_FAILED_ATTEMPTS` → `> MAX_FAILED_ATTEMPTS` | `login_WrongPassword5thTime_LocksAccount`, `login_WhileLocked…` (2 lượt), `login_CorrectPasswordAfterNFailures[5]` |
| M2 | Bỏ nhánh `if (account.isLocked())` trong `login()` | `login_WhileLocked…` (2 lượt), `login_CorrectPasswordAfterNFailures[5]`, `[6]` |
| M3 | `Account.unlock()` quên đặt `failedAttempts = 0` | `login_AfterAdminUnlock_CounterRestartsAndCanLogin`, `resetPassword_Success_UnlocksAndTokenSingleUse` |
| M4 | Regex username `{4,19}` → `{4,20}` | `isValidUsername_BoundaryLength[21]`, `isValidUsername_InvalidValues[21×a]` |
| M5 | `EMAIL_MAX_LENGTH` 100 → 101 | `isValidEmail_BoundaryLength[101]` |
| M6 | `PASSWORD_MIN_LENGTH` 8 → 7 | `isValidPassword_BoundaryLength[7]` |
| M7 | `< MIN_AGE` → `<= MIN_AGE` | `register_AgeBoundary[18, 0]`, `[18, -1]` |
| M8 | Bỏ `toLowerCase` khi lưu email | `register_DuplicateEmailIgnoreCase…` (2 lượt), `register_UpperCaseEmail_StoredAsLowerCase` |
| M9 | Bỏ `resetFailedAttempts()` khi login thành công | `login_SuccessAfterFailures_ResetsCounter` |
| M10 *(bonus)* | `PASSWORD_HISTORY_SIZE` 3 → 2 | `changePassword_ReuseWithinLast3_Rejected_ThenAllowedAfterRollingOut` |

## 5. Điểm chấm cần lưu ý

- **Phone:** `isValidPhone(null)` trả `false` (đúng yêu cầu "null trả false"); tính *tùy chọn* xử lý trong `register()` (`null` hoặc `""` bỏ qua, `"   "` là `INVALID_PHONE` vì phone không áp dụng quy ước blank).
- **Thứ tự register:** trùng username/email được kiểm tra **sau cùng**, nên username trùng + email sai trả `INVALID_EMAIL`.
- **Khóa:** sau lần sai thứ 5, `failedAttempts` giữ nguyên 5 và `locked = true` cho tới khi mở khóa. Đang khóa thì nhập đúng hay sai đều không làm tăng bộ đếm.
- **Tuổi:** bài làm dùng ngày cố định (ví dụ `2008-09-28`) để test `register()` là **lỗi thiết kế test**, vì test sẽ hỏng khi ngày hiện tại thay đổi. Trừ điểm B3 nếu gặp.
- **Lịch sử mật khẩu (bonus):** so sánh bằng hash cùng salt của tài khoản (salt cố định cho mỗi tài khoản).
- **Token (bonus):** lỗi mật khẩu mới không xóa token; đặt lại thành công thì xóa token và `unlock()`.

---

## 6. Mã nguồn

### `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>fu.swt301</groupId>
  <artifactId>lab2-account</artifactId>
  <version>1.0.0</version>
  <properties>
    <maven.compiler.release>17</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <junit.version>5.10.2</junit.version>
  </properties>
  <dependencies>
    <dependency>
      <groupId>org.junit.jupiter</groupId>
      <artifactId>junit-jupiter</artifactId>
      <version>${junit.version}</version>
      <scope>test</scope>
    </dependency>
  </dependencies>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.2.5</version>
      </plugin>
      <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.11</version>
        <executions>
          <execution><goals><goal>prepare-agent</goal></goals></execution>
          <execution><id>report</id><phase>test</phase><goals><goal>report</goal></goals></execution>
        </executions>
      </plugin>
    </plugins>
  </build>
</project>
```

## 6.1 Production – `src/main/java/lab2/account`

### `src/main/java/lab2/account/ResultCode.java`

```java
package lab2.account;

/** Mã kết quả trả về của các nghiệp vụ trong AccountService. */
public enum ResultCode {
    SUCCESS,
    INVALID_INPUT,
    // Đăng ký
    INVALID_USERNAME,
    DUPLICATE_USERNAME,
    INVALID_EMAIL,
    DUPLICATE_EMAIL,
    WEAK_PASSWORD,
    PASSWORD_MISMATCH,
    UNDERAGE,
    INVALID_PHONE,
    // Đăng nhập
    INVALID_CREDENTIALS,
    ACCOUNT_LOCKED,
    ACCOUNT_DISABLED,
    // Đổi / đặt lại mật khẩu
    USER_NOT_FOUND,
    OLD_PASSWORD_INCORRECT,
    SAME_AS_OLD_PASSWORD,
    PASSWORD_REUSED,
    INVALID_TOKEN;

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
```

### `src/main/java/lab2/account/AccountStatus.java`

```java
package lab2.account;

/** Trạng thái quản trị của tài khoản (khóa do đăng nhập sai được lưu riêng bằng cờ locked trong Account). */
public enum AccountStatus {
    ACTIVE,
    DISABLED
}
```

### `src/main/java/lab2/account/TokenResult.java`

```java
package lab2.account;

/** Kết quả của yêu cầu quên mật khẩu: mã kết quả + token (null nếu không thành công). */
public record TokenResult(ResultCode code, String token) {
}
```

### `src/main/java/lab2/account/AccountValidator.java`

```java
package lab2.account;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.regex.Pattern;

/** Các hàm kiểm tra dữ liệu thuần túy (static, không trạng thái, không ném exception với null). */
public final class AccountValidator {

    /** BR-REG-02: 5–20 ký tự, bắt đầu bằng chữ cái, chỉ gồm chữ ASCII, số, dấu _. */
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{4,19}$");
    /** BR-REG-04: local@domain.tld, các nhãn domain không rỗng, TLD >= 2 chữ cái. */
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
    /** BR-REG-09: 10 chữ số, đầu số 03/05/07/08/09. */
    private static final Pattern PHONE = Pattern.compile("^0[35789]\\d{8}$");

    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 32;
    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=";

    private AccountValidator() {
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null
                && email.length() <= EMAIL_MAX_LENGTH
                && EMAIL.matcher(email).matches();
    }

    /** BR-REG-06. username null/blank thì bỏ qua điều kiện "không chứa username". */
    public static boolean isValidPassword(String password, String username) {
        if (password == null
                || password.length() < PASSWORD_MIN_LENGTH
                || password.length() > PASSWORD_MAX_LENGTH) {
            return false;
        }
        boolean upper = false, lower = false, digit = false, special = false;
        for (char c : password.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                upper = true;
            } else if (c >= 'a' && c <= 'z') {
                lower = true;
            } else if (c >= '0' && c <= '9') {
                digit = true;
            } else if (SPECIAL_CHARS.indexOf(c) >= 0) {
                special = true;
            } else {
                return false; // khoảng trắng hoặc ký tự ngoài các nhóm cho phép
            }
        }
        if (!(upper && lower && digit && special)) {
            return false;
        }
        if (username != null && !username.isBlank()) {
            return !password.toLowerCase(Locale.ROOT)
                    .contains(username.toLowerCase(Locale.ROOT));
        }
        return true;
    }

    /** Chỉ kiểm tra định dạng; null trả false. Việc phone là tùy chọn do AccountService xử lý. */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE.matcher(phone).matches();
    }

    /** Số tuổi tròn năm tính đến ngày today (sinh nhật hôm nay là đã đủ tuổi). */
    public static int calculateAge(LocalDate dob, LocalDate today) {
        return Period.between(dob, today).getYears();
    }
}
```

### `src/main/java/lab2/account/PasswordHasher.java`

```java
package lab2.account;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

/** Băm mật khẩu SHA-256 + salt. Kết quả là chuỗi hex 64 ký tự. */
public final class PasswordHasher {

    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String generateSalt() {
        byte[] bytes = new byte[SALT_BYTES];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    public static String hash(String salt, String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean matches(String salt, String raw, String expectedHash) {
        if (salt == null || raw == null || expectedHash == null) {
            return false;
        }
        byte[] actual = hash(salt, raw).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(actual, expectedHash.getBytes(StandardCharsets.UTF_8));
    }
}
```

### `src/main/java/lab2/account/Account.java`

```java
package lab2.account;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Thực thể tài khoản. Các hàm thay đổi trạng thái để package-private: chỉ AccountService được gọi. */
public class Account {
    private final String username;
    private final String email;
    private final LocalDate dateOfBirth;
    private final String phone;
    private final String salt;
    private final List<String> passwordHistory = new ArrayList<>(); // phần tử cuối = mật khẩu hiện tại
    private AccountStatus status = AccountStatus.ACTIVE;
    private int failedAttempts;
    private boolean locked;

    Account(String username, String email, LocalDate dateOfBirth, String phone,
            String salt, String passwordHash) {
        this.username = username;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.phone = phone;
        this.salt = salt;
        this.passwordHistory.add(passwordHash);
    }

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getPhone() { return phone; }
    public String getSalt() { return salt; }
    public AccountStatus getStatus() { return status; }
    public int getFailedAttempts() { return failedAttempts; }
    public boolean isLocked() { return locked; }

    public String getCurrentPasswordHash() {
        return passwordHistory.get(passwordHistory.size() - 1);
    }

    public List<String> getPasswordHistory() {
        return List.copyOf(passwordHistory);
    }

    // ----- package-private: thay đổi trạng thái -----
    void setStatus(AccountStatus status) { this.status = status; }
    void incrementFailedAttempts() { failedAttempts++; }
    void resetFailedAttempts() { failedAttempts = 0; }
    void lock() { locked = true; }

    void unlock() {
        locked = false;
        failedAttempts = 0;
    }

    /** Thêm băm mới vào lịch sử, chỉ giữ tối đa maxHistory phần tử (bỏ phần tử cũ nhất). */
    void changePasswordHash(String newHash, int maxHistory) {
        passwordHistory.add(newHash);
        while (passwordHistory.size() > maxHistory) {
            passwordHistory.remove(0);
        }
    }
}
```

### `src/main/java/lab2/account/AccountService.java`

```java
package lab2.account;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Nghiệp vụ quản lý tài khoản, lưu trong bộ nhớ (HashMap), không phụ thuộc thời gian thực trừ ngày hiện tại khi đăng ký. */
public class AccountService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    private final Map<String, Account> accountsByUsername = new HashMap<>(); // key: username lowercase
    private final Map<String, String> usernameByEmail = new HashMap<>();     // email lowercase -> username key
    private final Map<String, String> usernameByToken = new HashMap<>();     // token -> username key
    private final Map<String, String> tokenByUsername = new HashMap<>();     // username key -> token hiện hành

    // ================= Đăng ký =================
    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        LocalDate today = LocalDate.now();
        // BR-REG-01
        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)
                || dateOfBirth == null || dateOfBirth.isAfter(today)) {
            return ResultCode.INVALID_INPUT;
        }
        // BR-REG-02
        if (!AccountValidator.isValidUsername(username)) {
            return ResultCode.INVALID_USERNAME;
        }
        // BR-REG-04
        if (!AccountValidator.isValidEmail(email)) {
            return ResultCode.INVALID_EMAIL;
        }
        // BR-REG-06
        if (!AccountValidator.isValidPassword(password, username)) {
            return ResultCode.WEAK_PASSWORD;
        }
        // BR-REG-07
        if (!password.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }
        // BR-REG-08
        if (AccountValidator.calculateAge(dateOfBirth, today) < MIN_AGE) {
            return ResultCode.UNDERAGE;
        }
        // BR-REG-09 (phone tùy chọn: null hoặc "" được chấp nhận)
        if (phone != null && !phone.isEmpty() && !AccountValidator.isValidPhone(phone)) {
            return ResultCode.INVALID_PHONE;
        }
        String userKey = key(username);
        String emailKey = key(email);
        // BR-REG-03
        if (accountsByUsername.containsKey(userKey)) {
            return ResultCode.DUPLICATE_USERNAME;
        }
        // BR-REG-05
        if (usernameByEmail.containsKey(emailKey)) {
            return ResultCode.DUPLICATE_EMAIL;
        }
        // BR-REG-10
        String salt = PasswordHasher.generateSalt();
        Account account = new Account(username, emailKey, dateOfBirth, phone,
                salt, PasswordHasher.hash(salt, password));
        accountsByUsername.put(userKey, account);
        usernameByEmail.put(emailKey, userKey);
        return ResultCode.SUCCESS;
    }

    // ================= Đăng nhập =================
    public ResultCode login(String username, String password) {
        // BR-LOG-01
        if (isBlank(username) || isBlank(password)) {
            return ResultCode.INVALID_INPUT;
        }
        // BR-LOG-02, 03 (user không tồn tại)
        Account account = accountsByUsername.get(key(username));
        if (account == null) {
            return ResultCode.INVALID_CREDENTIALS;
        }
        // BR-LOG-04
        if (account.getStatus() == AccountStatus.DISABLED) {
            return ResultCode.ACCOUNT_DISABLED;
        }
        // BR-LOG-06: đang khóa -> từ chối, không tăng bộ đếm
        if (account.isLocked()) {
            return ResultCode.ACCOUNT_LOCKED;
        }
        // BR-LOG-03, 05: sai mật khẩu
        if (!PasswordHasher.matches(account.getSalt(), password, account.getCurrentPasswordHash())) {
            account.incrementFailedAttempts();
            if (account.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                account.lock();
                return ResultCode.ACCOUNT_LOCKED;
            }
            return ResultCode.INVALID_CREDENTIALS;
        }
        // BR-LOG-08
        account.resetFailedAttempts();
        return ResultCode.SUCCESS;
    }

    // ================= Đổi mật khẩu (BONUS) =================
    public ResultCode changePassword(String username, String oldPassword,
                                     String newPassword, String confirmPassword) {
        // BR-CHG-01
        if (isBlank(username) || isBlank(oldPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            return ResultCode.INVALID_INPUT;
        }
        // BR-CHG-02
        Account account = accountsByUsername.get(key(username));
        if (account == null) {
            return ResultCode.USER_NOT_FOUND;
        }
        if (account.getStatus() == AccountStatus.DISABLED) {
            return ResultCode.ACCOUNT_DISABLED;
        }
        // BR-CHG-03 (không đụng tới failedAttempts)
        if (!PasswordHasher.matches(account.getSalt(), oldPassword, account.getCurrentPasswordHash())) {
            return ResultCode.OLD_PASSWORD_INCORRECT;
        }
        // BR-CHG-04..07
        ResultCode check = validateNewPassword(account, newPassword, confirmPassword);
        if (!check.isSuccess()) {
            return check;
        }
        // BR-CHG-08
        account.changePasswordHash(PasswordHasher.hash(account.getSalt(), newPassword), PASSWORD_HISTORY_SIZE);
        return ResultCode.SUCCESS;
    }

    // ================= Quên / đặt lại mật khẩu (BONUS) =================
    public TokenResult requestPasswordReset(String email) {
        // BR-RST-01
        if (isBlank(email)) {
            return new TokenResult(ResultCode.INVALID_INPUT, null);
        }
        String userKey = usernameByEmail.get(key(email));
        if (userKey == null) {
            return new TokenResult(ResultCode.USER_NOT_FOUND, null);
        }
        if (accountsByUsername.get(userKey).getStatus() == AccountStatus.DISABLED) {
            return new TokenResult(ResultCode.ACCOUNT_DISABLED, null);
        }
        // BR-RST-03: yêu cầu mới vô hiệu token cũ
        String oldToken = tokenByUsername.remove(userKey);
        if (oldToken != null) {
            usernameByToken.remove(oldToken);
        }
        String token = UUID.randomUUID().toString();
        usernameByToken.put(token, userKey);
        tokenByUsername.put(userKey, token);
        return new TokenResult(ResultCode.SUCCESS, token);
    }

    public ResultCode resetPassword(String token, String newPassword, String confirmPassword) {
        // BR-RST-04: input -> token tồn tại
        if (isBlank(token) || isBlank(newPassword) || isBlank(confirmPassword)) {
            return ResultCode.INVALID_INPUT;
        }
        String userKey = usernameByToken.get(token);
        if (userKey == null) {
            return ResultCode.INVALID_TOKEN;
        }
        Account account = accountsByUsername.get(userKey);
        // BR-RST-05: lỗi mật khẩu mới -> token vẫn còn
        ResultCode check = validateNewPassword(account, newPassword, confirmPassword);
        if (!check.isSuccess()) {
            return check;
        }
        // BR-RST-06
        account.changePasswordHash(PasswordHasher.hash(account.getSalt(), newPassword), PASSWORD_HISTORY_SIZE);
        account.unlock();
        usernameByToken.remove(token);
        tokenByUsername.remove(userKey);
        return ResultCode.SUCCESS;
    }

    // ================= Quản trị & truy vấn =================
    public ResultCode disableAccount(String username) {
        Optional<Account> account = findByUsername(username);
        if (account.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }
        account.get().setStatus(AccountStatus.DISABLED);
        return ResultCode.SUCCESS;
    }

    /** BR-ADM-03: quản trị viên mở khóa tài khoản bị khóa do đăng nhập sai. */
    public ResultCode unlockAccount(String username) {
        Optional<Account> account = findByUsername(username);
        if (account.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }
        account.get().unlock();
        return ResultCode.SUCCESS;
    }

    public Optional<Account> findByUsername(String username) {
        if (isBlank(username)) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByUsername.get(key(username)));
    }

    public boolean isLocked(String username) {
        return findByUsername(username).map(Account::isLocked).orElse(false);
    }

    // ================= Helpers =================
    /** Dùng chung cho changePassword và resetPassword: CHG-04 -> 05 -> 06 -> 07. */
    private ResultCode validateNewPassword(Account account, String newPassword, String confirmPassword) {
        if (!AccountValidator.isValidPassword(newPassword, account.getUsername())) {
            return ResultCode.WEAK_PASSWORD;
        }
        if (!newPassword.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }
        String newHash = PasswordHasher.hash(account.getSalt(), newPassword);
        if (newHash.equals(account.getCurrentPasswordHash())) {
            return ResultCode.SAME_AS_OLD_PASSWORD;
        }
        if (account.getPasswordHistory().contains(newHash)) {
            return ResultCode.PASSWORD_REUSED;
        }
        return ResultCode.SUCCESS;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}
```

## 6.2 Test – `src/test/java/lab2/account`

### `src/test/java/lab2/account/AccountValidatorTest.java`

```java
package lab2.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AccountValidator")
class AccountValidatorTest {

    // ---------- Username (BR-REG-02) ----------
    @Nested
    @DisplayName("isValidUsername")
    class Username {

        @ParameterizedTest(name = "[{index}] \"{0}\" hợp lệ")
        @ValueSource(strings = {"alice", "Alice_01", "Z____", "bob_the_builder", "abcdefghij0123456789"})
        void isValidUsername_ValidValues_ReturnsTrue(String username) {
            assertTrue(AccountValidator.isValidUsername(username));
        }

        @ParameterizedTest(name = "[{index}] \"{0}\" không hợp lệ")
        @ValueSource(strings = {"ab_1", "1alice", "_alice", "ali ce", "alice!", "alice-01", "álice", "aaaaaaaaaaaaaaaaaaaaa"})
        void isValidUsername_InvalidValues_ReturnsFalse(String username) {
            assertFalse(AccountValidator.isValidUsername(username));
        }

        @ParameterizedTest(name = "[{index}] null/rỗng/blank: \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "     "})
        void isValidUsername_NullEmptyBlank_ReturnsFalse(String username) {
            assertFalse(AccountValidator.isValidUsername(username));
        }

        @ParameterizedTest(name = "[{index}] độ dài {0} -> {1}")
        @MethodSource("lab2.account.AccountValidatorTest#usernameLengths")
        void isValidUsername_BoundaryLength(int length, boolean expected) {
            String username = "a".repeat(length);
            assertEquals(expected, AccountValidator.isValidUsername(username));
        }
    }

    static Stream<Arguments> usernameLengths() {
        return Stream.of(
                Arguments.of(4, false), Arguments.of(5, true), Arguments.of(6, true),
                Arguments.of(19, true), Arguments.of(20, true), Arguments.of(21, false));
    }

    // ---------- Email (BR-REG-04) ----------
    @Nested
    @DisplayName("isValidEmail")
    class Email {

        @ParameterizedTest(name = "[{index}] {0} -> {1}")
        @CsvSource({
                "alice@example.com,       true",
                "a.b+tag@mail.fpt.edu.vn, true",
                "ALICE@EXAMPLE.COM,       true",
                "alice@example.c,         false",
                "alice@example,           false",
                "alice.example.com,       false",
                "@example.com,            false",
                "alice@.com,              false",
                "alice@example..com,      false",
                "alice@exa mple.com,      false",
                "alice@example.c0m,       false"
        })
        void isValidEmail_Partitions(String email, boolean expected) {
            assertEquals(expected, AccountValidator.isValidEmail(email));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void isValidEmail_NullEmptyBlank_ReturnsFalse(String email) {
            assertFalse(AccountValidator.isValidEmail(email));
        }

        @ParameterizedTest(name = "[{index}] độ dài {0} -> {1}")
        @CsvSource({"99, true", "100, true", "101, false"})
        void isValidEmail_BoundaryLength(int totalLength, boolean expected) {
            String suffix = "@example.com"; // 12 ký tự
            String email = "a".repeat(totalLength - suffix.length()) + suffix;
            assertEquals(totalLength, email.length());
            assertEquals(expected, AccountValidator.isValidEmail(email));
        }
    }

    // ---------- Password (BR-REG-06) ----------
    @Nested
    @DisplayName("isValidPassword")
    class Password {

        @ParameterizedTest(name = "[{index}] {3}")
        @CsvSource(delimiter = '|', value = {
                "Secret@123   | alice_01 | true  | hợp lệ đủ 4 nhóm",
                "Abcdef1=     | alice_01 | true  | ký tự đặc biệt '='",
                "secret@123   | alice_01 | false | thiếu chữ hoa",
                "SECRET@123   | alice_01 | false | thiếu chữ thường",
                "Secret@abc   | alice_01 | false | thiếu chữ số",
                "Secret1234   | alice_01 | false | thiếu ký tự đặc biệt",
                "'Secret @123'| alice_01 | false | chứa khoảng trắng",
                "Secret@123~  | alice_01 | false | ký tự ngoài tập cho phép",
                "Xalice_01@1  | alice_01 | false | chứa username",
                "XALICE_01@1a | alice_01 | false | chứa username khác hoa/thường",
                "Xalice_01@1  |          | true  | username null -> bỏ qua điều kiện"
        })
        void isValidPassword_Partitions(String password, String username, boolean expected, String desc) {
            assertEquals(expected, AccountValidator.isValidPassword(password, username));
        }

        @ParameterizedTest(name = "[{index}] độ dài {0} -> {1}")
        @CsvSource({"7, false", "8, true", "9, true", "31, true", "32, true", "33, false"})
        void isValidPassword_BoundaryLength(int length, boolean expected) {
            String password = "Aa1!" + "b".repeat(length - 4);
            assertEquals(expected, AccountValidator.isValidPassword(password, null));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"        "})
        void isValidPassword_NullEmptyBlank_ReturnsFalse(String password) {
            assertFalse(AccountValidator.isValidPassword(password, "alice_01"));
        }
    }

    // ---------- Phone (BR-REG-09) ----------
    @Nested
    @DisplayName("isValidPhone")
    class Phone {

        @ParameterizedTest
        @ValueSource(strings = {"0312345678", "0512345678", "0712345678", "0812345678", "0912345678"})
        void isValidPhone_ValidPrefixes_ReturnsTrue(String phone) {
            assertTrue(AccountValidator.isValidPhone(phone));
        }

        @ParameterizedTest
        @ValueSource(strings = {"0112345678", "0412345678", "0612345678", "091234567", "09123456789",
                "091234567a", "+84912345678", "9123456789", " 0912345678"})
        void isValidPhone_InvalidValues_ReturnsFalse(String phone) {
            assertFalse(AccountValidator.isValidPhone(phone));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void isValidPhone_NullOrEmpty_ReturnsFalse(String phone) {
            // validator chỉ kiểm định dạng; tính "tùy chọn" do AccountService xử lý
            assertFalse(AccountValidator.isValidPhone(phone));
        }
    }

    // ---------- Age (BR-REG-08) ----------
    @ParameterizedTest(name = "[{index}] sinh {0}, hôm nay {1} -> {2} tuổi")
    @CsvSource({
            "2008-09-28, 2026-09-28, 18",   // đúng sinh nhật 18
            "2008-09-29, 2026-09-28, 17",   // 18 tuổi trừ 1 ngày
            "2008-09-27, 2026-09-28, 18",
            "2008-02-29, 2026-02-28, 17",   // năm nhuận
            "2008-02-29, 2026-03-01, 18",
            "2026-09-28, 2026-09-28, 0"
    })
    void calculateAge_Boundaries(LocalDate dob, LocalDate today, int expected) {
        assertEquals(expected, AccountValidator.calculateAge(dob, today));
    }
}
```

### `src/test/java/lab2/account/AccountServiceTest.java`

```java
package lab2.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AccountService")
class AccountServiceTest {

    static final String USER = "alice_01";
    static final String EMAIL = "alice@example.com";
    static final String PASS = "Secret@123";
    static final String WRONG = "Wrong@123";
    static final LocalDate DOB = LocalDate.of(2000, 1, 15);
    static final String PHONE = "0912345678";
    static final LocalDate CHILD_DOB = LocalDate.now().minusYears(10);

    AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    /** Arrange dùng chung: đăng ký tài khoản mẫu thành công. */
    void registerDefault() {
        assertEquals(ResultCode.SUCCESS, service.register(USER, EMAIL, PASS, PASS, DOB, PHONE));
    }

    Account account() {
        return service.findByUsername(USER).orElseThrow();
    }

    void failLogin(int times) {
        for (int i = 0; i < times; i++) {
            service.login(USER, WRONG);
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        void register_ValidData_CreatesActiveAccountWithHashedPassword() {
            ResultCode result = service.register(USER, EMAIL, PASS, PASS, DOB, PHONE);

            assertEquals(ResultCode.SUCCESS, result);
            Account acc = account();
            assertEquals(AccountStatus.ACTIVE, acc.getStatus());
            assertEquals(0, acc.getFailedAttempts());
            assertFalse(acc.isLocked());
            assertNotEquals(PASS, acc.getCurrentPasswordHash());
            assertEquals(64, acc.getCurrentPasswordHash().length());
            assertEquals(1, acc.getPasswordHistory().size());
        }

        @Test
        void register_UpperCaseEmail_StoredAsLowerCase() {
            service.register(USER, "Alice@Example.COM", PASS, PASS, DOB, PHONE);
            assertEquals("alice@example.com", account().getEmail());
        }

        @Test
        void register_TwoAccountsSamePassword_HaveDifferentSaltAndHash() {
            registerDefault();
            service.register("bob_02", "bob@example.com", PASS, PASS, DOB, null);
            Account bob = service.findByUsername("bob_02").orElseThrow();
            assertNotEquals(account().getSalt(), bob.getSalt());
            assertNotEquals(account().getCurrentPasswordHash(), bob.getCurrentPasswordHash());
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("lab2.account.AccountServiceTest#invalidRegisterInputs")
        void register_InvalidInput_ReturnsExpectedCode(String desc, String username, String email,
                                                       String password, String confirm, LocalDate dob,
                                                       String phone, ResultCode expected) {
            ResultCode result = service.register(username, email, password, confirm, dob, phone);

            assertEquals(expected, result);
            assertTrue(service.findByUsername(username).isEmpty(), "Không được tạo tài khoản");
        }

        @ParameterizedTest(name = "[{index}] username = \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void register_UsernameNullEmptyBlank_ReturnsInvalidInput(String username) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(username, EMAIL, PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] email = \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void register_EmailNullEmptyBlank_ReturnsInvalidInput(String email) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(USER, email, PASS, PASS, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] password = \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void register_PasswordNullEmptyBlank_ReturnsInvalidInput(String password) {
            assertEquals(ResultCode.INVALID_INPUT, service.register(USER, EMAIL, password, PASS, DOB, PHONE));
            assertEquals(ResultCode.INVALID_INPUT, service.register(USER, EMAIL, PASS, password, DOB, PHONE));
        }

        @ParameterizedTest(name = "[{index}] phone = \"{0}\" được chấp nhận")
        @NullAndEmptySource
        void register_PhoneNullOrEmpty_Success(String phone) {
            assertEquals(ResultCode.SUCCESS, service.register(USER, EMAIL, PASS, PASS, DOB, phone));
        }

        @ParameterizedTest(name = "[{index}] trùng username \"{0}\"")
        @ValueSource(strings = {"alice_01", "ALICE_01", "Alice_01"})
        void register_DuplicateUsernameIgnoreCase_ReturnsDuplicateUsername(String username) {
            registerDefault();
            assertEquals(ResultCode.DUPLICATE_USERNAME,
                    service.register(username, "other@example.com", PASS, PASS, DOB, null));
        }

        @ParameterizedTest(name = "[{index}] trùng email \"{0}\"")
        @ValueSource(strings = {"alice@example.com", "ALICE@EXAMPLE.COM", "Alice@Example.Com"})
        void register_DuplicateEmailIgnoreCase_ReturnsDuplicateEmail(String email) {
            registerDefault();
            assertEquals(ResultCode.DUPLICATE_EMAIL,
                    service.register("bob_02", email, PASS, PASS, DOB, null));
            assertTrue(service.findByUsername("bob_02").isEmpty());
        }

        /** Ngày sinh tính tương đối so với hôm nay: dob = today - {0} năm + {1} ngày. */
        @ParameterizedTest(name = "[{index}] today - {0} năm + {1} ngày -> {2}")
        @CsvSource({
                "18,  0, SUCCESS",       // đúng 18 tuổi hôm nay
                "18,  1, UNDERAGE",      // 18 tuổi trừ 1 ngày
                "18, -1, SUCCESS",       // 18 tuổi + 1 ngày
                "0,   0, UNDERAGE",      // sinh hôm nay
                "0,   1, INVALID_INPUT"  // ngày sinh ở tương lai
        })
        void register_AgeBoundary(int yearsAgo, int plusDays, ResultCode expected) {
            LocalDate dob = LocalDate.now().minusYears(yearsAgo).plusDays(plusDays);
            assertEquals(expected, service.register(USER, EMAIL, PASS, PASS, dob, null));
        }

        @Test
        void register_DuplicateUsernameButInvalidEmail_ReturnsInvalidEmailFirst() {
            // BR-REG-04 đứng trước BR-REG-03
            registerDefault();
            assertEquals(ResultCode.INVALID_EMAIL,
                    service.register(USER, "bad-email", PASS, PASS, DOB, null));
        }
    }

    static Stream<Arguments> invalidRegisterInputs() {
        return Stream.of(
                // từng quy tắc riêng lẻ
                Arguments.of("dob null", USER, EMAIL, PASS, PASS, null, PHONE, ResultCode.INVALID_INPUT),
                Arguments.of("username sai", "1alice", EMAIL, PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
                Arguments.of("email sai", USER, "alice@example", PASS, PASS, DOB, PHONE, ResultCode.INVALID_EMAIL),
                Arguments.of("mật khẩu yếu", USER, EMAIL, "password", "password", DOB, PHONE, ResultCode.WEAK_PASSWORD),
                Arguments.of("mật khẩu chứa username", USER, EMAIL, "Alice_01@x", "Alice_01@x", DOB, PHONE, ResultCode.WEAK_PASSWORD),
                Arguments.of("confirm lệch", USER, EMAIL, PASS, "Secret@124", DOB, PHONE, ResultCode.PASSWORD_MISMATCH),
                Arguments.of("chưa đủ tuổi", USER, EMAIL, PASS, PASS, CHILD_DOB, PHONE, ResultCode.UNDERAGE),
                Arguments.of("phone sai đầu số", USER, EMAIL, PASS, PASS, DOB, "0112345678", ResultCode.INVALID_PHONE),
                Arguments.of("phone blank", USER, EMAIL, PASS, PASS, DOB, "   ", ResultCode.INVALID_PHONE),
                // thứ tự ưu tiên khi vi phạm nhiều quy tắc
                Arguments.of("thiếu email + username sai -> INVALID_INPUT", "1alice", "", PASS, PASS, DOB, PHONE, ResultCode.INVALID_INPUT),
                Arguments.of("username sai + email sai -> INVALID_USERNAME", "1alice", "bad", PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
                Arguments.of("email sai + mk yếu -> INVALID_EMAIL", USER, "bad", "weak", "weak", DOB, PHONE, ResultCode.INVALID_EMAIL),
                Arguments.of("mk yếu + confirm lệch -> WEAK_PASSWORD", USER, EMAIL, "weak", "other", DOB, PHONE, ResultCode.WEAK_PASSWORD),
                Arguments.of("confirm lệch + chưa đủ tuổi -> PASSWORD_MISMATCH", USER, EMAIL, PASS, "x", CHILD_DOB, PHONE, ResultCode.PASSWORD_MISMATCH),
                Arguments.of("chưa đủ tuổi + phone sai -> UNDERAGE", USER, EMAIL, PASS, PASS, CHILD_DOB, "123", ResultCode.UNDERAGE)
        );
    }

    // ======================================================================
    @Nested
    @DisplayName("login()")
    class Login {

        @BeforeEach
        void registerUser() {
            registerDefault();
        }

        @Test
        void login_CorrectCredentials_Success() {
            assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
            assertEquals(0, account().getFailedAttempts());
            assertFalse(service.isLocked(USER));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        void login_UsernameNullEmptyBlank_ReturnsInvalidInput(String username) {
            assertEquals(ResultCode.INVALID_INPUT, service.login(username, PASS));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  "})
        void login_PasswordNullEmptyBlank_ReturnsInvalidInput(String password) {
            assertEquals(ResultCode.INVALID_INPUT, service.login(USER, password));
            assertEquals(0, account().getFailedAttempts());
        }

        @ParameterizedTest(name = "[{index}] username \"{0}\"")
        @ValueSource(strings = {"alice_01", "ALICE_01", "Alice_01"})
        void login_UsernameIgnoreCase_Success(String username) {
            assertEquals(ResultCode.SUCCESS, service.login(username, PASS));
        }

        @ParameterizedTest(name = "[{index}] password \"{0}\"")
        @ValueSource(strings = {"secret@123", "SECRET@123"})
        void login_PasswordCaseSensitive_ReturnsInvalidCredentials(String password) {
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, password));
        }

        @Test
        void login_UnknownUserAndWrongPassword_ReturnSameCode() {
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login("nobody_1", PASS));
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, WRONG));
        }

        @ParameterizedTest(name = "[{index}] sai {0} lần -> chưa khóa")
        @ValueSource(ints = {1, 2, 3, 4})
        void login_WrongPasswordLessThan5Times_IncrementsCounter(int times) {
            failLogin(times - 1);

            ResultCode result = service.login(USER, WRONG);

            assertEquals(ResultCode.INVALID_CREDENTIALS, result);
            assertEquals(times, account().getFailedAttempts());
            assertFalse(service.isLocked(USER));
        }

        @Test
        void login_WrongPassword5thTime_LocksAccount() {
            failLogin(4);

            ResultCode result = service.login(USER, WRONG);

            assertEquals(ResultCode.ACCOUNT_LOCKED, result);
            assertEquals(5, account().getFailedAttempts());
            assertTrue(service.isLocked(USER));
        }

        @ParameterizedTest(name = "[{index}] đang khóa + password \"{0}\"")
        @ValueSource(strings = {PASS, WRONG})
        void login_WhileLocked_RejectsWithoutIncrement(String password) {
            failLogin(5);

            assertEquals(ResultCode.ACCOUNT_LOCKED, service.login(USER, password));
            assertEquals(5, account().getFailedAttempts());
            assertTrue(service.isLocked(USER));
        }

        @ParameterizedTest(name = "[{index}] {0} lần sai -> {1}, locked={2}")
        @CsvSource({
                "3, SUCCESS,        false",
                "4, SUCCESS,        false",
                "5, ACCOUNT_LOCKED, true",
                "6, ACCOUNT_LOCKED, true"
        })
        void login_CorrectPasswordAfterNFailures(int failures, ResultCode expected, boolean locked) {
            failLogin(failures);

            assertEquals(expected, service.login(USER, PASS));
            assertEquals(locked, service.isLocked(USER));
        }

        @Test
        void login_AfterAdminUnlock_CounterRestartsAndCanLogin() {
            failLogin(5);
            assertEquals(ResultCode.SUCCESS, service.unlockAccount(USER));

            assertFalse(service.isLocked(USER));
            assertEquals(0, account().getFailedAttempts());
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, WRONG));
            assertEquals(1, account().getFailedAttempts());
            assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
        }

        @Test
        void login_SuccessAfterFailures_ResetsCounter() {
            failLogin(3);
            assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
            assertEquals(0, account().getFailedAttempts());
        }

        @ParameterizedTest(name = "[{index}] DISABLED + password \"{0}\"")
        @ValueSource(strings = {PASS, WRONG})
        void login_DisabledAccount_ReturnsAccountDisabled(String password) {
            service.disableAccount(USER);
            assertEquals(ResultCode.ACCOUNT_DISABLED, service.login(USER, password));
            assertEquals(0, account().getFailedAttempts());
        }
    }

    // ======================================================================
    @Nested
    @DisplayName("Quản trị & truy vấn")
    class Admin {

        @Test
        void disableAccount_ExistingUser_SetsDisabled() {
            registerDefault();
            assertEquals(ResultCode.SUCCESS, service.disableAccount("ALICE_01"));
            assertEquals(AccountStatus.DISABLED, account().getStatus());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "nobody_1"})
        void disableAccount_BlankOrUnknown_ReturnsUserNotFound(String username) {
            assertEquals(ResultCode.USER_NOT_FOUND, service.disableAccount(username));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "nobody_1"})
        void unlockAccount_BlankOrUnknown_ReturnsUserNotFound(String username) {
            assertEquals(ResultCode.USER_NOT_FOUND, service.unlockAccount(username));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "nobody_1"})
        void findByUsername_BlankOrUnknown_ReturnsEmpty(String username) {
            assertTrue(service.findByUsername(username).isEmpty());
            assertFalse(service.isLocked(username));
        }
    }

    // ======================================================================
    // BONUS: đổi mật khẩu
    @Nested
    @DisplayName("changePassword() [bonus]")
    class ChangePassword {

        @BeforeEach
        void registerUser() {
            registerDefault();
        }

        @Test
        void changePassword_Valid_OldPasswordNoLongerWorks() {
            assertEquals(ResultCode.SUCCESS, service.changePassword(USER, PASS, "NewPass@1", "NewPass@1"));
            assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, PASS));
            assertEquals(ResultCode.SUCCESS, service.login(USER, "NewPass@1"));
            assertEquals(2, account().getPasswordHistory().size());
        }

        @ParameterizedTest(name = "[{index}] {4}")
        @CsvSource(delimiter = '|', value = {
                "nobody_1 | Secret@123 | NewPass@1  | NewPass@1  | USER_NOT_FOUND",
                "alice_01 | Wrong@123  | NewPass@1  | NewPass@1  | OLD_PASSWORD_INCORRECT",
                "alice_01 | Secret@123 | weak       | weak       | WEAK_PASSWORD",
                "alice_01 | Secret@123 | NewPass@1  | NewPass@2  | PASSWORD_MISMATCH",
                "alice_01 | Secret@123 | Secret@123 | Secret@123 | SAME_AS_OLD_PASSWORD",
                "alice_01 | Wrong@123  | weak       | x          | OLD_PASSWORD_INCORRECT",
                "alice_01 |            | NewPass@1  | NewPass@1  | INVALID_INPUT"
        })
        void changePassword_Rules(String user, String oldPw, String newPw, String confirm, ResultCode expected) {
            assertEquals(expected, service.changePassword(user, oldPw, newPw, confirm));
        }

        @Test
        void changePassword_WrongOldPassword_DoesNotAffectFailedAttempts() {
            service.changePassword(USER, WRONG, "NewPass@1", "NewPass@1");
            assertEquals(0, account().getFailedAttempts());
        }

        @Test
        void changePassword_DisabledAccount_ReturnsAccountDisabled() {
            service.disableAccount(USER);
            assertEquals(ResultCode.ACCOUNT_DISABLED, service.changePassword(USER, PASS, "NewPass@1", "NewPass@1"));
        }

        @Test
        void changePassword_ReuseWithinLast3_Rejected_ThenAllowedAfterRollingOut() {
            service.changePassword(USER, PASS, "NewPass@1", "NewPass@1");      // history: P, N1
            service.changePassword(USER, "NewPass@1", "NewPass@2", "NewPass@2"); // history: P, N1, N2
            assertEquals(ResultCode.PASSWORD_REUSED, service.changePassword(USER, "NewPass@2", PASS, PASS));
            assertEquals(ResultCode.PASSWORD_REUSED, service.changePassword(USER, "NewPass@2", "NewPass@1", "NewPass@1"));

            service.changePassword(USER, "NewPass@2", "NewPass@3", "NewPass@3"); // history: N1, N2, N3
            assertEquals(3, account().getPasswordHistory().size());
            assertEquals(ResultCode.SUCCESS, service.changePassword(USER, "NewPass@3", PASS, PASS));
        }
    }

    // ======================================================================
    // BONUS: quên / đặt lại mật khẩu
    @Nested
    @DisplayName("requestPasswordReset() / resetPassword() [bonus]")
    class ResetPassword {

        @BeforeEach
        void registerUser() {
            registerDefault();
        }

        String token() {
            TokenResult r = service.requestPasswordReset("ALICE@example.com");
            assertEquals(ResultCode.SUCCESS, r.code());
            assertNotNull(r.token());
            return r.token();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" "})
        void requestPasswordReset_BlankEmail_ReturnsInvalidInput(String email) {
            TokenResult r = service.requestPasswordReset(email);
            assertEquals(ResultCode.INVALID_INPUT, r.code());
            assertNull(r.token());
        }

        @Test
        void requestPasswordReset_UnknownOrDisabled() {
            assertEquals(ResultCode.USER_NOT_FOUND, service.requestPasswordReset("x@example.com").code());
            service.disableAccount(USER);
            assertEquals(ResultCode.ACCOUNT_DISABLED, service.requestPasswordReset(EMAIL).code());
        }

        @Test
        void resetPassword_Success_UnlocksAndTokenSingleUse() {
            failLogin(5);
            String token = token();

            assertEquals(ResultCode.SUCCESS, service.resetPassword(token, "NewPass@1", "NewPass@1"));
            assertFalse(service.isLocked(USER));
            assertEquals(0, account().getFailedAttempts());
            assertEquals(ResultCode.SUCCESS, service.login(USER, "NewPass@1"));
            assertEquals(ResultCode.INVALID_TOKEN, service.resetPassword(token, "NewPass@2", "NewPass@2"));
        }

        @Test
        void resetPassword_NewRequest_InvalidatesOldToken() {
            String first = token();
            String second = token();
            assertEquals(ResultCode.INVALID_TOKEN, service.resetPassword(first, "NewPass@1", "NewPass@1"));
            assertEquals(ResultCode.SUCCESS, service.resetPassword(second, "NewPass@1", "NewPass@1"));
        }

        @Test
        void resetPassword_RejectedPassword_TokenStillValid() {
            String token = token();
            assertEquals(ResultCode.WEAK_PASSWORD, service.resetPassword(token, "weak", "weak"));
            assertEquals(ResultCode.SAME_AS_OLD_PASSWORD, service.resetPassword(token, PASS, PASS));
            assertEquals(ResultCode.SUCCESS, service.resetPassword(token, "NewPass@1", "NewPass@1"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        void resetPassword_BlankToken_ReturnsInvalidInput(String token) {
            assertEquals(ResultCode.INVALID_INPUT, service.resetPassword(token, "NewPass@1", "NewPass@1"));
        }

        @Test
        void resetPassword_UnknownToken_ReturnsInvalidToken() {
            assertEquals(ResultCode.INVALID_TOKEN, service.resetPassword("not-a-token", "NewPass@1", "NewPass@1"));
        }
    }
}
```

### `src/test/java/lab2/account/PasswordHasherTest.java`

```java
package lab2.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PasswordHasherTest {

    @Test
    void hash_SameSaltAndPassword_IsDeterministic() {
        String salt = PasswordHasher.generateSalt();
        assertEquals(PasswordHasher.hash(salt, "Secret@123"), PasswordHasher.hash(salt, "Secret@123"));
    }

    @Test
    void hash_DifferentSalt_ProducesDifferentHash() {
        assertNotEquals(PasswordHasher.hash("salt-1", "Secret@123"), PasswordHasher.hash("salt-2", "Secret@123"));
    }

    @Test
    void generateSalt_TwoCalls_AreDifferent() {
        assertNotEquals(PasswordHasher.generateSalt(), PasswordHasher.generateSalt());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Secret@123", "a", "Mật khẩu có dấu"})
    void hash_IsHex64_AndNeverEqualsRawPassword(String raw) {
        String hash = PasswordHasher.hash("salt", raw);
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
        assertNotEquals(raw, hash);
    }

    @Test
    void matches_CorrectPassword_ReturnsTrue() {
        String hash = PasswordHasher.hash("salt", "Secret@123");
        assertTrue(PasswordHasher.matches("salt", "Secret@123", hash));
    }

    @ParameterizedTest
    @ValueSource(strings = {"secret@123", "SECRET@123", "Secret@1234", "Secret@12"})
    void matches_DifferentOrWrongCase_ReturnsFalse(String attempt) {
        String hash = PasswordHasher.hash("salt", "Secret@123");
        assertFalse(PasswordHasher.matches("salt", attempt, hash));
    }

    @Test
    void matches_NullArguments_ReturnsFalse() {
        assertFalse(PasswordHasher.matches(null, "x", "y"));
        assertFalse(PasswordHasher.matches("s", null, "y"));
        assertFalse(PasswordHasher.matches("s", "x", null));
    }
}
```

### `src/test/java/lab2/account/ResultCodeTest.java`

```java
package lab2.account;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ResultCodeTest {

    @Test
    void isSuccess_Success_ReturnsTrue() {
        assertTrue(ResultCode.SUCCESS.isSuccess());
    }

    @ParameterizedTest(name = "{0} không phải thành công")
    @EnumSource(value = ResultCode.class, names = "SUCCESS", mode = EnumSource.Mode.EXCLUDE)
    void isSuccess_AllOtherCodes_ReturnsFalse(ResultCode code) {
        assertFalse(code.isSuccess());
    }
}
```

---

## 7. README mẫu cho sinh viên nộp

```markdown
# Lab2 – Account Management (JUnit 5)

## Chạy
mvn clean test  → report: target/site/jacoco/index.html

## Kết quả
- Tests: 43 methods (core), 0 failures
- JaCoCo: AccountValidator  Line ..% / Branch ..%
          AccountService    Line ..% / Branch ..%   (ảnh: docs/jacoco.png)

## Mutation thủ công
| # | Lỗi | Test fail | Hoàn tác |
|---|-----|-----------|----------|
| M1 | >= MAX_FAILED_ATTEMPTS -> > | Login.login_WrongPassword5thTime_LocksAccount | ✅ |
| M2 | bỏ kiểm tra isLocked() trong login() | Login.login_WhileLocked_RejectsWithoutIncrement | ✅ |
| M3 | regex username {4,19} -> {4,20} | Username...BoundaryLength[21] | ✅ |

## Ma trận truy vết
(BR → tên test, xem mục 3 lời giải)

## Nguồn tham khảo
JUnit 5 User Guide – Parameterized Tests
```
