# SWT301 – ProgressTest 1: Unit Testing với JUnit 5

> **Chủ đề:** Kiểm thử đơn vị module *Account Management* bằng JUnit 5 và Parameterized Test
> **Thời lượng:** 90 phút trên lớp · **Hình thức:** Cá nhân
> **Công cụ:** JDK 17+, Maven 3.9+, JUnit Jupiter 5.10.2, JaCoCo 0.8.11, Git

---



### 0.1 Bảng quyết định cho `login()` 

| # | User tồn tại | DISABLED | Đang bị khóa | Mật khẩu đúng | Đã sai 4 lần | Kết quả |
|---|---|---|---|---|---|---|
| 1 | N | – | – | – | – | `INVALID_CREDENTIALS` |
| 2 | Y | Y | – | – | – | `ACCOUNT_DISABLED` |
| 3 | Y | N | Y | – | – | `ACCOUNT_LOCKED` (không tăng bộ đếm) |
| 4 | Y | N | N | N | N | `INVALID_CREDENTIALS` (bộ đếm +1) |
| 5 | Y | N | N | N | Y | `ACCOUNT_LOCKED` (khóa) |
| 6 | Y | N | N | Y | – | `SUCCESS` (bộ đếm về 0) |

### 0.3 Phạm vi core / bonus

| Nhóm | Bắt buộc trên lớp (core) | Bonus / về nhà |
|---|---|---|
| Mã cho sẵn | `ResultCode`, `AccountStatus`, `TokenResult` | – |
| `AccountValidator` | Cả 5 hàm: `isValidUsername`, `isValidEmail`, `isValidPassword`, `isValidPhone`, `calculateAge` | – |
| `PasswordHasher` | Cài đặt theo gợi ý (gần như cho sẵn) | `PasswordHasherTest` |
| `Account` | Getter + hàm thay đổi trạng thái package-private | – |
| `AccountService` | `register()`, `login()`, `disableAccount()`, `unlockAccount()`, `findByUsername()`, `isLocked()` | `changePassword()` (BR-CHG), `requestPasswordReset()` / `resetPassword()` (BR-RST): **giữ nguyên stub** `throw new UnsupportedOperationException("TODO")` |
| Test | `AccountValidatorTest`, `AccountServiceTest` (`@Nested Register`, `Login`, `Admin`) | `@Nested ChangePassword`, `ResetPassword`, `ResultCodeTest` (`@EnumSource`), `@CsvFileSource` |
| Chất lượng | JaCoCo + **3** lỗi giả lập | ≥ 5 lỗi giả lập |

**Yêu cầu tối thiểu:**

| Tiêu chí | Bản gốc | **Bản 90 phút** |
|---|---|---|
| Số phương thức test | ≥ 40 | **≥ 20** |
| Số `@ParameterizedTest` | ≥ 20 | **≥ 12** |
| Tổng lượt chạy (invocation) | ≥ 120 | **≥ 60** |
| `@ValueSource` / `@NullAndEmptySource` / `@CsvSource` / `@MethodSource` | 4 / 4 / 4 / 2 | **2 / 3 / 3 / 1** |
| JaCoCo `AccountValidator` + `AccountService` | Line ≥ 90%, Branch ≥ 80% | **Line ≥ 80%, Branch ≥ 70%**  |
| Mutation thủ công | ≥ 5 | **≥ 3** |

---

## 1. Phân bổ thời gian

| Phút | TODO | Sản phẩm | Commit |
|---|---|---|---|
| 0 – 8 | TODO-0: Khởi tạo project, mã cho sẵn | `mvn clean compile` xanh | `chore: ...` |
| 8 – 27 | TODO-1, TODO-2: `AccountValidator` + test | Validator test xanh | `feat(validator)`, `test(validator)` |
| 27 – 34 | TODO-3: `PasswordHasher`, `Account` | Compile xanh | `feat(account)` |
| 34 – 54 | TODO-4, TODO-5: `register()` + test | Register test xanh | `feat(register)`, `test(register)` |
| 54 – 74 | TODO-6, TODO-7: `login()`, admin + test | Login test xanh | `feat(login)`, `test(login)` |
| 74 – 85 | TODO-8: JaCoCo + 3 lỗi giả lập | Ảnh coverage, bảng mutation | `test: ...` |
| 85 – 90 | TODO-9: README, checklist, nộp bài | File `.zip` | `docs: ...` |

