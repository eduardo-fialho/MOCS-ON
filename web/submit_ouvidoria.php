<?php
require_once __DIR__ . '/config.php';

// Verifica se é um método POST
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

    header('Location: ouvidoria.html?success=1');
    exit;

} catch (Throwable $e) {
    header('Location: ouvidoria.html?error=2');
    exit;
}