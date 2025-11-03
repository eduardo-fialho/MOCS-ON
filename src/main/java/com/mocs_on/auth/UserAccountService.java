package com.mocs_on.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
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

    @Value("${app.users.name-column:nome}")
    private String usersNameColumn;

    @Value("${app.users.type-column:tipo}")
    private String usersTypeColumn;

    @Value("${app.users.created-at-column:created_at}")
    private String usersCreatedAtColumn;

    @Value("${app.users.updated-at-column:updated_at}")
    private String usersUpdatedAtColumn;

    @Value("${app.users.change-log-table:user_change_logs}")
    private String userChangeLogTable;

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

    public Optional<UserRecord> findUserByEmail(String email) {
        String normalized = normalizeEmail(email);
        if (!isValidEmail(normalized)) {
            return Optional.empty();
        }
        String sql = String.format(
                "SELECT id, `%s` AS name, `%s` AS email, `%s` AS password_hash, `%s` AS tipo, `%s` AS created_at, `%s` AS updated_at FROM `%s` WHERE LOWER(`%s`) = LOWER(?)",
                usersNameColumn,
                usersEmailColumn,
                usersPasswordColumn,
                usersTypeColumn,
                usersCreatedAtColumn,
                usersUpdatedAtColumn,
                usersTable,
                usersEmailColumn
        );
        List<UserRecord> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), normalized);
        return result.stream().findFirst();
    }

    public Optional<UserRecord> findUserById(long id) {
        String sql = String.format(
                "SELECT id, `%s` AS name, `%s` AS email, `%s` AS password_hash, `%s` AS tipo, `%s` AS created_at, `%s` AS updated_at FROM `%s` WHERE id = ?",
                usersNameColumn,
                usersEmailColumn,
                usersPasswordColumn,
                usersTypeColumn,
                usersCreatedAtColumn,
                usersUpdatedAtColumn,
                usersTable
        );
        List<UserRecord> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs), id);
        return result.stream().findFirst();
    }

    public List<UserRecord> findAllUsers() {
        String sql = String.format(
                "SELECT id, `%s` AS name, `%s` AS email, `%s` AS password_hash, `%s` AS tipo, `%s` AS created_at, `%s` AS updated_at FROM `%s` ORDER BY `%s`",
                usersNameColumn,
                usersEmailColumn,
                usersPasswordColumn,
                usersTypeColumn,
                usersCreatedAtColumn,
                usersUpdatedAtColumn,
                usersTable,
                usersNameColumn
        );
        return jdbcTemplate.query(sql, (rs, rowNum) -> mapUser(rs));
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

    public int createUser(String name, String email, String passwordHash, String type) {
        String normalized = normalizeEmail(email);
        validateEmailOrThrow(normalized);

        String safeName = name == null ? "" : name.trim();
        String safeType = normalizeRole(type);

        boolean hasTypeColumn = usersTypeColumn != null && !usersTypeColumn.isBlank();
        boolean hasNameColumn = usersNameColumn != null && !usersNameColumn.isBlank();

        if (hasNameColumn && hasTypeColumn) {
            String sql = String.format(
                "INSERT INTO `%s` (`%s`, `%s`, `%s`, `%s`) VALUES (?, ?, ?, ?)",
                    usersTable,
                    usersNameColumn,
                    usersEmailColumn,
                    usersPasswordColumn,
                    usersTypeColumn
            );
            return jdbcTemplate.update(sql, safeName, normalized, passwordHash, safeType);
        }

        if (hasNameColumn) {
            String sql = String.format(
                    "INSERT INTO `%s` (`%s`, `%s`, `%s`) VALUES (?, ?, ?)",
                    usersTable,
                    usersNameColumn,
                    usersEmailColumn,
                    usersPasswordColumn
            );
            return jdbcTemplate.update(sql, safeName, normalized, passwordHash);
        }

        String sql = String.format(
                "INSERT INTO `%s` (`%s`, `%s`) VALUES (?, ?)",
                usersTable,
                usersEmailColumn,
                usersPasswordColumn
        );
        return jdbcTemplate.update(sql, normalized, passwordHash);
    }

    public void updateUser(long userId, String newName, String newEmail, String newType, String newPasswordHash, String changedByEmail) {
        Optional<UserRecord> existingOpt = findUserById(userId);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
        UserRecord existing = existingOpt.get();

        String normalizedEmail = normalizeEmail(newEmail);
        validateEmailOrThrow(normalizedEmail);

        if (!existing.email().equalsIgnoreCase(normalizedEmail) && userExists(normalizedEmail)) {
            throw new IllegalArgumentException("E-mail ja cadastrado.");
        }

        String safeName = newName == null ? "" : newName.trim();
        String safeType = normalizeRole(newType);

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        sql.append(String.format("UPDATE `%s` SET `%s` = ?, `%s` = ?, `%s` = ?", usersTable, usersNameColumn, usersEmailColumn, usersTypeColumn));
        params.add(safeName);
        params.add(normalizedEmail);
        params.add(safeType);
        if (newPasswordHash != null) {
            sql.append(String.format(", `%s` = ?", usersPasswordColumn));
            params.add(newPasswordHash);
        }
        sql.append(", ").append(usersUpdatedAtColumn).append(" = CURRENT_TIMESTAMP");
        sql.append(" WHERE id = ?");
        params.add(userId);

        jdbcTemplate.update(sql.toString(), params.toArray());

        List<FieldChange> changes = new ArrayList<>();
        if (!Objects.equals(existing.name(), safeName)) {
            changes.add(new FieldChange("nome", existing.name(), safeName));
        }
        if (!existing.email().equalsIgnoreCase(normalizedEmail)) {
            changes.add(new FieldChange("email", existing.email(), normalizedEmail));
        }
        if (!Objects.equals(existing.type(), safeType)) {
            changes.add(new FieldChange("tipo", existing.type(), safeType));
        }
        if (newPasswordHash != null) {
            changes.add(new FieldChange("senha", "(hash)", "(alterada)"));
        }

        if (!changes.isEmpty()) {
            changes.forEach(change -> insertChangeLog(userId, change.field(), change.oldValue(), change.newValue(), changedByEmail));
        }
    }

    private void validateEmailOrThrow(String normalizedEmail) {
        if (!isValidEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    private String normalizeRole(String type) {
        if (type == null || type.isBlank()) {
            return "DELEGADO";
        }
        return type.trim().toUpperCase(Locale.ROOT);
    }

    private void insertChangeLog(long userId, String field, String oldValue, String newValue, String changedBy) {
        String sql = String.format(
                "INSERT INTO `%s` (user_id, field, old_value, new_value, changed_by, changed_at) VALUES (?, ?, ?, ?, ?, ?)",
                userChangeLogTable
        );
        jdbcTemplate.update(sql, userId, field, truncate(oldValue), truncate(newValue), changedBy, Timestamp.from(Instant.now()));
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 500 ? value.substring(0, 500) : value;
    }

    private UserRecord mapUser(java.sql.ResultSet rs) throws java.sql.SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String type = rs.getString("tipo");
        java.sql.Timestamp created = null;
        java.sql.Timestamp updated = null;
        try {
            created = rs.getTimestamp("created_at");
        } catch (java.sql.SQLException ignored) {
        }
        try {
            updated = rs.getTimestamp("updated_at");
        } catch (java.sql.SQLException ignored) {
        }
        return new UserRecord(id, name, email, passwordHash, type, created, updated);
    }

    public record UserRecord(long id,
                              String name,
                              String email,
                              String passwordHash,
                              String type,
                              java.sql.Timestamp createdAt,
                              java.sql.Timestamp updatedAt) {
    }

    private record FieldChange(String field, String oldValue, String newValue) {}

    public void ensureCoreTables() {
        String usersSql = "CREATE TABLE IF NOT EXISTS `" + usersTable + "` ("
                + " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                + " `" + usersNameColumn + "` VARCHAR(255) NOT NULL,"
                + " `" + usersEmailColumn + "` VARCHAR(255) NOT NULL UNIQUE,"
                + " `" + usersPasswordColumn + "` VARCHAR(255) NOT NULL,"
                + " `" + usersTypeColumn + "` VARCHAR(50) NOT NULL DEFAULT 'DELEGADO',"
                + " `" + usersCreatedAtColumn + "` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + " `" + usersUpdatedAtColumn + "` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,"
                + " PRIMARY KEY (`id`),"
                + " KEY `idx_usuarios_tipo` (`" + usersTypeColumn + "`)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(usersSql);

        String changeSql = "CREATE TABLE IF NOT EXISTS `" + userChangeLogTable + "` ("
                + " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                + " `user_id` INT UNSIGNED NOT NULL,"
                + " `field` VARCHAR(50) NOT NULL,"
                + " `old_value` VARCHAR(500) NULL,"
                + " `new_value` VARCHAR(500) NULL,"
                + " `changed_by` VARCHAR(255) NULL,"
                + " `changed_at` DATETIME NOT NULL,"
                + " PRIMARY KEY (`id`),"
                + " KEY `idx_change_user` (`user_id`),"
                + " CONSTRAINT `fk_change_user` FOREIGN KEY (`user_id`) REFERENCES `" + usersTable + "` (`id`) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(changeSql);

        String tokensSql = "CREATE TABLE IF NOT EXISTS `password_reset_tokens` ("
                + " `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                + " `email` VARCHAR(255) NOT NULL,"
                + " `token_hash` CHAR(64) NOT NULL,"
                + " `expires_at` DATETIME NOT NULL,"
                + " `used_at` DATETIME NULL,"
                + " `created_at` DATETIME NOT NULL,"
                + " `ip` VARCHAR(45) NULL,"
                + " `user_agent` VARCHAR(255) NULL,"
                + " PRIMARY KEY (`id`),"
                + " UNIQUE KEY `uniq_token_hash` (`token_hash`),"
                + " KEY `idx_email` (`email`),"
                + " KEY `idx_expires_at` (`expires_at`),"
                + " CONSTRAINT `fk_token_user` FOREIGN KEY (`email`) REFERENCES `" + usersTable + "` (`" + usersEmailColumn + "`) ON DELETE CASCADE"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        jdbcTemplate.execute(tokensSql);
    }
}