> Mẹo: **làm xong phần nào, test và commit phần đó**. 

---

## 2. Quy ước commit message (Conventional Commits)

```
<type>(<scope>): <mô tả ngắn, thể mệnh lệnh, ≤ 72 ký tự>

[body – tùy chọn: vì sao thay đổi, BR nào được cài/test]
```

| type | Dùng khi |
|---|---|
| `chore` | Khởi tạo project, cấu hình build, `.gitignore` |
| `feat` | Thêm/cài đặt mã production |
| `test` | Thêm/sửa mã test |
| `fix` | Sửa lỗi mã production do test phát hiện |
| `refactor` | Đổi cấu trúc mã, không đổi hành vi |
| `docs` | README, checklist, báo cáo |

Scope gợi ý: `validator`, `hasher`, `account`, `service`, `register`, `login`, `coverage`.

✅ Tốt: `feat(register): implement BR-REG-01..10 with validation order`
❌ Tránh: `update`, `fix bug`, `lab2 done`, `commit 1`

---

## 3. TODO chi tiết

### TODO-0 · Khởi tạo project (8 phút)

**Việc cần làm**
- [ ] Tạo project Maven `lab2-account` với `pom.xml` như mục 3.2 của đề gốc (JUnit 5.10.2, surefire 3.2.5, JaCoCo 0.8.11).
- [ ] Tạo package `lab2.account` ở cả `src/main/java` và `src/test/java`.
- [ ] Chép 3 file cho sẵn vào `main`: `ResultCode` (**xóa hằng `TOKEN_EXPIRED`**), `AccountStatus`, `TokenResult`. **Không** cần `MutableClock`.
- [ ] Tạo khung `AccountService` với constructor **không tham số**, 3 hằng số, các phương thức ném `UnsupportedOperationException("TODO")`. Thêm `ResultCode unlockAccount(String username)`.
- [ ] `git init`, tạo `.gitignore`.

**Gợi ý**
```java
public class AccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    public AccountService() { /* TODO: khởi tạo các Map */ }
    public ResultCode unlockAccount(String username) { throw new UnsupportedOperationException("TODO"); }
    // ... register, login, changePassword, requestPasswordReset, resetPassword,
    //     disableAccount, findByUsername, isLocked như mục 5.3
}
```
```gitignore
target/
.idea/
.vscode/
*.iml
```

**Checklist hoàn thành**
- [ ] `mvn clean compile` → BUILD SUCCESS


**Commit**
```
chore: init maven project with junit5, jacoco and provided classes
```

---

### TODO-1 · Cài đặt `AccountValidator` (8 phút)

Lớp `final`, constructor `private`, các hàm `static`, **nhận `null` phải trả `false` và không ném exception**.

**Việc cần làm**
- [ ] `isValidUsername(String)`: BR-REG-02, dài 5–20, bắt đầu bằng chữ cái, chỉ gồm `[A-Za-z0-9_]`.
- [ ] `isValidEmail(String)`: BR-REG-04, dạng `local@domain.tld`, TLD ≥ 2 chữ cái, nhãn domain không rỗng, dài ≤ 100.
- [ ] `isValidPassword(String password, String username)`: BR-REG-06, dài 8–32, đủ 4 nhóm ký tự, chỉ ký tự cho phép, không chứa username (bỏ qua nếu username null/blank).
- [ ] `isValidPhone(String)`: chỉ kiểm tra định dạng `0[35789]` + 8 chữ số; `null` trả `false`. (Việc phone là **tùy chọn** do `register()` xử lý.)
- [ ] `calculateAge(LocalDate dob, LocalDate today)`: số năm tròn. Đây là **hàm thuần**: `today` do bên gọi truyền vào, nên test được mọi biên bằng ngày cố định.

