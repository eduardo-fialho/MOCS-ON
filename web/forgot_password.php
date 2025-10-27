<?php
/*O que você precisa configurar

Ajustar config.php com as credenciais do seu MySQL.
Criar a tabela executando db_schema.sql no banco.
Ajustar a query de atualização de senha em reset_password.php: update_user_password() para refletir sua tabela real de usuários.
Configurar envio de e‑mail no servidor (ex.: SMTP autenticado). O mail() local pode não funcionar sem MTA.*/
require_once __DIR__ . '/config.php';

$isPost = ($_SERVER['REQUEST_METHOD'] ?? 'GET') === 'POST';
$submitted = false;
$error = null;

if ($isPost) {
    $email = strtolower(trim($_POST['email'] ?? ''));
    $submitted = true;

    // Validação simples de e-mail
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        // Mantém resposta genérica para não expor usuários
        $error = 'invalid_email';
    } else {
        try {
            $pdo = db_connect();

            // Gera token aleatório e hash armazenável
            $token = bin2hex(random_bytes(32));
            $tokenHash = hash('sha256', $token);
            $expiresAt = (new DateTimeImmutable('+1 hour'))->format('Y-m-d H:i:s');

            // Armazena token (associa por e-mail). Ajuste o schema conforme necessário.
            $stmt = $pdo->prepare('INSERT INTO password_reset_tokens (email, token_hash, expires_at, created_at, ip, user_agent) VALUES (?, ?, ?, NOW(), ?, ?)');
            $ip = $_SERVER['REMOTE_ADDR'] ?? null;
            $ua = $_SERVER['HTTP_USER_AGENT'] ?? null;
            $stmt->execute([$email, $tokenHash, $expiresAt, $ip, $ua]);

            // Monta link de redefinição
            $base = rtrim(APP_BASE_URL, '/');
            $link = $base . '/reset_password.php?token=' . urlencode($token);

            // Envio de e-mail via Python SMTP (sem MTA)
            $subject = 'Instruções para redefinição de senha';
            $message = "Olá,\n\nRecebemos uma solicitação para redefinir sua senha.\n\nAcesse o link abaixo para continuar (válido por 1 hora):\n$link\n\nSe você não solicitou, ignore este e-mail.";

            // Caminho para arquivo temporário com o corpo do e-mail
            $tmpFile = tempnam(sys_get_temp_dir(), 'mocs_mail_');
            file_put_contents($tmpFile, $message);

            $python = escapeshellarg(PYTHON_BIN);
            $script = escapeshellarg(PYTHON_MAIL_SCRIPT);
            $to = escapeshellarg($email);
            $subj = escapeshellarg($subject);
            $bodyFile = escapeshellarg($tmpFile);

            // Define variáveis de ambiente SMTP_* se estiverem configuradas no servidor (opcional)
            // Ex.: putenv('SMTP_HOST=smtp.gmail.com'); putenv('SMTP_USERNAME=...'); putenv('SMTP_PASSWORD=...');
            // Aqui definimos apenas um nome de remetente padrão; demais variáveis devem ser configuradas no ambiente.
            if (!getenv('SMTP_FROM_NAME')) {
                putenv('SMTP_FROM_NAME=MOCS ON');
            }

            // Chamada sem --config: o Python lerá SMTP_* do ambiente
            $cmd = "$python $script --to $to --subject $subj --text-file $bodyFile";
            $output = null; $ret = 0;
            // Em alguns ambientes, exec pode estar desabilitado. Nesses casos, apenas registramos o link no log.
            if (function_exists('exec')) {
                @exec($cmd . ' 2>&1', $output, $ret);
            } else {
                $ret = 1; // força fallback
            }

            if ($ret !== 0) {
                // Fallback de desenvolvimento: registra o link no log
                error_log('RESET_LINK: ' . $link);
            }

            @unlink($tmpFile);

        } catch (Throwable $e) {
            // Log opcional: error_log($e->getMessage());
            // Mantém resposta genérica
        }
    }
}
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Esqueceu sua senha</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
    <style>
        body { font-family: 'Inter', sans-serif; }
        .bg-mocs-blue { background-color: #205395; }
        .text-mocs-blue { color: #205395; }
    </style>
    <meta http-equiv="Cache-Control" content="no-store" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="0" />
    <?php if ($submitted): ?>
    <meta http-equiv="refresh" content="60"> <!-- evita reenvio acidental -->
    <?php endif; ?>
    </head>
<body class="bg-gray-100 flex items-center justify-center min-h-screen">
    <main class="w-full max-w-md mx-auto p-6">
        <div class="bg-white p-8 rounded-2xl shadow-lg w-full">
            <div class="text-center mb-6">
                <a href="login.html" class="inline-flex items-center text-mocs-blue hover:underline text-sm"><i class="fa fa-arrow-left mr-2"></i>Voltar</a>
                <h1 class="text-2xl font-bold text-mocs-blue mt-2">Recuperar senha</h1>
                <p class="text-gray-600">Digite seu e-mail para receber instruções.</p>
            </div>

            <?php if ($submitted): ?>
                <div class="bg-blue-50 text-blue-800 border border-blue-200 p-4 rounded-md mb-4">
                    Se o e-mail estiver cadastrado, enviaremos instruções para redefinição de senha.
                </div>
            <?php endif; ?>

            <form method="POST" action="forgot_password.php" novalidate>
                <div class="mb-4">
                    <label for="email" class="block text-sm font-medium text-gray-700 mb-1">E-mail</label>
                    <input type="email" id="email" name="email" required class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-mocs-blue" placeholder="seu@email.com">
                    <?php if ($isPost && $error === 'invalid_email'): ?>
                        <p class="mt-1 text-sm text-red-600">Informe um e-mail válido.</p>
                    <?php endif; ?>
                </div>
                <button type="submit" class="w-full bg-mocs-blue text-white font-semibold py-2.5 rounded-lg hover:bg-opacity-90 transition">
                    Enviar instruções
                </button>
            </form>
        </div>
    </main>
</body>
</html>
