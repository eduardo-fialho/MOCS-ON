<?php
require_once __DIR__ . '/config.php';

function find_valid_token(PDO $pdo, string $token): ?array {
    $hash = hash('sha256', $token);
    $stmt = $pdo->prepare('SELECT * FROM password_reset_tokens WHERE token_hash = ? LIMIT 1');
    $stmt->execute([$hash]);
    $row = $stmt->fetch();
    if (!$row) return null;
    // Verifica expiração e se já foi usado
    if (!empty($row['used_at'])) return null;
    if (!empty($row['expires_at']) && (new DateTimeImmutable($row['expires_at'])) < new DateTimeImmutable('now')) return null;
    return $row;
}

function mark_token_used(PDO $pdo, int $id): void {
    $stmt = $pdo->prepare('UPDATE password_reset_tokens SET used_at = NOW() WHERE id = ?');
    $stmt->execute([$id]);
}

function update_user_password(PDO $pdo, string $email, string $passwordHash): void {
    // Usa constantes configuráveis para refletir seu schema real
    $table   = USERS_TABLE;
    $colMail = USERS_EMAIL_COLUMN;
    $colPass = USERS_PASSWORD_COLUMN;

    // Monta SQL com nomes fixos (vindos de constantes) e valores parametrizados
    // Case-insensitive match por segurança contra diferenças de caixa
    $sql = sprintf('UPDATE `%s` SET `%s` = ? WHERE LOWER(`%s`) = LOWER(?)', $table, $colPass, $colMail);
    $stmt = $pdo->prepare($sql);
    $stmt->execute([$passwordHash, $email]);
}

$method = $_SERVER['REQUEST_METHOD'] ?? 'GET';
$token = $_GET['token'] ?? ($_POST['token'] ?? '');
$token = is_string($token) ? trim($token) : '';

$state = [
    'valid' => false,
    'changed' => false,
    'error' => null,
];

try {
    $pdo = db_connect();
    if ($token !== '') {
        $row = find_valid_token($pdo, $token);
        if ($row) {
            $state['valid'] = true;

            if ($method === 'POST') {
                $password = $_POST['password'] ?? '';
                $confirm  = $_POST['confirm'] ?? '';

                if (strlen($password) < 8) {
                    $state['error'] = 'A senha deve ter pelo menos 8 caracteres.';
                } elseif ($password !== $confirm) {
                    $state['error'] = 'As senhas não coincidem.';
                } else {
                    $hash = password_hash($password, PASSWORD_DEFAULT);
                    update_user_password($pdo, $row['email'], $hash);
                    mark_token_used($pdo, (int)$row['id']);
                    $state['changed'] = true;
                }
            }
        }
    }
} catch (Throwable $e) {
    // error_log($e->getMessage());
    $state['valid'] = false;
}
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Redefinir senha</title>
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
</head>
<body class="bg-gray-100 flex items-center justify-center min-h-screen">
    <main class="w-full max-w-md mx-auto p-6">
        <div class="bg-white p-8 rounded-2xl shadow-lg w-full">
            <div class="text-center mb-6">
                <a href="login.html" class="inline-flex items-center text-mocs-blue hover:underline text-sm"><i class="fa fa-arrow-left mr-2"></i>Voltar</a>
                <h1 class="text-2xl font-bold text-mocs-blue mt-2">Redefinir senha</h1>
            </div>

            <?php if (!$state['valid']): ?>
                <div class="bg-red-50 text-red-800 border border-red-200 p-4 rounded-md mb-4">
                    Link inválido ou expirado. Solicite uma nova redefinição.
                </div>
                <a href="forgot_password.php" class="block text-center text-mocs-blue hover:underline">Solicitar novamente</a>
            <?php elseif ($state['changed']): ?>
                <div class="bg-green-50 text-green-800 border border-green-200 p-4 rounded-md mb-4">
                    Senha alterada com sucesso. Você já pode entrar com a nova senha.
                </div>
                <a href="login.html" class="block text-center text-white bg-mocs-blue py-2.5 rounded-lg hover:bg-opacity-90">Ir para o login</a>
            <?php else: ?>
                <?php if (!empty($state['error'])): ?>
                    <div class="bg-red-50 text-red-800 border border-red-200 p-3 rounded-md mb-3">
                        <?= htmlspecialchars($state['error']) ?>
                    </div>
                <?php endif; ?>
                <form method="POST" action="reset_password.php">
                    <input type="hidden" name="token" value="<?= htmlspecialchars($token) ?>">
                    <div class="mb-4">
                        <label for="password" class="block text-sm font-medium text-gray-700 mb-1">Nova senha</label>
                        <input type="password" id="password" name="password" required minlength="8" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-mocs-blue" placeholder="Mínimo 8 caracteres">
                    </div>
                    <div class="mb-6">
                        <label for="confirm" class="block text-sm font-medium text-gray-700 mb-1">Confirmar senha</label>
                        <input type="password" id="confirm" name="confirm" required minlength="8" class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-mocs-blue" placeholder="Repita a senha">
                    </div>
                    <button type="submit" class="w-full bg-mocs-blue text-white font-semibold py-2.5 rounded-lg hover:bg-opacity-90 transition">Salvar nova senha</button>
                </form>
            <?php endif; ?>
        </div>
    </main>
</body>
</html>
