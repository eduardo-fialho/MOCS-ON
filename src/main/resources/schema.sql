CREATE DATABASE IF NOT EXISTS mocson
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mocson;

CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `nome` VARCHAR(255) NOT NULL,
  `email` VARCHAR(255) NOT NULL UNIQUE,
  `senha` VARCHAR(255) NOT NULL,
  `tipo` VARCHAR(50) NOT NULL DEFAULT 'DELEGADO',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `profile_photo` LONGBLOB NULL,
  `profile_photo_content_type` VARCHAR(100) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_usuarios_tipo` (`tipo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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

CREATE TABLE IF NOT EXISTS `avisos` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `autor` varchar(200) NOT NULL,
    `titulo` varchar(200) NOT NULL,
    `mensagem` varchar(10000) NOT NULL,
    `data` datetime NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `documentos` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(255) NOT NULL,
    `autor` VARCHAR(255) NOT NULL,
    `arquivo` LONGBLOB NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'EM ENVIO',
    `ativo` BOOLEAN NOT NULL DEFAULT TRUE,
    `data` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `avaliacao` VARCHAR(1000) NOT NULL,

    PRIMARY KEY (`id`),
    KEY `idx_documentos_status` (`status`),
    KEY `idx_documentos_ativo` (`ativo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `posts` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `autor` VARCHAR(255) NOT NULL,
  `mensagem` TEXT NOT NULL,
  `data` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PUBLICO',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `post_reactions` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `usuario` VARCHAR(255) NOT NULL,
  `emoji` VARCHAR(50) NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_post_reaction_post` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`) ON DELETE CASCADE,
  UNIQUE KEY `ux_post_user` (`post_id`, `usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `post_comments` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `usuario` VARCHAR(255) NOT NULL,
  `usuario_nome` VARCHAR(255) NULL,
  `mensagem` TEXT NOT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) NULL,
  PRIMARY KEY (`id`),
  KEY `idx_post_comments_post_id` (`post_id`),
  CONSTRAINT `fk_post_comment_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `posts` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `post_curtidas` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL,
  `usuario` VARCHAR(255) NOT NULL,
  `usuario_nome` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_post_usuario` (`post_id`, `usuario`),
  KEY `idx_curtidas_post_id` (`post_id`),
  CONSTRAINT `fk_curtida_post`
    FOREIGN KEY (`post_id`)
    REFERENCES `posts` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `pre_registrations` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `nome` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `instituicao` VARCHAR(255) NULL,
    `telefone` VARCHAR(100) NULL,
    `comite_preferido` VARCHAR(255) NULL,
    `mensagem` TEXT NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    `secretariado_notified` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `processed_at` DATETIME NULL,
    `processed_by` VARCHAR(255) NULL,
    PRIMARY KEY (`id`),
    KEY `idx_pre_reg_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `relato_ouvidoria` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `criado_em` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `resolvido_em` DATETIME NULL,
  `ativo` BOOLEAN NOT NULL DEFAULT TRUE,
  `status` VARCHAR(50) NOT NULL,
  `autor` VARCHAR(255) NOT NULL,
  `assunto` VARCHAR(255) NOT NULL,
  `relato` TEXT NOT NULL,
  `ouvidor` VARCHAR(255) NULL,
  `resposta` TEXT NULL,

  PRIMARY KEY (`id`),
  KEY `idx_ouvidoria_status` (`status`),
  KEY `idx_ouvidoria_ativo` (`ativo`),
  KEY `idx_ouvidoria_autor` (`autor`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `agenda_diaria` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `titulo` VARCHAR(255) NOT NULL,
    `descricao` VARCHAR(255) NOT NULL,
    `data_evento` DATE NOT NULL,
    `hora_evento` TIME NOT NULL,
    `tipo` VARCHAR(255) DEFAULT 'GERAL',
    `visivel` BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `comites` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT,
    `nome` varchar(200) NOT NULL,
    `sigla` varchar(200) NOT NULL,
    `num_delegados` BIGINT NOT NULL,
    `descricao` VARCHAR(500) NOT NULL,
    `status` VARCHAR(50) NOT NULL DEFAULT 'EM_ANDAMENTO',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `materias` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    `titulo` VARCHAR(255) NOT NULL,
    `lead` VARCHAR(500) NOT NULL,
    `texto` TEXT NOT NULL,

    `imagem` LONGBLOB NULL,

    `autor` VARCHAR(255) NOT NULL,
    `revisor` VARCHAR(255) NULL,

    `comite_id` BIGINT UNSIGNED NULL,

    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    `ativo` BOOLEAN NOT NULL DEFAULT TRUE,

    `data_criacao` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `data_edicao` DATETIME NULL,
    `data_aprovacao` DATETIME NULL,

    PRIMARY KEY (`id`),

    KEY `idx_materias_status` (`status`),
    KEY `idx_materias_autor` (`autor`),
    KEY `idx_materias_comite` (`comite_id`),
    KEY `idx_materias_ativo` (`ativo`),

    CONSTRAINT `fk_materia_comite`
        FOREIGN KEY (`comite_id`)
        REFERENCES `comites` (`id`)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `materia_logs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

    `materia_id` BIGINT UNSIGNED NOT NULL,
    `acao` VARCHAR(50) NOT NULL,

    `usuario` VARCHAR(255) NOT NULL,
    `descricao` VARCHAR(1000) NULL,

    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    KEY `idx_materia_log_materia` (`materia_id`),

    CONSTRAINT `fk_materia_log_materia`
        FOREIGN KEY (`materia_id`)
        REFERENCES `materias` (`id`)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `guia_estudos` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `autor` VARCHAR(255) NOT NULL,
  `titulo` VARCHAR(255) NOT NULL,
  `conteudo` TEXT NOT NULL,
  `regras` TEXT NOT NULL,
  `arquivo` LONGBLOB NULL,
  `data` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `atualizado_em` DATETIME NULL,
  `oficial` BOOLEAN NOT NULL DEFAULT FALSE,
  `ativo` BOOLEAN NOT NULL DEFAULT TRUE,
  `id_comite` BIGINT UNSIGNED NOT NULL,

  PRIMARY KEY (`id`),
  KEY `idx_guia_estudos_autor` (`autor`),
  KEY `idx_guia_estudos_oficial` (`oficial`),
  KEY `idx_guia_estudos_ativo` (`ativo`),
  KEY `idx_guia_estudos_comite` (`id_comite`),

  CONSTRAINT `fk_guia_estudos_comite`
    FOREIGN KEY (`id_comite`)
    REFERENCES `comites` (`id`)
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `presenca_listas` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `comite_id` BIGINT UNSIGNED NULL,
    `titulo` VARCHAR(255) NOT NULL,
    `data_sessao` DATE NOT NULL,
    `hora_inicio` TIME NULL,
    `hora_fim` TIME NULL,
    `observacao` VARCHAR(500) NULL,
    `criado_por` VARCHAR(255) NULL,
    `criado_em` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_presenca_comite` (`comite_id`),
    KEY `idx_presenca_data` (`data_sessao`),
    CONSTRAINT `fk_presenca_comite` FOREIGN KEY (`comite_id`) REFERENCES `comites` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `presenca_registros` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `lista_id` BIGINT UNSIGNED NOT NULL,
    `usuario_id` INT UNSIGNED NULL,
    `usuario_nome` VARCHAR(255) NOT NULL,
    `usuario_email` VARCHAR(255) NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'AUSENTE',
    `observacao` VARCHAR(500) NULL,
    `registrado_em` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `atualizado_em` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_presenca_lista_usuario` (`lista_id`, `usuario_id`),
    KEY `idx_presenca_lista` (`lista_id`),
    CONSTRAINT `fk_presenca_lista` FOREIGN KEY (`lista_id`) REFERENCES `presenca_listas` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_presenca_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `link_guia` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `id_guia` BIGINT UNSIGNED NOT NULL,
    `link` VARCHAR(1000) NOT NULL,
    `ativo` BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (`id`),
    KEY `idx_link_guia_id_guia` (`id_guia`),
    KEY `idx_link_guia_ativo` (`ativo`),

    CONSTRAINT `fk_link_guia_guia_estudos`
        FOREIGN KEY (`id_guia`)
        REFERENCES `guia_estudos` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `user_profiles` (
    `user_id` INT UNSIGNED NOT NULL,
    `instituicao` VARCHAR(255) NULL,
    `telefone` VARCHAR(100) NULL,
    `comite_preferido` VARCHAR(255) NULL,
    `observacoes` TEXT NULL,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`user_id`),

    CONSTRAINT `fk_user_profiles_user` 
      FOREIGN KEY (`user_id`) 
      REFERENCES `usuarios` (`id`) 
      ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `consulta` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `titulo` VARCHAR(255) NOT NULL,
  `pergunta` VARCHAR(255) NOT NULL,
  `status` ENUM('PENDENTE', 'APROVADA', 'REJEITADA', 'ARQUIVADA') NOT NULL,
  `ativo` BOOLEAN NOT NULL DEFAULT true,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `approved_at` TIMESTAMP NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS `consulta_votos` (
  `id` INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  `consulta_id` INT UNSIGNED NOT NULL,
  `usuario_username` VARCHAR(255) NOT NULL,
  `voto` ENUM('SIM', 'NAO') NOT NULL,

  UNIQUE (`consulta_id`, `usuario_username`),

  FOREIGN KEY (`consulta_id`)
    REFERENCES consulta(id)
    ON DELETE CASCADE
);

CREATE TABLE usuario_comites (
  usuario_id INT UNSIGNED NOT NULL,
  comite_id BIGINT UNSIGNED NOT NULL,

  PRIMARY KEY (usuario_id, comite_id),

  CONSTRAINT fk_uc_usuario
    FOREIGN KEY (usuario_id)
    REFERENCES usuarios(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_uc_comite
    FOREIGN KEY (comite_id)
    REFERENCES comites(id)
    ON DELETE CASCADE
);