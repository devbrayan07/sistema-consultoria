document.addEventListener(
    'DOMContentLoaded',
    () => {

        if (
            !window.auth
                ?.validarProtecaoRota()
        ) {
            return;
        }


        const usuario =
            window.auth.getUsuario();


        configurarUsuario(
            usuario
        );

        configurarDataAtual();


        carregarDashboard();


        async function carregarDashboard() {

            const resultados =
                await Promise.allSettled([
                    window.api.get('/empresas'),
                    window.api.get('/obrigacoes'),
                    window.api.get('/documentos')
                ]);


            const empresas =
                resultados[0].status === 'fulfilled'
                && Array.isArray(
                    resultados[0].value
                )
                    ? resultados[0].value
                    : [];


            const obrigacoes =
                resultados[1].status === 'fulfilled'
                && Array.isArray(
                    resultados[1].value
                )
                    ? resultados[1].value
                    : [];


            const documentos =
                resultados[2].status === 'fulfilled'
                && Array.isArray(
                    resultados[2].value
                )
                    ? resultados[2].value
                    : [];


            atualizarIndicadores(
                empresas,
                obrigacoes,
                documentos
            );


            renderizarProximosVencimentos(
                obrigacoes
            );


            renderizarResumoOperacional(
                obrigacoes
            );


            renderizarDocumentosRecentes(
                documentos
            );
        }


        /*
         * =====================================================
         * USUÁRIO
         * =====================================================
         */

        function configurarUsuario(
            usuario
        ) {

            const elemento =
                document.getElementById(
                    'dashboard-user-name'
                );


            if (!elemento) {
                return;
            }


            const nome =
                usuario?.nome
                    ?.trim();


            if (!nome) {

                elemento.textContent =
                    'usuário';

                return;
            }


            elemento.textContent =
                nome.split(' ')[0];
        }


        /*
         * =====================================================
         * DATA ATUAL
         * =====================================================
         */

        function configurarDataAtual() {

            const elemento =
                document.getElementById(
                    'dashboard-current-date'
                );


            if (!elemento) {
                return;
            }


            const hoje =
                new Date();


            elemento.textContent =
                hoje.toLocaleDateString(
                    'pt-BR',
                    {
                        day: '2-digit',
                        month: 'long',
                        year: 'numeric'
                    }
                );
        }


        /*
         * =====================================================
         * INDICADORES
         * =====================================================
         */

        function atualizarIndicadores(
            empresas,
            obrigacoes,
            documentos
        ) {

            const hoje =
                inicioDoDia(
                    new Date()
                );


            const pendentes =
                obrigacoes.filter(
                    obrigacao =>
                        isPendente(
                            obrigacao
                        )
                );


            const atrasadas =
                obrigacoes.filter(
                    obrigacao =>
                        isAtrasada(
                            obrigacao,
                            hoje
                        )
                );


            definirTexto(
                'stat-empresas',
                empresas.length
            );


            definirTexto(
                'stat-obrigacoes-pendentes',
                pendentes.length
            );


            definirTexto(
                'stat-obrigacoes-atrasadas',
                atrasadas.length
            );


            definirTexto(
                'stat-documentos',
                documentos.length
            );


            const statusAtraso =
                document.getElementById(
                    'dashboard-atraso-status'
                );


            if (statusAtraso) {

                if (
                    atrasadas.length === 0
                ) {

                    statusAtraso.textContent =
                        'Tudo em dia';

                    statusAtraso.className =
                        'dashboard-stat-status status-ok';

                } else {

                    statusAtraso.textContent =
                        atrasadas.length === 1
                            ? '1 item requer atenção'
                            : `${atrasadas.length} itens requerem atenção`;

                    statusAtraso.className =
                        'dashboard-stat-status status-alert';
                }
            }
        }


        /*
         * =====================================================
         * PRÓXIMOS VENCIMENTOS
         * =====================================================
         */

        function renderizarProximosVencimentos(
            obrigacoes
        ) {

            const container =
                document.getElementById(
                    'lista-proximos-vencimentos'
                );


            if (!container) {
                return;
            }


            const hoje =
                inicioDoDia(
                    new Date()
                );


            const proximas =
                obrigacoes
                    .filter(
                        obrigacao => {

                            if (
                                !isPendente(
                                    obrigacao
                                )
                            ) {
                                return false;
                            }


                            const data =
                                converterData(
                                    obrigacao.dataVencimento
                                );


                            return (
                                data
                                &&
                                data >= hoje
                            );
                        }
                    )
                    .sort(
                        (
                            a,
                            b
                        ) => {

                            const dataA =
                                converterData(
                                    a.dataVencimento
                                );


                            const dataB =
                                converterData(
                                    b.dataVencimento
                                );


                            return (
                                dataA - dataB
                            );
                        }
                    )
                    .slice(
                        0,
                        6
                    );


            if (
                proximas.length === 0
            ) {

                container.innerHTML = `
                    <div class="dashboard-empty-state">

                        <div class="dashboard-empty-icon">
                            ✓
                        </div>

                        <strong>
                            Nenhum vencimento próximo
                        </strong>

                        <span>
                            Não há obrigações pendentes
                            com vencimento futuro.
                        </span>

                    </div>
                `;

                return;
            }


            container.innerHTML =
                proximas
                    .map(
                        obrigacao =>
                            criarItemVencimento(
                                obrigacao,
                                hoje
                            )
                    )
                    .join('');
        }


        function criarItemVencimento(
            obrigacao,
            hoje
        ) {

            const data =
                converterData(
                    obrigacao.dataVencimento
                );


            const dias =
                diferencaDias(
                    hoje,
                    data
                );


            const empresa =
                escaparHtml(
                    obrigacao.razaoSocialEmpresa
                    ?? 'Empresa não informada'
                );


            const tipo =
                escaparHtml(
                    formatarTipoObrigacao(
                        obrigacao.tipo
                    )
                );


            let prazoTexto;
            let prazoClasse;


            if (
                dias === 0
            ) {

                prazoTexto =
                    'Hoje';

                prazoClasse =
                    'deadline-critical';

            } else if (
                dias === 1
            ) {

                prazoTexto =
                    'Amanhã';

                prazoClasse =
                    'deadline-warning';

            } else if (
                dias <= 5
            ) {

                prazoTexto =
                    `${dias} dias`;

                prazoClasse =
                    'deadline-warning';

            } else {

                prazoTexto =
                    `${dias} dias`;

                prazoClasse =
                    'deadline-normal';
            }


            return `
                <div class="dashboard-deadline-item">

                    <div
                        class="
                            dashboard-deadline-date
                            ${prazoClasse}
                        "
                    >

                        <strong>
                            ${String(
                data.getDate()
            ).padStart(
                2,
                '0'
            )}
                        </strong>

                        <span>
                            ${obterMesCurto(
                data
            )}
                        </span>

                    </div>


                    <div class="dashboard-deadline-info">

                        <strong>
                            ${tipo}
                        </strong>

                        <span>
                            ${empresa}
                        </span>

                    </div>


                    <div
                        class="
                            dashboard-deadline-count
                            ${prazoClasse}
                        "
                    >
                        ${prazoTexto}
                    </div>

                </div>
            `;
        }


        /*
         * =====================================================
         * RESUMO OPERACIONAL
         * =====================================================
         */

        function renderizarResumoOperacional(
            obrigacoes
        ) {

            const total =
                obrigacoes.length;


            const hoje =
                inicioDoDia(
                    new Date()
                );


            const concluidas =
                obrigacoes.filter(
                    obrigacao =>
                        isFinalizada(
                            obrigacao.status
                        )
                ).length;


            const atrasadas =
                obrigacoes.filter(
                    obrigacao =>
                        isAtrasada(
                            obrigacao,
                            hoje
                        )
                ).length;


            const pendentes =
                obrigacoes.filter(
                    obrigacao =>
                        isPendente(
                            obrigacao
                        )
                ).length;


            const percentualConcluidas =
                calcularPercentual(
                    concluidas,
                    total
                );


            const percentualPendentes =
                calcularPercentual(
                    pendentes,
                    total
                );


            const percentualAtrasadas =
                calcularPercentual(
                    atrasadas,
                    total
                );


            definirTexto(
                'dashboard-total-obrigacoes',
                total
            );


            atualizarProgresso(
                'dashboard-percent-concluidas',
                'dashboard-progress-concluidas',
                percentualConcluidas
            );


            atualizarProgresso(
                'dashboard-percent-pendentes',
                'dashboard-progress-pendentes',
                percentualPendentes
            );


            atualizarProgresso(
                'dashboard-percent-atrasadas',
                'dashboard-progress-atrasadas',
                percentualAtrasadas
            );
        }


        function atualizarProgresso(
            idTexto,
            idBarra,
            percentual
        ) {

            definirTexto(
                idTexto,
                `${percentual}%`
            );


            const barra =
                document.getElementById(
                    idBarra
                );


            if (barra) {

                barra.style.width =
                    `${percentual}%`;
            }
        }


        /*
         * =====================================================
         * DOCUMENTOS RECENTES
         * =====================================================
         */

        function renderizarDocumentosRecentes(
            documentos
        ) {

            const container =
                document.getElementById(
                    'lista-documentos-recentes'
                );


            if (!container) {
                return;
            }


            const recentes =
                [...documentos]
                    .sort(
                        (
                            a,
                            b
                        ) => {

                            const dataA =
                                converterDataHora(
                                    a.criadoEm
                                );


                            const dataB =
                                converterDataHora(
                                    b.criadoEm
                                );


                            return (
                                dataB - dataA
                            );
                        }
                    )
                    .slice(
                        0,
                        5
                    );


            if (
                recentes.length === 0
            ) {

                container.innerHTML = `
                    <div class="dashboard-empty-state">

                        <div class="dashboard-empty-icon">
                            📄
                        </div>

                        <strong>
                            Nenhum documento
                        </strong>

                        <span>
                            Os documentos recentes
                            aparecerão aqui.
                        </span>

                    </div>
                `;

                return;
            }


            container.innerHTML =
                recentes
                    .map(
                        documento =>
                            criarDocumentoRecente(
                                documento
                            )
                    )
                    .join('');
        }


        function criarDocumentoRecente(
            documento
        ) {

            const nome =
                escaparHtml(
                    documento.nomeArquivo
                    ?? 'Documento'
                );


            const empresa =
                escaparHtml(
                    documento.razaoSocialEmpresa
                    ?? '-'
                );


            const extensao =
                obterExtensao(
                    documento.nomeArquivo
                );


            const data =
                converterDataHora(
                    documento.criadoEm
                );


            return `
                <div class="dashboard-document-item">

                    <div
                        class="
                            dashboard-document-icon
                            ${classeDocumento(
                extensao
            )}
                        "
                    >
                        ${iconeArquivo()}
                    </div>


                    <div class="dashboard-document-info">

                        <strong
                            title="${nome}"
                        >
                            ${nome}
                        </strong>

                        <span>
                            ${empresa}
                        </span>

                    </div>


                    <div class="dashboard-document-meta">

                        <span>
                            ${
                extensao
                    ? extensao.toUpperCase()
                    : 'ARQUIVO'
            }
                        </span>

                        <small>
                            ${
                data
                    ? formatarDataCurta(
                        data
                    )
                    : '-'
            }
                        </small>

                    </div>

                </div>
            `;
        }


        /*
         * =====================================================
         * REGRAS DE STATUS
         * =====================================================
         */

        function isFinalizada(
            status
        ) {

            const valor =
                normalizarStatus(
                    status
                );


            return [
                'CONCLUIDA',
                'CONCLUIDO',
                'FINALIZADA',
                'FINALIZADO',
                'PAGA',
                'PAGO'
            ].includes(
                valor
            );
        }


        function isPendente(
            obrigacao
        ) {

            return !isFinalizada(
                obrigacao.status
            );
        }


        function isAtrasada(
            obrigacao,
            hoje
        ) {

            if (
                !isPendente(
                    obrigacao
                )
            ) {
                return false;
            }


            const vencimento =
                converterData(
                    obrigacao.dataVencimento
                );


            if (!vencimento) {
                return false;
            }


            return (
                vencimento < hoje
            );
        }


        /*
         * =====================================================
         * HELPERS
         * =====================================================
         */

        function definirTexto(
            id,
            valor
        ) {

            const elemento =
                document.getElementById(
                    id
                );


            if (elemento) {

                elemento.textContent =
                    valor;
            }
        }


        function converterData(
            valor
        ) {

            if (!valor) {
                return null;
            }


            const partes =
                String(valor)
                    .substring(
                        0,
                        10
                    )
                    .split('-');


            if (
                partes.length !== 3
            ) {
                return null;
            }


            const ano =
                Number(
                    partes[0]
                );


            const mes =
                Number(
                    partes[1]
                );


            const dia =
                Number(
                    partes[2]
                );


            if (
                !ano
                ||
                !mes
                ||
                !dia
            ) {
                return null;
            }


            return new Date(
                ano,
                mes - 1,
                dia
            );
        }


        function converterDataHora(
            valor
        ) {

            if (!valor) {
                return null;
            }


            const data =
                new Date(
                    valor
                );


            return Number.isNaN(
                data.getTime()
            )
                ? null
                : data;
        }


        function inicioDoDia(
            data
        ) {

            return new Date(
                data.getFullYear(),
                data.getMonth(),
                data.getDate()
            );
        }


        function diferencaDias(
            inicio,
            fim
        ) {

            const diferenca =
                fim.getTime()
                -
                inicio.getTime();


            return Math.ceil(
                diferenca
                /
                (
                    1000
                    *
                    60
                    *
                    60
                    *
                    24
                )
            );
        }


        function calcularPercentual(
            valor,
            total
        ) {

            if (!total) {
                return 0;
            }


            return Math.round(
                (
                    valor
                    /
                    total
                )
                *
                100
            );
        }


        function normalizarStatus(
            valor
        ) {

            return String(
                valor ?? ''
            )
                .normalize('NFD')
                .replace(
                    /[\u0300-\u036f]/g,
                    ''
                )
                .toUpperCase()
                .trim();
        }


        function formatarTipoObrigacao(
            tipo
        ) {

            if (!tipo) {
                return 'Obrigação';
            }


            return String(tipo)
                .replace(
                    /_/g,
                    ' '
                )
                .toLowerCase()
                .replace(
                    /\b\w/g,
                    letra =>
                        letra.toUpperCase()
                );
        }


        function obterMesCurto(
            data
        ) {

            return [
                'JAN',
                'FEV',
                'MAR',
                'ABR',
                'MAI',
                'JUN',
                'JUL',
                'AGO',
                'SET',
                'OUT',
                'NOV',
                'DEZ'
            ][
                data.getMonth()
                ];
        }


        function formatarDataCurta(
            data
        ) {

            return data
                .toLocaleDateString(
                    'pt-BR',
                    {
                        day: '2-digit',
                        month: '2-digit'
                    }
                );
        }


        function obterExtensao(
            nome
        ) {

            const valor =
                String(
                    nome ?? ''
                );


            if (
                !valor.includes('.')
            ) {
                return '';
            }


            return valor
                .split('.')
                .pop()
                .toLowerCase();
        }


        function classeDocumento(
            extensao
        ) {

            if (
                extensao === 'pdf'
            ) {
                return 'dashboard-file-pdf';
            }


            if (
                extensao === 'doc'
                ||
                extensao === 'docx'
            ) {
                return 'dashboard-file-word';
            }


            if (
                extensao === 'xls'
                ||
                extensao === 'xlsx'
            ) {
                return 'dashboard-file-excel';
            }


            if (
                [
                    'png',
                    'jpg',
                    'jpeg',
                    'webp'
                ].includes(
                    extensao
                )
            ) {
                return 'dashboard-file-image';
            }


            return 'dashboard-file-default';
        }


        function escaparHtml(
            valor
        ) {

            return String(
                valor ?? ''
            )
                .replace(
                    /&/g,
                    '&amp;'
                )
                .replace(
                    /</g,
                    '&lt;'
                )
                .replace(
                    />/g,
                    '&gt;'
                )
                .replace(
                    /"/g,
                    '&quot;'
                )
                .replace(
                    /'/g,
                    '&#039;'
                );
        }


        function iconeArquivo() {

            return `
                <svg
                    width="19"
                    height="19"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                >
                    <path
                        d="
                            M14 2H6
                            a2 2 0 0 0-2 2
                            v16a2 2 0 0 0 2 2h12
                            a2 2 0 0 0 2-2V8z
                        "
                    ></path>

                    <polyline
                        points="14 2 14 8 20 8"
                    ></polyline>
                </svg>
            `;
        }
    }
);