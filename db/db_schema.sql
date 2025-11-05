-- Tabela principal de usuarios
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `senha` VARCHAR(255) NOT NULL,
  `tipo` VARCHAR(50) NOT NULL DEFAULT 'DELEGADO',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_usuarios_tipo` (`tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Historico de alteracoes de perfis
CREATE TABLE IF NOT EXISTS `user_change_logs` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  `field` VARCHAR(50) NOT NULL,
  `old_value` VARCHAR(500) NULL,
  `new_value` VARCHAR(500) NULL,
  `changed_by` VARCHAR(255) NULL,
  `changed_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_change_user` (`user_id`),
  CONSTRAINT `fk_change_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabela para tokens de redefinicao de senha
CREATE TABLE IF NOT EXISTS `password_reset_tokens` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `email` VARCHAR(255) NOT NULL,
  `token_hash` CHAR(64) NOT NULL,
  `expires_at` DATETIME NOT NULL,
  `used_at` DATETIME NULL,
  `created_at` DATETIME NOT NULL,
  `ip` VARCHAR(45) NULL,
  `user_agent` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_token_hash` (`token_hash`),
  KEY `idx_email` (`email`),
  KEY `idx_expires_at` (`expires_at`),
  CONSTRAINT `fk_token_user` FOREIGN KEY (`email`) REFERENCES `usuarios` (`email`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
