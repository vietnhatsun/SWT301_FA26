package fu.de200475;

import java.time.LocalDate;
import java.util.HashMap;
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

    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        throw new UnsupportedOperationException("TODO");
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
        throw new UnsupportedOperationException("TODO");
    }

    public boolean isLocked(String username) {
        throw new UnsupportedOperationException("TODO");
    }
}
