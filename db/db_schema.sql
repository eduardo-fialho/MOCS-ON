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

-- Perfil detalhado para membros do secretariado
CREATE TABLE IF NOT EXISTS `secretariado_profiles` (
  `user_id` INT UNSIGNED NOT NULL,
  `funcao` VARCHAR(50) NOT NULL,
  `departamento` VARCHAR(255) NOT NULL,
  `matricula` VARCHAR(100) NULL,
  `telefone` VARCHAR(50) NULL,
  `turno_atendimento` VARCHAR(100) NULL,
  `responsabilidades` VARCHAR(255) NULL,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_secretariado_user` FOREIGN KEY (`user_id`) REFERENCES `usuarios` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE posts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  autor VARCHAR(255),
  mensagem TEXT,
  data TIMESTAMP
);

/*Como muito provavelmente muitos de vcs testaram sem essa coluna, recomendo rodar esse esquema de novo
com esse trechinho slq aqui:*/

ALTER TABLE posts ADD COLUMN status VARCHAR(20) DEFAULT 'PUBLICO';
UPDATE posts SET status = 'PUBLICO' WHERE status IS NULL;
/*Principalmente pra Alterar os status dos outros post pra público*/

CREATE TABLE post_reactions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  post_id BIGINT NOT NULL,
  usuario VARCHAR(255),
  emoji VARCHAR(50) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (post_id) REFERENCES posts(id),
  UNIQUE KEY ux_post_user_emoji (post_id, usuario, emoji)
);

CREATE TABLE `avisos` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `autor` varchar(200) NOT NULL,
    `titulo` varchar(200) NOT NULL,
    `mensagem` varchar(10000) NOT NULL,
    `data` datetime NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Ouvidoria
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