package fu.de200475;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AccountService {
    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    private final Map<String, Account> accountsByUsername;
    private final Map<String, String> usernameByEmail;
    private final Map<String, String> usernameByToken;
    private final Map<String, String> tokenByUsername;

    public AccountService() {
        this.accountsByUsername = new HashMap<>();
        this.usernameByEmail = new HashMap<>();
        this.usernameByToken = new HashMap<>();
        this.tokenByUsername = new HashMap<>();
    }

    // ================= Đăng ký (BR-REG-01..10) =================
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

    public ResultCode login(String username, String password) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode changePassword(String username, String oldPassword,
                                     String newPassword, String confirmPassword) {
        throw new UnsupportedOperationException("TODO");
    }

    public TokenResult requestPasswordReset(String email) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode resetPassword(String token, String newPassword, String confirmPassword) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode disableAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode unlockAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public Optional<Account> findByUsername(String username) {
        if (isBlank(username)) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByUsername.get(key(username)));
    }

    public boolean isLocked(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    // ================= Helpers =================
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}
