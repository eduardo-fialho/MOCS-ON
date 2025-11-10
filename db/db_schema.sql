-- Tabela para tokens de redefinição de senha
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
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Exemplo de tabela de usuários (AJUSTE ao seu schema real)
-- CREATE TABLE `users` (
--   `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
--   `email` VARCHAR(255) NOT NULL UNIQUE,
--   `password_hash` VARCHAR(255) NOT NULL,
--   PRIMARY KEY (`id`)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================
-- Tabela de ouvidoria_relatos
-- ==========================
DROP TABLE IF EXISTS `ouvidoria_relatos`;

CREATE TABLE IF NOT EXISTS `ouvidoria_relatos` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(50) NOT NULL DEFAULT 'novo',

  `identificacao` VARCHAR(50) NOT NULL, -- 'anonimo', 'comite_apenas', 'comite_e_nome'
  `nome_relator` VARCHAR(255) NULL,
  `comite_relator` VARCHAR(255) NULL,

  `categoria_relato` VARCHAR(100) NOT NULL, -- 'comite', 'secretariado', 'outros'

  `comite_conducao` VARCHAR(50) NULL,
  `comite_respeito` VARCHAR(50) NULL,
  `comite_imparcialidade` VARCHAR(50) NULL,
  `comite_apoio` VARCHAR(50) NULL,
  `comite_mensagem` TEXT NULL,
  
  `secretariado_positivos` TEXT NULL,
  `secretariado_negativos` TEXT NULL,
  `secretariado_falta` TEXT NULL,
  `secretariado_sugestoes` TEXT NULL,

  `outros_mensagem` TEXT NULL,

  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_categoria_relato` (`categoria_relato`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;