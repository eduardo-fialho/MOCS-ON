# Módulo Web (CDU: Esqueceu a Senha)

Este módulo implementa o caso de uso “Esqueceu a senha” usando HTML/CSS/JS, PHP e MySQL. O envio de e‑mail é feito via Python (SMTP), sem MTA local.

## Fluxo de Funcionamento

1. Usuário acessa `login.html` e clica em “Esqueceu sua senha?”.
2. Em `forgot_password.php` (POST):
   - Normalizo o e‑mail (`lowercase`).
   - Gero `token` aleatório, salvo apenas o `hash` (SHA‑256) com expiração em `password_reset_tokens`.
   - Monto link `reset_password.php?token=...` e envio por e‑mail via `send_mail.py` (SMTP). Se o envio falhar, registro o link no log (fallback de desenvolvimento).
   - Exibo mensagem genérica: “Se o e‑mail estiver cadastrado, enviaremos instruções...”.
3. Em `reset_password.php`:
   - Valido `token` (hash na tabela, expiração, não utilizado).
   - Exibo formulário para nova senha; ao enviar, valido força mínima e igualdade.
   - Atualizo a senha do usuário com `password_hash()` e marco o token como usado (`used_at`).
   - Exibo confirmação e link para `login.html`.

Decisões de segurança: mensagens genéricas (não expõem existência de conta), hash do token no banco, expiração/uso único, match case‑insensitive de e‑mail, e normalização para minúsculas.

## Requisitos

- PHP com PDO MySQL habilitado (`pdo_mysql`) e `exec()` permitido.
- Python instalado e acessível no PATH (ou configure `PYTHON_BIN`).
- MySQL acessível (XAMPP recomendado no Windows).

## Configuração

1. Banco de dados:
   - Crie o banco (ex.: `mocsdb`).
   - Execute `db/db_schema.sql` para criar `password_reset_tokens`.
   - Garanta sua tabela de usuários e ajuste as constantes no `web/config.php`:
     - `USERS_TABLE`, `USERS_EMAIL_COLUMN`, `USERS_PASSWORD_COLUMN`.
2. App/base e DB em `web/config.php`:
   - `APP_BASE_URL` (ex.: `http://localhost/mocsON-views-pvd-main/web`).
   - `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASS`.
   - `PYTHON_BIN` se necessário (ex.: `C:\\Python311\\python.exe`).
3. Variáveis de ambiente SMTP (sem JSON):
   - `SMTP_HOST`, `SMTP_PORT` (587 recomendado), `SMTP_USERNAME`, `SMTP_PASSWORD`.
   - `SMTP_FROM_EMAIL`, `SMTP_FROM_NAME`.
   - `SMTP_USE_STARTTLS=1`, `SMTP_USE_SSL=0` (ou conforme o provedor).

No XAMPP, defina como variáveis de sistema do Windows ou via `httpd-vhosts.conf` com `SetEnv`.

## Teste Rápido

1. Acesse `http://localhost/mocsON-views-pvd-main/web/login.html`.
2. Clique em “Esqueceu sua senha?”, informe o e‑mail do usuário de teste.
3. Se o e‑mail não sair, verifique o log do Apache e pegue o `RESET_LINK` (fallback):
   - Windows/XAMPP: `C:\xampp\apache\logs\error.log`.
4. Abra `reset_password.php?token=...`, defina a nova senha e valide o login.

## Estrutura de Pastas

```
web/                   # Arquivos servidos (HTML/PHP/Python)
  config.php
  login.html
  forgot_password.php
  reset_password.php
  send_mail.py
db/
  db_schema.sql        # Tabela de tokens (ajuste sua tabela de usuários)
```

## Pendências (recomendadas)

- Limpeza periódica de tokens expirados (cron/Agendador de Tarefas).
- Rate‑limit por IP/e‑mail para evitar abuso.
- CSRF token em `reset_password.php`.
- Template HTML de e‑mail e remetente de domínio com SPF/DKIM/DMARC.

## Intenção do Módulo

Entregar um fluxo completo e seguro de recuperação de senha, isolado do restante do backend, com integração mínima (tabela de usuários e credenciais SMTP), para ser plugado ao sistema maior do MOCS ON sem introduzir novas dependências além de Python padrão.

