# SWT301 – ProgressTest 1: Account Management (JUnit 5 & JaCoCo)

> **Môn học:** SWT301 – Software Testing  
> **Chủ đề:** Unit Testing module Account Management bằng JUnit 5 (Parameterized Test) và JaCoCo  
> **MSSV:** DE200475  

---

## 1. Hướng dẫn chạy và sinh báo cáo

### Lệnh chạy kiểm thử và đo độ phủ:
```bash
# Chạy toàn bộ test suite và sinh báo cáo JaCoCo
mvn clean test

# Chạy riêng từng lớp test
mvn -Dtest=AccountValidatorTest test
mvn -Dtest=AccountServiceTest test
```

### Đường dẫn báo cáo JaCoCo:
- Mở file trên trình duyệt: `target/site/jacoco/index.html`

---

## 2. Kết quả kiểm thử tổng hợp

| Tiêu chí | Yêu cầu tối thiểu (90 phút) | Kết quả đạt được | Trạng thái |
|---|:---:|:---:|:---:|
| **Số phương thức test** | ≥ 20 | **26 phương thức** (Core) | ✅ Đạt |
| **Số `@ParameterizedTest`** | ≥ 12 | **18 phương thức** | ✅ Đạt |
| **Tổng lượt chạy (Invocations)** | ≥ 60 | **≥ 100 lượt chạy** | ✅ Đạt |
| **Kết quả thực thi** | 0 fail / error | **100% Passed (0 Failures, 0 Errors)** | ✅ Đạt |
| **Nguồn dữ liệu Parameterized** | Đủ 4 nguồn | Đủ `@ValueSource`, `@NullAndEmptySource`, `@CsvSource`, `@MethodSource` | ✅ Đạt |
| **Độ phủ JaCoCo** | Line ≥ 80%, Branch ≥ 70% | Line ≥ 90%, Branch ≥ 85% | ✅ Đạt |
| **Kiểm thử đột biến (Mutation)** | ≥ 3 lỗi giả lập | **5/5 lỗi bị phát hiện** (Killed) | ✅ Đạt |

---

## 3. Bảng kiểm thử đột biến thủ công (Manual Mutation Testing)

| # | File / Vị trí | Lỗi chèn thử nghiệm (Mutant) | Test case phát hiện lỗi (Killed Test) | Đã hoàn tác |
|:---:|---|---|---|:---:|
| **M1** | `AccountService.java` (`login`) | `>= MAX_FAILED_ATTEMPTS` $\rightarrow$ `> MAX_FAILED_ATTEMPTS` | `Login.login_WrongPassword5thTime_LocksAccount`<br>`Login.login_WhileLocked_RejectsWithoutIncrement`<br>`Login.login_CorrectPasswordAfterNFailures[5]` | ✅ |
| **M2** | `AccountService.java` (`login`) | Bỏ nhánh kiểm tra `if (account.isLocked())` | `Login.login_WhileLocked_RejectsWithoutIncrement`<br>`Login.login_CorrectPasswordAfterNFailures[5]`, `[6]` | ✅ |
| **M3** | `AccountValidator.java` | Regex username `{4,19}` $\rightarrow$ `{4,20}` | `Username.isValidUsername_BoundaryLength[21]`<br>`Username.isValidUsername_InvalidValues` | ✅ |
| **M4** | `AccountService.java` (`register`) | `< MIN_AGE` $\rightarrow$ `<= MIN_AGE` | `Register.register_AgeBoundary[18, 0]` | ✅ |
| **M5** | `Account.java` (`unlock`) | Bỏ dòng đặt `failedAttempts = 0;` | `Login.login_AfterAdminUnlock_CounterRestartsAndCanLogin` | ✅ |

---

## 4. Ma trận truy vết yêu cầu (Traceability Matrix)

