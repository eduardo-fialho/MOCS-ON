<?php
// Configuração de conexão com MySQL (AJUSTAR DEPOIS GALERA)
define('DB_HOST', 'localhost');
define('DB_NAME', 'mocsdb');
define('DB_USER', 'root');
define('DB_PASS', '');

// URL base da aplicação para montar links em e-mails (AJUSTAAR DEPOIS GALERA)
// Ex.: http://localhost/mocsON-views-pvd-main/web
define('APP_BASE_URL', 'http://localhost/mocsON-views-pvd-main/web');

function db_connect(): PDO {
    $dsn = 'mysql:host=' . DB_HOST . ';dbname=' . DB_NAME . ';charset=utf8mb4';
    $options = [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ];
    return new PDO($dsn, DB_USER, DB_PASS, $options);
}

// Ajustes do schema de usuários (altere conforme seu banco)
if (!defined('USERS_TABLE')) {
    define('USERS_TABLE', 'users');
}
if (!defined('USERS_EMAIL_COLUMN')) {
    define('USERS_EMAIL_COLUMN', 'email');
}
if (!defined('USERS_PASSWORD_COLUMN')) {
    define('USERS_PASSWORD_COLUMN', 'password_hash');
}

// Caminho do Python e arquivos de suporte para envio SMTP sem MTA
// Ajuste se necessário (ex.: 'C:\\Python311\\python.exe' no Windows)
if (!defined('PYTHON_BIN')) {
    define('PYTHON_BIN', 'python');
}
if (!defined('PYTHON_MAIL_SCRIPT')) {
    define('PYTHON_MAIL_SCRIPT', __DIR__ . '/send_mail.py');
}
if (!defined('SMTP_CONFIG_PATH')) {
    // Copie smtp_config.example.json para smtp_config.json e preencha as credenciais
    define('SMTP_CONFIG_PATH', __DIR__ . '/smtp_config.json');
}
