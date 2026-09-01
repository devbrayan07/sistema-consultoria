-- ========================================================
-- 1. INSERÇÃO DE USUÁRIOS
-- Senha padrão para os usuários abaixo: 123456
-- ========================================================

INSERT INTO usuario (
    nome,
    email,
    senha,
    tipo,
    ativo
)
VALUES
    (
        'Jorge Contador Principal',
        'contador@consultoria.com',
        '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iC.s2JGu',
        'CONTADOR',
        true
    ),
    (
        'Ana Paula Equipe',
        'ana.equipe@consultoria.com',
        '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iC.s2JGu',
        'CONTADOR',
        true
    ),
    (
        'Carlos Cliente Silva',
        'carlos.cliente@empresa.com',
        '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xD0m1bC.iC.s2JGu',
        'USUARIO',
        true
    )
    ON DUPLICATE KEY UPDATE
                         nome = VALUES(nome),
                         senha = VALUES(senha),
                         tipo = VALUES(tipo),
                         ativo = VALUES(ativo);


-- ========================================================
-- 2. INSERÇÃO DE EMPRESA
-- Vinculada ao usuário cliente
-- ========================================================

INSERT INTO empresa (
    razao_social,
    nome_fantasia,
    cnpj,
    regime_tributario,
    id_usuario_cliente
)
VALUES
    (
        'Silva e Santos Comércio LTDA',
        'Silva Tech',
        '12345678000199',
        'SIMPLES_NACIONAL',
        3
    )
    ON DUPLICATE KEY UPDATE
                         razao_social = VALUES(razao_social),
                         nome_fantasia = VALUES(nome_fantasia),
                         regime_tributario = VALUES(regime_tributario),
                         id_usuario_cliente = VALUES(id_usuario_cliente);


-- ========================================================
-- 3. VÍNCULO EMPRESA RESPONSÁVEL
-- Empresa 1 vinculada ao contador 1
-- ========================================================

INSERT INTO empresa_responsavel (
    id_empresa,
    id_usuario
)
VALUES
    (
        1,
        1
    )
    ON DUPLICATE KEY UPDATE
                         id_empresa = VALUES(id_empresa),
                         id_usuario = VALUES(id_usuario);


-- ========================================================
-- 4. INSERÇÃO DE DOCUMENTOS
-- ========================================================

INSERT INTO documento (
    tipo,
    nome_arquivo,
    url_arquivo,
    competencia,
    enviado_por,
    id_empresa
)
VALUES
    (
        'NOTA_FISCAL',
        'NF_1042_JULHO.pdf',
        'https://storage.consultoria.com/docs/NF_1042_JULHO.pdf',
        '2026-07-01',
        3,
        1
    ),
    (
        'DECLARACAO',
        'DEFIS_2025.pdf',
        'https://storage.consultoria.com/docs/DEFIS_2025.pdf',
        '2025-12-31',
        1,
        1
    )
    ON DUPLICATE KEY UPDATE
                         tipo = VALUES(tipo),
                         nome_arquivo = VALUES(nome_arquivo),
                         url_arquivo = VALUES(url_arquivo),
                         competencia = VALUES(competencia),
                         enviado_por = VALUES(enviado_por),
                         id_empresa = VALUES(id_empresa);


-- ========================================================
-- 5. INSERÇÃO DE OBRIGAÇÕES FISCAIS
-- ========================================================

INSERT INTO obrigacao_fiscal (
    tipo,
    competencia,
    data_vencimento,
    id_empresa,
    valor,
    status,
    honorario
)
VALUES
    (
        'DAS',
        '2026-07-01',
        '2026-08-20',
        1,
        1250.50,
        'PENDENTE',
        350.00
    ),
    (
        'DARF',
        '2026-06-01',
        '2026-07-25',
        1,
        450.00,
        'PAGA',
        350.00
    )
    ON DUPLICATE KEY UPDATE
                         tipo = VALUES(tipo),
                         competencia = VALUES(competencia),
                         data_vencimento = VALUES(data_vencimento),
                         id_empresa = VALUES(id_empresa),
                         valor = VALUES(valor),
                         status = VALUES(status),
                         honorario = VALUES(honorario);


-- ========================================================
-- 6. INSERÇÃO DE RELATÓRIO FINANCEIRO
-- ========================================================

INSERT INTO relatorio_financeiro (
    periodo,
    receita,
    despesa,
    impostos_pagos,
    gerado_por,
    id_empresa
)
VALUES
    (
        '2026-06-01',
        45000.00,
        18000.00,
        3200.00,
        1,
        1
    )
    ON DUPLICATE KEY UPDATE
                         periodo = VALUES(periodo),
                         receita = VALUES(receita),
                         despesa = VALUES(despesa),
                         impostos_pagos = VALUES(impostos_pagos),
                         gerado_por = VALUES(gerado_por),
                         id_empresa = VALUES(id_empresa);


-- ========================================================
-- 7. INSERÇÃO DE NOTIFICAÇÃO
-- ========================================================

INSERT INTO notificacao (
    mensagem,
    id_usuario,
    lida,
    referencia_tipo,
    id_referencia
)
VALUES
    (
        'O imposto DAS referente à competência 07/2026 está pendente de pagamento.',
        3,
        false,
        'OBRIGACAO',
        1
    )
    ON DUPLICATE KEY UPDATE
                         mensagem = VALUES(mensagem),
                         id_usuario = VALUES(id_usuario),
                         lida = VALUES(lida),
                         referencia_tipo = VALUES(referencia_tipo),
                         id_referencia = VALUES(id_referencia);