**Gợi ý code**
```java
private static final Pattern USERNAME = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{4,19}$");
private static final Pattern EMAIL =
        Pattern.compile("^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");
private static final Pattern PHONE = Pattern.compile("^0[35789]\\d{8}$");
private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=";

public static boolean isValidUsername(String username) {
    return username != null && USERNAME.matcher(username).matches(); // matches(), KHÔNG dùng find()
}

public static boolean isValidPassword(String password, String username) {
    if (password == null || password.length() < 8 || password.length() > 32) return false;
    boolean upper = false, lower = false, digit = false, special = false;
    for (char c : password.toCharArray()) {
        if (c >= 'A' && c <= 'Z') upper = true;
        else if (c >= 'a' && c <= 'z') lower = true;
        else if (c >= '0' && c <= '9') digit = true;
        else if (SPECIAL_CHARS.indexOf(c) >= 0) special = true;
        else return false;                       // khoảng trắng / ký tự lạ
    }
    // TODO: kiểm tra đủ 4 nhóm + không chứa username (toLowerCase(Locale.ROOT))
}

public static int calculateAge(LocalDate dob, LocalDate today) {
    return Period.between(dob, today).getYears();
}
```

**Checklist hoàn thành**
- [ ] Đủ 5 hàm, đúng chữ ký (mục 5.2)
- [ ] Dùng `Pattern.matcher(s).matches()`
- [ ] Không hàm nào ném `NullPointerException` với tham số `null`

**Commit**
```
feat(validator): implement username, email, password, phone and age rules
```

---

### TODO-2 · Viết `AccountValidatorTest` (11 phút)

**Việc cần làm** (mỗi hàm có đủ 3 loại: hợp lệ, không hợp lệ, biên)
- [ ] Username: `@ValueSource` hợp lệ (`alice`, `Alice_01`, `Z____`), `@ValueSource` không hợp lệ (`ab_1`, `1alice`, `_alice`, `ali ce`, `alice!`, `alice-01`), `@NullAndEmptySource` + `" "`.
- [ ] Username biên 4/5/6/19/20/21 bằng `@MethodSource` + `"a".repeat(n)`.
- [ ] Email: `@CsvSource(email, expected)` cho các phân vùng; biên độ dài 99/100/101.
- [ ] Password: `@CsvSource(delimiter = '|')` gồm `(password, username, expected, mô tả)`; biên 7/8/32/33.
- [ ] Phone: `@ValueSource` 5 đầu số hợp lệ và các giá trị sai.
- [ ] Tuổi: `@CsvSource(dob, today, expectedAge)` với **ngày cố định**, gồm đúng sinh nhật 18, 18 tuổi trừ 1 ngày, và 29/02 năm nhuận.

**Gợi ý code**
```java
@ParameterizedTest(name = "[{index}] độ dài {0} -> {1}")
@MethodSource("lab2.account.AccountValidatorTest#usernameLengths")
void isValidUsername_BoundaryLength(int length, boolean expected) {
    assertEquals(expected, AccountValidator.isValidUsername("a".repeat(length)));
}

static Stream<Arguments> usernameLengths() {          // PHẢI là static
    return Stream.of(Arguments.of(4, false), Arguments.of(5, true), Arguments.of(20, true),
                     Arguments.of(21, false) /* thêm 6, 19 */);
}

@ParameterizedTest(name = "[{index}] {3}")
@CsvSource(delimiter = '|', value = {
    "Secret@123    | alice_01 | true  | hợp lệ",
    "secret@123    | alice_01 | false | thiếu chữ hoa",
    "'Secret @123' | alice_01 | false | chứa khoảng trắng",   // giữ khoảng trắng bằng '...'
    "Xalice_01@1   | alice_01 | false | chứa username",
    "Xalice_01@1   |          | true  | username null -> bỏ qua"  // ô trống = null
})
void isValidPassword_Partitions(String pw, String user, boolean expected, String desc) {
    assertEquals(expected, AccountValidator.isValidPassword(pw, user));
}

@ParameterizedTest(name = "[{index}] sinh {0}, hôm nay {1} -> {2} tuổi")
@CsvSource({
    "2008-09-28, 2026-09-28, 18",   // đúng sinh nhật 18
    "2008-09-29, 2026-09-28, 17",   // 18 tuổi trừ 1 ngày
    "2008-02-29, 2026-02-28, 17",   // năm nhuận
    "2008-02-29, 2026-03-01, 18"
})
void calculateAge_Boundaries(LocalDate dob, LocalDate today, int expected) {  // JUnit tự đổi String -> LocalDate
    assertEquals(expected, AccountValidator.calculateAge(dob, today));
}
```

