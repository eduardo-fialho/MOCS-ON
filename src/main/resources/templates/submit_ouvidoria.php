<?php
require_once __DIR__ . '/config.php';

if (($_SERVER['REQUEST_METHOD'] ?? 'GET') !== 'POST') {
    header('Location: ouvidoria.html');
    exit;
}

function post_or_null(string $key): ?string {
    $value = trim($_POST[$key] ?? '');
    return $value === '' ? null : $value;
}

$identificacao = post_or_null('identificacao');
$categoria_relato = post_or_null('categoria_relato');

if (empty($identificacao) || empty($categoria_relato)) {
    header('Location: ouvidoria.html?error=1');
    exit;
}

try {
    $pdo = db_connect(); //

    $sql = "INSERT INTO ouvidoria_relatos (
                identificacao, nome_relator, comite_relator, 
                categoria_relato, 
                comite_conducao, comite_respeito, comite_imparcialidade, comite_apoio, comite_mensagem, 
                secretariado_positivos, secretariado_negativos, secretariado_falta, secretariado_sugestoes, 
                outros_mensagem, 
                status, created_at
            ) VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'novo', NOW()
            )";

    $params = [
        $identificacao,
        post_or_null('nome_relator'),
        post_or_null('comite_relator'),

        $categoria_relato,

        post_or_null('comite_conducao'),
        post_or_null('comite_respeito'),
        post_or_null('comite_imparcialidade'),
        post_or_null('comite_apoio'),
        post_or_null('comite_mensagem'),

        post_or_null('secretariado_positivos'),
        post_or_null('secretariado_negativos'),
        post_or_null('secretariado_falta'),
        post_or_null('secretariado_sugestoes'),

        post_or_null('outros_mensagem')
    ];

    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);

    try {
        if (file_exists(__DIR__ . '/vendor/autoload.php')) {
            require __DIR__ . '/vendor/autoload.php';
            $mail = new PHPMailer\PHPMailer\PHPMailer(true);

            $mail->isSMTP();
            $mail->Host = 'smtp.gmail.com';
            $mail->SMTPAuth = true;
            $mail->Username = 'projectmocs@gmail.com';
            $mail->Password = 'zbfx tnjs igdj rdpw';
            $mail->SMTPSecure = PHPMailer\PHPMailer\PHPMailer::ENCRYPTION_STARTTLS;
            $mail->Port = 587;
            $mail->CharSet = 'UTF-8';

            $mail->setFrom('projectmocs@gmail.com', 'MOCS ON - Ouvidoria');
            $mail->addAddress('projectmocs@gmail.com');

            $mail->isHTML(true);
            $mail->Subject = 'Novo relato de Ouvidoria - MOCS ON';

            $bodyParts = [];
            $bodyParts[] = "<p><strong>Identificação:</strong> " . htmlspecialchars($identificacao) . "</p>";
            if ($params[1]) $bodyParts[] = "<p><strong>Nome do relator:</strong> " . htmlspecialchars($params[1]) . "</p>";
            if ($params[2]) $bodyParts[] = "<p><strong>Comitê do relator:</strong> " . htmlspecialchars($params[2]) . "</p>";
            $bodyParts[] = "<p><strong>Categoria:</strong> " . htmlspecialchars($categoria_relato) . "</p>";

            $fieldsMap = [
                'comite_conducao' => $params[4],
                'comite_respeito' => $params[5],
                'comite_imparcialidade' => $params[6],
                'comite_apoio' => $params[7],
                'comite_mensagem' => $params[8],
                'secretariado_positivos' => $params[9],
                'secretariado_negativos' => $params[10],
                'secretariado_falta' => $params[11],
                'secretariado_sugestoes' => $params[12],
                'outros_mensagem' => $params[13],
            ];

            foreach ($fieldsMap as $label => $val) {
                if ($val !== null) {
                    $labelText = ucwords(str_replace(['_', '-'], ' ', $label));
                    $bodyParts[] = "<p><strong>{$labelText}:</strong><br>" . nl2br(htmlspecialchars($val)) . "</p>";
                }
            }

            $mail->Body = implode("\n", $bodyParts);
            $mail->AltBody = strip_tags(str_replace(['<br>', '<br/>', '<br />'], "\n", $mail->Body));

            $mail->send();
        }
    } catch (Throwable $e) {
        error_log('Mail error: ' . $e->getMessage());
    }

    header('Location: ouvidoria.html?success=1');
    exit;

} catch (Throwable $e) {
    header('Location: ouvidoria.html?error=2');
    exit;
}
?>