| Business Rule (BR) | Mô tả ngắn gọn | Phương thức Test bảo vệ |
|---|---|---|
| **BR-REG-01** | Bắt buộc nhập đủ thông tin, ngày sinh không ở tương lai | `Register.register_UsernameNullEmptyBlank...`, `...EmailNullEmptyBlank...`, `...PasswordNullEmptyBlank...`, `invalidRegisterInputs[dob null]`, `register_AgeBoundary[0, 1]` |
| **BR-REG-02** | Định dạng username (5–20 ký tự, bắt đầu bằng chữ cái) | `AccountValidatorTest.Username.*`, `invalidRegisterInputs[username sai]` |
| **BR-REG-03** | Username là duy nhất (không phân biệt hoa/thường) | `Register.register_DuplicateUsernameIgnoreCase_ReturnsDuplicateUsername` |
| **BR-REG-04** | Định dạng email `local@domain.tld`, max 100 ký tự | `AccountValidatorTest.Email.*`, `invalidRegisterInputs[email sai]` |
| **BR-REG-05** | Email là duy nhất (không phân biệt hoa/thường) | `Register.register_DuplicateEmailIgnoreCase_ReturnsDuplicateEmail` |
| **BR-REG-06** | Độ phức tạp mật khẩu (8–32 ký tự, 4 nhóm, không chứa username) | `AccountValidatorTest.Password.*`, `invalidRegisterInputs[mật khẩu yếu / chứa username]` |
| **BR-REG-07** | Xác nhận mật khẩu phải khớp chính xác | `invalidRegisterInputs[confirm lệch]` |
| **BR-REG-08** | Độ tuổi tối thiểu 18 tuổi tròn | `AccountValidatorTest.calculateAge_Boundaries`, `Register.register_AgeBoundary` |
| **BR-REG-09** | Định dạng số điện thoại (tùy chọn, 10 số đầu 03/05/07/08/09) | `AccountValidatorTest.Phone.*`, `Register.register_PhoneNullOrEmpty_Success`, `invalidRegisterInputs[phone ...]` |
| **BR-REG-10** | Lưu mật khẩu băm SHA-256 + salt riêng, email lowercase | `Register.register_ValidData_CreatesActiveAccountWithHashedPassword`, `register_UpperCaseEmail_StoredAsLowerCase`, `register_TwoAccountsSamePassword_HaveDifferentSaltAndHash` |
| **Thứ tự REG** | Kiểm tra theo đúng thứ tự ưu tiên REG-01..10 | Các dòng thứ tự ưu tiên trong `invalidRegisterInputs`, `register_DuplicateUsernameButInvalidEmail_ReturnsInvalidEmailFirst` |
| **BR-LOG-01** | Bắt buộc nhập đủ username và password khi đăng nhập | `Login.login_UsernameNullEmptyBlank...`, `login_PasswordNullEmptyBlank...` |
| **BR-LOG-02** | Username không phân biệt hoa/thường, mật khẩu phân biệt | `Login.login_UsernameIgnoreCase_Success`, `login_PasswordCaseSensitive_ReturnsInvalidCredentials` |
| **BR-LOG-03** | Sai username hoặc sai password đều trả cùng mã lỗi | `Login.login_UnknownUserAndWrongPassword_ReturnSameCode` |
| **BR-LOG-04** | Tài khoản bị vô hiệu hóa (`DISABLED`) không thể đăng nhập | `Login.login_DisabledAccount_ReturnsAccountDisabled` |
| **BR-LOG-05** | Khóa tài khoản sau 5 lần đăng nhập sai liên tiếp | `Login.login_WrongPasswordLessThan5Times_IncrementsCounter`, `login_WrongPassword5thTime_LocksAccount`, `login_CorrectPasswordAfterNFailures` |
| **BR-LOG-06** | Khi đang khóa, mọi lần đăng nhập bị từ chối và không tăng bộ đếm | `Login.login_WhileLocked_RejectsWithoutIncrement` |
| **BR-LOG-08** | Đăng nhập đúng đặt lại bộ đếm số lần sai về 0 | `Login.login_CorrectCredentials_Success`, `login_SuccessAfterFailures_ResetsCounter` |
| **BR-ADM-01/02**| Vô hiệu hóa tài khoản và tìm kiếm tài khoản | `Admin.disableAccount_*`, `Admin.findByUsername_BlankOrUnknown_ReturnsEmpty` |
| **BR-ADM-03** | Admin mở khóa tài khoản, đặt `failedAttempts = 0` | `Login.login_AfterAdminUnlock_CounterRestartsAndCanLogin`, `Admin.unlockAccount_BlankOrUnknown_ReturnsUserNotFound` |

---

## 5. Checklist tự đánh giá (Checklist hoàn thành)

### A. Mã Production
- [x] **A1** `mvn clean compile` thành công.
- [x] **A2** `AccountValidator` đủ 5 hàm, `null` trả `false`, không ném exception.
- [x] **A3** Băm mật khẩu bằng SHA-256 + salt riêng biệt, không lưu mật khẩu rõ.
- [x] **A4** `register()` cài đặt đủ BR-REG-01..10 theo đúng thứ tự ưu tiên bắt buộc.
- [x] **A5** `login()` khóa sau 5 lần sai liên tiếp; đang khóa không tăng bộ đếm; đăng nhập đúng reset về 0.
- [x] **A6** `unlockAccount()` mở khóa và đặt `failedAttempts = 0`.
- [x] **A7** Username/Email lưu và tra cứu chuẩn hóa lowercase, password giữ nguyên phân biệt hoa/thường.
- [x] **A8** Không dùng `Clock`/`MutableClock`, không dùng `System.out`, không dùng biến `static` giữ trạng thái.

### B. Mã Test
- [x] **B1** Đạt ≥ 20 phương thức test, ≥ 12 `@ParameterizedTest`, ≥ 60 lượt chạy.
- [x] **B2** Sử dụng đa dạng các nguồn dữ liệu: `@ValueSource`, `@NullAndEmptySource`, `@CsvSource`, `@MethodSource`.
- [x] **B3** Kiểm thử biên (BVA): username (4, 5, 20, 21), password (7, 8, 32, 33), email (100, 101), tuổi (17, 18, năm nhuận 29/02).
- [x] **B4** Kiểm thử biên số lần sai 4/5 lần và quy trình admin mở khóa.
- [x] **B5** Đầy đủ các test case kiểm tra thứ tự ưu tiên lỗi trong `register()`.
- [x] **B6** Tổ chức `@Nested` (`Register`, `Login`, `Admin`) kết hợp `@BeforeEach` đảm bảo tính độc lập giữa các test.
- [x] **B7** Assert kiểm tra cả mã trả về và trạng thái đối tượng (`status`, `failedAttempts`, `isLocked`).
- [x] **B8** Đặt tên test theo chuẩn `method_Scenario_ExpectedResult`, cấu trúc rõ ràng AAA.

### C. Chất lượng & Nộp bài
- [x] **C1** `mvn clean test` vượt qua 100% (0 failures, 0 errors).
- [x] **C2** JaCoCo đạt Line ≥ 80%, Branch ≥ 70%.
- [x] **C3** Hoàn thành bảng Mutation Testing với các lỗi đột biến giả lập.
- [x] **C4** Lịch sử Git tuân thủ nghiêm ngặt chuẩn Conventional Commits.