**Checklist hoàn thành**
- [ ] `mvn -Dtest=AccountValidatorTest test` xanh
- [ ] Có đủ biên: username 4/5/20/21, password 7/8/32/33, email 100/101, tuổi 17/18
- [ ] Tên lượt chạy dễ đọc nhờ `@ParameterizedTest(name = ...)`

**Commit**
```
test(validator): add parameterized EP and BVA tests for AccountValidator
```

---

### TODO-3 · `PasswordHasher` và `Account` (7 phút)

**Việc cần làm**
- [ ] `PasswordHasher`: `generateSalt()` bằng `SecureRandom`, `hash(salt, raw)` SHA-256 và trả hex 64 ký tự, `matches(...)` (null trả `false`).
- [ ] `Account`: `username`, `email`, `dateOfBirth`, `phone`, `salt`, `status`, `failedAttempts`, **`locked` (boolean)**, `passwordHistory` (`List<String>`, phần tử cuối là mật khẩu hiện tại). Getter `public`; hàm thay đổi trạng thái để **package-private**.

**Gợi ý code**
```java
public static String hash(String salt, String rawPassword) {
    try {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(md.digest(rawPassword.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
        throw new IllegalStateException(e);
    }
}
```
```java
// Account
public boolean isLocked()           { return locked; }
void incrementFailedAttempts()      { failedAttempts++; }
void resetFailedAttempts()          { failedAttempts = 0; }
void lock()                         { locked = true; }
void unlock()                       { locked = false; failedAttempts = 0; }
void setStatus(AccountStatus s)     { status = s; }
public String getCurrentPasswordHash() { return passwordHistory.get(passwordHistory.size() - 1); }
public List<String> getPasswordHistory() { return List.copyOf(passwordHistory); }
```

**Checklist hoàn thành**
- [ ] Không lưu hay so sánh mật khẩu ở dạng rõ
- [ ] `getPasswordHistory()` trả bản sao, không để lộ list nội bộ

**Commit**
```
feat(account): add Account entity and SHA-256 salted PasswordHasher
```

---

### TODO-4 · Cài đặt `register()` (8 phút)

**Thứ tự kiểm tra bắt buộc:** `REG-01 → 02 → 04 → 06 → 07 → 08 → 09 → 03 → 05 → 10`

**Việc cần làm**
- [ ] Constructor khởi tạo `Map<String, Account>` (key = username lowercase) và `Map<String, String>` cho email lowercase.
- [ ] Kiểm tra theo **đúng thứ tự** ở trên; `today = LocalDate.now()`, tuổi tính bằng `AccountValidator.calculateAge(dob, today)`.
- [ ] Phone: `null` hoặc `""` được chấp nhận; `"   "` là `INVALID_PHONE`.
- [ ] Thành công: sinh salt riêng, lưu hash, email lưu lowercase.

**Gợi ý code**
```java
public ResultCode register(String username, String email, String password,
                           String confirmPassword, LocalDate dateOfBirth, String phone) {
    LocalDate today = LocalDate.now();
    if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)
            || dateOfBirth == null || dateOfBirth.isAfter(today)) return ResultCode.INVALID_INPUT;
    if (!AccountValidator.isValidUsername(username)) return ResultCode.INVALID_USERNAME;
    if (!AccountValidator.isValidEmail(email))       return ResultCode.INVALID_EMAIL;
    // TODO: WEAK_PASSWORD -> PASSWORD_MISMATCH -> UNDERAGE -> INVALID_PHONE
    // TODO: DUPLICATE_USERNAME -> DUPLICATE_EMAIL (so sánh bằng key lowercase)
    String salt = PasswordHasher.generateSalt();
    // TODO: tạo Account, put vào 2 map
    return ResultCode.SUCCESS;
}

private static boolean isBlank(String s) { return s == null || s.isBlank(); }
private static String key(String s)     { return s.toLowerCase(Locale.ROOT); }
```

