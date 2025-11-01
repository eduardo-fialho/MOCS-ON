package com.mocs_on.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserAccountService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate jdbcTemplate;

    @Value("${app.users.table:usuarios}")
    private String usersTable;

    @Value("${app.users.email-column:email}")
    private String usersEmailColumn;

    @Value("${app.users.password-column:senha}")
    private String usersPasswordColumn;

    public UserAccountService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean userExists(String email) {
        String normalized = normalizeEmail(email);
        if (!isValidEmail(normalized)) {
            return false;
        }
        String sql = String.format(
                "SELECT COUNT(*) FROM `%s` WHERE LOWER(`%s`) = LOWER(?)",
                usersTable,
                usersEmailColumn
        );
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, normalized);
        return count != null && count > 0;
    }

    public Optional<String> findPasswordHashByEmail(String email) {
        String normalized = normalizeEmail(email);
        if (!isValidEmail(normalized)) {
            return Optional.empty();
        }
        String sql = String.format(
                "SELECT `%s` FROM `%s` WHERE LOWER(`%s`) = LOWER(?)",
                usersPasswordColumn,
                usersTable,
                usersEmailColumn
        );
        List<String> hashes = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getString(usersPasswordColumn),
                normalized
        );
        return hashes.stream().findFirst();
    }

    public int updatePassword(String email, String passwordHash) {
        String normalized = normalizeEmail(email);
        if (!isValidEmail(normalized)) {
            return 0;
        }
        String sql = String.format(
                "UPDATE `%s` SET `%s` = ? WHERE LOWER(`%s`) = LOWER(?)",
                usersTable,
                usersPasswordColumn,
                usersEmailColumn
        );
        return jdbcTemplate.update(sql, passwordHash, normalized);
    }
}