**Checklist hoàn thành**
- [ ] Đúng thứ tự kiểm tra
- [ ] Logic tính tuổi nằm ở `calculateAge` (hàm thuần), `register()` chỉ gọi lại
- [ ] Đăng ký thất bại thì **không** tạo tài khoản

**Commit**
```
feat(register): implement BR-REG-01..10 with required validation order
```

---

### TODO-5 · Test `register()` với `@Nested Register` (12 phút)

**Việc cần làm**
- [ ] `@BeforeEach` ở lớp ngoài: `service = new AccountService()`.
- [ ] Test thành công: assert `SUCCESS` **và** trạng thái (`ACTIVE`, `failedAttempts == 0`, `isLocked() == false`, hash ≠ mật khẩu rõ, email lowercase).
- [ ] `@MethodSource` cho các input sai, mỗi quy tắc 1 dòng, và **≥ 3 dòng thứ tự ưu tiên** (vi phạm nhiều quy tắc cùng lúc).
- [ ] `@NullAndEmptySource` + `" "` cho username, email, password.
- [ ] `@ValueSource` trùng username và trùng email không phân biệt hoa/thường.
- [ ] Biên tuổi qua `register()`: ngày sinh **tương đối** so với hôm nay (`@CsvSource(yearsAgo, plusDays, expected)`).

**Gợi ý code**
```java
AccountService service;

@BeforeEach
void setUp() {
    service = new AccountService();          // mỗi test một service mới -> test độc lập
}

@Nested
class Register {
    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("lab2.account.AccountServiceTest#invalidRegisterInputs") // tên đầy đủ, method static ở lớp ngoài
    void register_InvalidInput_ReturnsExpectedCode(String desc, String u, String e, String p, String c,
                                                   LocalDate dob, String phone, ResultCode expected) {
        assertEquals(expected, service.register(u, e, p, c, dob, phone));
        assertTrue(service.findByUsername(u).isEmpty());          // assert trạng thái
    }

    @ParameterizedTest(name = "[{index}] today - {0} năm + {1} ngày -> {2}")
    @CsvSource({"18, 0, SUCCESS", "18, 1, UNDERAGE", "0, 1, INVALID_INPUT"})
    void register_AgeBoundary(int yearsAgo, int plusDays, ResultCode expected) {
        LocalDate dob = LocalDate.now().minusYears(yearsAgo).plusDays(plusDays);
        assertEquals(expected, service.register(USER, EMAIL, PASS, PASS, dob, null));
    }
}

static Stream<Arguments> invalidRegisterInputs() {
    return Stream.of(
        Arguments.of("username sai", "1alice", EMAIL, PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
        // ... mỗi BR một dòng ...
        Arguments.of("username sai + email sai", "1alice", "bad", PASS, PASS, DOB, PHONE, ResultCode.INVALID_USERNAME),
        Arguments.of("email sai + mk yếu", USER, "bad", "weak", "weak", DOB, PHONE, ResultCode.INVALID_EMAIL),
        Arguments.of("mk yếu + confirm lệch", USER, EMAIL, "weak", "x", DOB, PHONE, ResultCode.WEAK_PASSWORD));
}
```

**Checklist hoàn thành**
- [ ] Mỗi BR-REG-01..10 có ít nhất 1 test
- [ ] Có ≥ 3 test thứ tự ưu tiên
- [ ] Test độc lập: mỗi test có `AccountService` mới

**Commit**
```
test(register): cover BR-REG rules, priority order and age boundary
```

---

### TODO-6 · Cài đặt `login()`, `disableAccount()`, `unlockAccount()`, `findByUsername()`, `isLocked()` (8 phút)

**Thứ tự:** `LOG-01 → tồn tại → DISABLED → đang khóa → kiểm tra mật khẩu → thành công`

**Gợi ý code**
```java
public ResultCode login(String username, String password) {
    if (isBlank(username) || isBlank(password)) return ResultCode.INVALID_INPUT;
    Account acc = accounts.get(key(username));
    if (acc == null) return ResultCode.INVALID_CREDENTIALS;              // không tiết lộ lý do
    if (acc.getStatus() == AccountStatus.DISABLED) return ResultCode.ACCOUNT_DISABLED;
    if (acc.isLocked()) return ResultCode.ACCOUNT_LOCKED;                // không tăng bộ đếm

    if (!PasswordHasher.matches(acc.getSalt(), password, acc.getCurrentPasswordHash())) {
        acc.incrementFailedAttempts();
        // TODO: nếu failedAttempts >= MAX_FAILED_ATTEMPTS -> acc.lock(), trả ACCOUNT_LOCKED
        return ResultCode.INVALID_CREDENTIALS;
    }
    // TODO: failedAttempts = 0
    return ResultCode.SUCCESS;
}

public ResultCode unlockAccount(String username) {          // BR-ADM-03
    Optional<Account> acc = findByUsername(username);
    if (acc.isEmpty()) return ResultCode.USER_NOT_FOUND;
    acc.get().unlock();                                         // locked = false, failedAttempts = 0
    return ResultCode.SUCCESS;
}
```

**Checklist hoàn thành**
- [ ] Lần sai thứ 5 (`>=`, không phải `>`) mới khóa
- [ ] `isLocked()` trả `false` cho user không tồn tại hoặc username null
- [ ] `disableAccount()` / `unlockAccount()` với username rỗng hoặc không tồn tại trả `USER_NOT_FOUND`

**Commit**
```
feat(login): implement BR-LOG rules with lock after 5 failed attempts
feat(service): add disableAccount, unlockAccount, findByUsername and isLocked
```

---

### TODO-7 · Test `login()` với `@Nested Login` (12 phút)

**Việc cần làm** (bám theo bảng quyết định mục 0.2)
- [ ] `@BeforeEach` trong `Login`: đăng ký sẵn user mẫu.
- [ ] Rule 6: đăng nhập đúng → `SUCCESS`, `failedAttempts == 0`.
- [ ] Rule 1: user không tồn tại và sai mật khẩu trả **cùng** `INVALID_CREDENTIALS`.
- [ ] Rule 2: DISABLED + mật khẩu đúng/sai → `ACCOUNT_DISABLED` (`@ValueSource`).
- [ ] Rule 4: `@ValueSource(ints = {1, 2, 3, 4})` → `INVALID_CREDENTIALS`, `failedAttempts == n`, `isLocked == false`.
- [ ] Rule 5: lần sai thứ 5 → `ACCOUNT_LOCKED`, `isLocked == true`.
- [ ] Rule 3: đang khóa, nhập đúng hay sai đều `ACCOUNT_LOCKED`, bộ đếm **không đổi**.
- [ ] **Biên số lần sai** bằng `@CsvSource`: sai 4 lần rồi nhập đúng → `SUCCESS`; sai 5 lần rồi nhập đúng → `ACCOUNT_LOCKED`.
- [ ] Mở khóa: `unlockAccount` → đăng nhập được, bộ đếm bắt đầu lại từ 0.
- [ ] Username không phân biệt hoa/thường, password phân biệt hoa/thường.
- [ ] `@NullAndEmptySource` cho username và password.

**Gợi ý code**
```java
void failLogin(int times) {
    for (int i = 0; i < times; i++) service.login(USER, WRONG);
}

@ParameterizedTest(name = "[{index}] {0} lần sai -> {1}, locked={2}")
@CsvSource({
    "4, SUCCESS,        false",     // ngay dưới ngưỡng
    "5, ACCOUNT_LOCKED, true",      // đúng ngưỡng
    "6, ACCOUNT_LOCKED, true"
})
void login_CorrectPasswordAfterNFailures(int failures, ResultCode expected, boolean locked) {
    failLogin(failures);                                             // Arrange
    ResultCode result = service.login(USER, PASS);                   // Act
    assertEquals(expected, result);                                  // Assert
    assertEquals(locked, service.isLocked(USER));
}

@Test
void login_AfterAdminUnlock_CounterRestartsAndCanLogin() {
    failLogin(5);
    assertEquals(ResultCode.SUCCESS, service.unlockAccount(USER));

    assertFalse(service.isLocked(USER));
    assertEquals(ResultCode.INVALID_CREDENTIALS, service.login(USER, WRONG));
    assertEquals(1, account().getFailedAttempts());                 // bộ đếm bắt đầu lại
    assertEquals(ResultCode.SUCCESS, service.login(USER, PASS));
}
```

**Checklist hoàn thành**
- [ ] Có biên số lần sai 4 / 5
- [ ] Mỗi dòng của bảng quyết định (1–6) có ít nhất 1 test
- [ ] Assert cả trạng thái (`failedAttempts`, `isLocked`, `status`)

**Commit**
```
test(login): cover decision table, 4/5 failed-attempt boundary and admin unlock
```

---

### TODO-8 · Coverage và lỗi giả lập (11 phút)

**Việc cần làm**
- [ ] `mvn clean test` → 0 failures, 0 errors, 0 skipped.
- [ ] Mở `target/site/jacoco/index.html` và chụp ảnh. Mục tiêu: `AccountValidator` và `AccountService` (không tính stub bonus) đạt **Line ≥ 80%, Branch ≥ 70%**.
- [ ] Chèn **3 lỗi**, **mỗi lần một lỗi**, chạy test, ghi lại, rồi **hoàn tác** (`git checkout -- <file>`).

**Gợi ý lỗi giả lập**

| # | File / vị trí | Thay đổi | Test mong đợi bị fail |
|---|---|---|---|
| M1 | `login()` | `>= MAX_FAILED_ATTEMPTS` → `> MAX_FAILED_ATTEMPTS` | sai 5 lần phải khóa |
| M2 | `login()` | bỏ nhánh `if (acc.isLocked())` | đang khóa nhập đúng vẫn phải `ACCOUNT_LOCKED` |
| M3 | `AccountValidator` | `{4,19}` → `{4,20}` | username 21 ký tự |
| M4 | `register()` | `< MIN_AGE` → `<= MIN_AGE` | biên đúng 18 tuổi |
| M5 | `register()` | bỏ `toLowerCase` khi lưu email | trùng email khác hoa/thường |
| M6 | `Account.unlock()` | quên đặt `failedAttempts = 0` | mở khóa rồi sai 1 lần |

Mẫu bảng ghi vào README:

| # | Lỗi chèn | Test fail | Đã hoàn tác |
|---|---|---|---|
| M1 | ... | `Login.login_WrongPassword5thTime_LocksAccount` | ✅ |

**Checklist hoàn thành**
- [ ] Ảnh báo cáo JaCoCo
- [ ] ≥ 3 lỗi giả lập đều làm ít nhất 1 test fail (nếu không có test nào fail thì **bổ sung test**)
- [ ] Code đã hoàn tác, `git status` sạch trước commit

**Commit**
```
test: add missing tests found by manual mutation testing
docs: add jacoco coverage screenshot and mutation table
```

---

### TODO-9 · README, checklist và nộp bài (5 phút)

- [ ] `README.md`: cách chạy (`mvn clean test`), số test, coverage, bảng mutation, ma trận truy vết rút gọn (BR → tên test).
- [ ] Đánh dấu checklist mục 4 bên dưới.
- [ ] Nén thành `Lab2_<MSSV>_<HoTen>.zip`, **không** kèm `target/`, `.idea/`, `.vscode/`.

**Commit**
```
docs: add README with run guide, coverage, traceability and checklist
```

---

## 4. Checklist tổng (tự đánh giá trước khi nộp)

### A. Mã production
- [ ] **A1** `mvn clean compile` thành công
- [ ] **A2** `AccountValidator` đủ 5 hàm, null trả `false`, không ném exception
- [ ] **A3** Mật khẩu băm SHA-256 + salt riêng, không lưu bản rõ
- [ ] **A4** `register()` đủ BR-REG-01..10, **đúng thứ tự**
- [ ] **A5** `login()`: sai 5 lần thì khóa; đang khóa không tăng bộ đếm; thành công thì đặt bộ đếm về 0
- [ ] **A6** `unlockAccount()` mở khóa và đặt `failedAttempts = 0`
- [ ] **A7** Username/email không phân biệt hoa/thường (`toLowerCase(Locale.ROOT)`), mật khẩu phân biệt
- [ ] **A8** Không dùng `Clock`; không `System.out`, không biến static giữ trạng thái

### B. Mã test
- [ ] **B1** ≥ 20 phương thức test, ≥ 12 `@ParameterizedTest`, ≥ 60 lượt chạy
- [ ] **B2** Dùng đủ `@ValueSource`, `@NullAndEmptySource`, `@CsvSource`, `@MethodSource`
- [ ] **B3** Biên: username 4/5/20/21, mật khẩu 7/8/32/33, email 100/101, tuổi 17/18 (qua `calculateAge`)
- [ ] **B4** Biên số lần đăng nhập sai 4/5 và test mở khóa
- [ ] **B5** ≥ 3 test thứ tự ưu tiên trong `register()`
- [ ] **B6** `@Nested` + `@BeforeEach` tạo service mới cho mỗi test
- [ ] **B7** Assert cả trạng thái, không `assertTrue(true)`, không `Thread.sleep`
- [ ] **B8** Tên test theo mẫu `method_TinhHuong_KetQua`, AAA

### C. Chất lượng và nộp bài
- [ ] **C1** `mvn clean test`: 0 failures / errors / skipped
- [ ] **C2** JaCoCo Line ≥ 80%, Branch ≥ 70% (có ảnh chụp)
- [ ] **C3** ≥ 3 lỗi giả lập có ghi lại
- [ ] **C4** Lịch sử git ≥ 6 commit đúng Conventional Commits
- [ ] **C5** File zip đúng tên, không có `target/`

---

## 5. Thang điểm (bản 90 phút)

| Hạng mục | Điểm | Checklist |
|---|---|---|
| `AccountValidator` đúng đặc tả | 1.5 | A2 |
| `register()` đúng đặc tả và thứ tự | 1.5 | A3, A4, A7 |
| `login()` + khóa/mở khóa + admin đúng đặc tả | 1.5 | A5, A6, A8 |
| Thiết kế test: EP, BVA, thứ tự ưu tiên, null/rỗng | 2.0 | B3, B4, B5 |
| Parameterized Test: đủ nguồn, đủ số lượng, tên dễ đọc | 1.5 | B1, B2, B8 |
| Chất lượng assert, coverage, mutation | 1.0 | B7, C2, C3 |
| Tổ chức mã, commit message, README | 1.0 | B6, C4, C5 |
| **Tổng** | **10.0** | |

**Bonus (tối đa +1.0, làm ở nhà):** `changePassword()` + test (+0.4) · quên/đặt lại mật khẩu (token dùng một lần, yêu cầu mới vô hiệu token cũ, đặt lại thành công thì mở khóa) (+0.4) · `@EnumSource` cho `ResultCode` hoặc `@CsvFileSource` (+0.2).

---

## 6. Lỗi thường gặp

- Dùng `find()` thay vì `matches()`, dẫn đến `123alice456` bị coi là hợp lệ.
- Quên chuẩn hóa hoa/thường khi tra `Map`, làm cho `ALICE_01` đăng ký trùng được.
- Khóa sai ngưỡng: dùng `>` thay vì `>=` nên phải sai 6 lần mới khóa.
- Đang khóa mà vẫn tăng `failedAttempts`, hoặc mở khóa nhưng quên đặt bộ đếm về 0.
- Test tuổi qua `register()` bằng ngày cố định (ví dụ `2008-09-28`): test sẽ **hỏng theo thời gian**. Dùng ngày cố định cho `calculateAge(dob, today)`, còn với `register()` thì dùng ngày tương đối `LocalDate.now().minusYears(18)`.
- `@MethodSource` trong lớp `@Nested`: provider phải `static` và đặt ở lớp ngoài, tham chiếu bằng `"lab2.account.AccountServiceTest#tenMethod"`.
- `@CsvSource`: ô trống là `null`, chuỗi rỗng viết `''`, giữ khoảng trắng bằng `'...'`.
- Chỉ assert `ResultCode`: test vẫn xanh khi `failedAttempts` sai.
