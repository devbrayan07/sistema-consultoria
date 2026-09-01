document.addEventListener(
    'DOMContentLoaded',
    () => {

        /*
         * =====================================================
         * PROTEÇÃO DA ROTA
         * =====================================================
         */

        if (
            !window.auth
                ?.validarProtecaoRota()
        ) {
            return;
        }


        /*
         * =====================================================
         * USUÁRIO LOGADO
         * =====================================================
         */

        function obterUsuarioLogado() {

            if (
                window.auth
                &&
                typeof window.auth.obterUsuario === 'function'
            ) {
                return window.auth.obterUsuario();
            }


            if (
                window.auth
                &&
                typeof window.auth.getUsuario === 'function'
            ) {
                return window.auth.getUsuario();
            }


            if (
                window.auth
                &&
                typeof window.auth.getUser === 'function'
            ) {
                return window.auth.getUser();
            }


            const chaves = [
                'usuario',
                'usuarioLogado',
                'user'
            ];


            for (const chave of chaves) {

                try {

                    const valor =
                        localStorage.getItem(
                            chave
                        );


                    if (valor) {

                        return JSON.parse(
                            valor
                        );
                    }

                } catch (erro) {

                    console.warn(
                        `Não foi possível ler ${chave}:`,
                        erro
                    );
                }
            }


            return null;
        }


        const usuarioLogado =
            obterUsuarioLogado();


        let tipoUsuario =
            String(
                usuarioLogado?.tipo
                ??
                usuarioLogado?.role
                ??
                usuarioLogado?.perfil
                ??
                ''
            )
                .replace(
                    'ROLE_',
                    ''
                )
                .toUpperCase()
                .trim();


        /*
         * Compatibilidade temporária
         * com perfis antigos.
         */

        if (
            tipoUsuario === 'CLIENTE'
        ) {
            tipoUsuario = 'USUARIO';
        }


        if (
            tipoUsuario === 'EQUIPE'
        ) {
            tipoUsuario = 'CONTADOR';
        }


        const ehUsuario =
            tipoUsuario === 'USUARIO';


        const ehContador =
            tipoUsuario === 'CONTADOR';


        const ehAdministrador =
            tipoUsuario === 'ADMINISTRADOR';


        const podeCadastrarObrigacao =
            ehContador
            ||
            ehAdministrador;


        console.log(
            'Usuário logado:',
            usuarioLogado
        );


        console.log(
            'Tipo de usuário:',
            tipoUsuario
        );


        /*
         * =====================================================
         * ELEMENTOS
         * =====================================================
         */

        const btnToggle =
            document.getElementById(
                'btn-toggle-form'
            );


        const btnFechar =
            document.getElementById(
                'btn-fechar-form'
            );


        const btnCancelar =
            document.getElementById(
                'btn-cancelar-form'
            );


        const btnSalvar =
            document.getElementById(
                'btn-salvar-obrigacao'
            );


        const cardForm =
            document.getElementById(
                'card-form'
            );


        const form =
            document.getElementById(
                'form-obrigacao'
            );


        const lista =
            document.getElementById(
                'lista-obrigacoes'
            );


        const empresaSelect =
            document.getElementById(
                'idEmpresa'
            );


        const filtroEmpresa =
            document.getElementById(
                'filtro-empresa'
            );


        const filtroStatus =
            document.getElementById(
                'filtro-status'
            );


        const filtroPesquisa =
            document.getElementById(
                'filtro-obrigacoes'
            );


        const contador =
            document.getElementById(
                'obrigacoes-contador'
            );


        const alerta =
            document.getElementById(
                'error-alert'
            );


        let obrigacoesCarregadas = [];
        let empresasCarregadas = [];


        /*
         * =====================================================
         * PERMISSÕES
         * =====================================================
         */

        function aplicarPermissoesObrigacoes() {

            /*
             * Somente CONTADOR e ADMINISTRADOR
             * podem cadastrar obrigações.
             */

            if (
                podeCadastrarObrigacao
            ) {

                if (
                    btnToggle
                    &&
                    cardForm?.style.display !== 'block'
                ) {

                    btnToggle.style.display =
                        'inline-flex';
                }


                return;
            }


            /*
             * USUARIO ou qualquer perfil não
             * reconhecido fica somente em consulta.
             */

            if (btnToggle) {

                btnToggle.style.display =
                    'none';
            }


            if (cardForm) {

                cardForm.style.display =
                    'none';
            }
        }


        /*
         * =====================================================
         * FORMULÁRIO
         * =====================================================
         */

        function abrirFormulario() {

            if (
                !podeCadastrarObrigacao
            ) {

                return;
            }


            if (!cardForm) {

                return;
            }


            cardForm.style.display =
                'block';


            if (btnToggle) {

                btnToggle.style.display =
                    'none';
            }


            ocultarErro();


            cardForm.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }


        function fecharFormulario() {

            if (!cardForm) {

                return;
            }


            cardForm.style.display =
                'none';


            form?.reset();


            ocultarErro();


            aplicarPermissoesObrigacoes();
        }


        btnToggle?.addEventListener(
            'click',
            abrirFormulario
        );


        btnFechar?.addEventListener(
            'click',
            fecharFormulario
        );


        btnCancelar?.addEventListener(
            'click',
            fecharFormulario
        );


        /*
         * =====================================================
         * EMPRESAS
         * =====================================================
         */

        async function carregarEmpresas() {

            try {

                const empresas =
                    await window.api.get(
                        '/empresas'
                    );


                empresasCarregadas =
                    Array.isArray(
                        empresas
                    )
                        ? empresas
                        : [];


                preencherEmpresas();

            } catch (erro) {

                console.error(
                    'Erro ao carregar empresas:',
                    erro
                );


                empresasCarregadas = [];


                preencherEmpresas();
            }
        }


        function preencherEmpresas() {

            /*
             * Select usado no cadastro.
             */

            if (empresaSelect) {

                empresaSelect.innerHTML = `
                    <option value="">
                        Selecione a empresa
                    </option>
                `;


                empresasCarregadas.forEach(
                    empresa => {

                        const option =
                            document.createElement(
                                'option'
                            );


                        option.value =
                            String(
                                empresa.idEmpresa
                            );


                        option.textContent =
                            empresa.razaoSocial
                            ??
                            `Empresa #${empresa.idEmpresa}`;


                        empresaSelect.appendChild(
                            option
                        );
                    }
                );
            }


            /*
             * Select usado no filtro.
             */

            if (filtroEmpresa) {

                filtroEmpresa.innerHTML = `
                    <option value="">
                        Todas as empresas
                    </option>
                `;


                empresasCarregadas.forEach(
                    empresa => {

                        const option =
                            document.createElement(
                                'option'
                            );


                        option.value =
                            String(
                                empresa.idEmpresa
                            );


                        option.textContent =
                            empresa.razaoSocial
                            ??
                            `Empresa #${empresa.idEmpresa}`;


                        filtroEmpresa.appendChild(
                            option
                        );
                    }
                );
            }
        }


        /*
         * =====================================================
         * OBRIGAÇÕES
         * =====================================================
         */

        async function carregarObrigacoes() {

            if (!lista) {

                return;
            }


            lista.innerHTML = `
                <tr>

                    <td
                        colspan="6"
                        class="obrigacoes-loading"
                    >
                        Carregando obrigações...
                    </td>

                </tr>
            `;


            try {

                const obrigacoes =
                    await window.api.get(
                        '/obrigacoes'
                    );


                obrigacoesCarregadas =
                    Array.isArray(
                        obrigacoes
                    )
                        ? obrigacoes
                        : [];


                atualizarIndicadores();

                aplicarFiltros();

            } catch (erro) {

                console.error(
                    'Erro ao carregar obrigações:',
                    erro
                );


                obrigacoesCarregadas = [];


                atualizarIndicadores();


                lista.innerHTML = `
                    <tr>

                        <td
                            colspan="6"
                            class="
                                obrigacoes-loading
                                obrigacoes-load-error
                            "
                        >
                            Não foi possível carregar
                            as obrigações.
                        </td>

                    </tr>
                `;


                atualizarContador(
                    0
                );
            }
        }


        /*
         * =====================================================
         * FILTROS
         * =====================================================
         */

        function aplicarFiltros() {

            const pesquisa =
                normalizarTexto(
                    filtroPesquisa?.value
                );


            const empresa =
                filtroEmpresa?.value
                ??
                '';


            const statusFiltro =
                String(
                    filtroStatus?.value
                    ??
                    ''
                )
                    .toUpperCase();


            const hoje =
                inicioDoDia(
                    new Date()
                );


            const filtradas =
                obrigacoesCarregadas.filter(
                    obrigacao => {

                        const razao =
                            normalizarTexto(
                                obrigacao.razaoSocialEmpresa
                            );


                        const tipo =
                            normalizarTexto(
                                obrigacao.tipo
                            );


                        const correspondePesquisa =
                            !pesquisa
                            ||
                            razao.includes(
                                pesquisa
                            )
                            ||
                            tipo.includes(
                                pesquisa
                            );


                        const correspondeEmpresa =
                            !empresa
                            ||
                            String(
                                obrigacao.idEmpresa
                            )
                            === empresa;


                        let correspondeStatus =
                            true;


                        if (
                            statusFiltro === 'ATRASADA'
                        ) {

                            correspondeStatus =
                                isAtrasada(
                                    obrigacao,
                                    hoje
                                );

                        } else if (
                            statusFiltro === 'PAGA'
                        ) {

                            correspondeStatus =
                                isConcluida(
                                    obrigacao.status
                                );

                        } else if (
                            statusFiltro === 'PENDENTE'
                        ) {

                            correspondeStatus =
                                !isConcluida(
                                    obrigacao.status
                                )
                                &&
                                !isAtrasada(
                                    obrigacao,
                                    hoje
                                );
                        }


                        return (
                            correspondePesquisa
                            &&
                            correspondeEmpresa
                            &&
                            correspondeStatus
                        );
                    }
                );


            renderizarObrigacoes(
                filtradas
            );
        }


        filtroPesquisa?.addEventListener(
            'input',
            aplicarFiltros
        );


        filtroEmpresa?.addEventListener(
            'change',
            aplicarFiltros
        );


        filtroStatus?.addEventListener(
            'change',
            aplicarFiltros
        );


        /*
         * =====================================================
         * TABELA
         * =====================================================
         */

        function renderizarObrigacoes(
            obrigacoes
        ) {

            if (!lista) {

                return;
            }


            if (
                !Array.isArray(
                    obrigacoes
                )
                ||
                obrigacoes.length === 0
            ) {

                lista.innerHTML = `
                    <tr>

                        <td
                            colspan="6"
                            class="obrigacoes-empty"
                        >

                            <div
                                class="obrigacoes-empty-icon"
                            >
                                ✓
                            </div>

                            <strong>
                                Nenhuma obrigação encontrada
                            </strong>

                            <span>
                                ${
                    ehUsuario
                        ? 'Nenhuma obrigação foi cadastrada para sua empresa.'
                        : 'Ajuste os filtros ou cadastre uma nova obrigação.'
                }
                            </span>

                        </td>

                    </tr>
                `;


                atualizarContador(
                    0
                );


                return;
            }


            lista.innerHTML =
                obrigacoes
                    .map(
                        obrigacao =>
                            criarLinhaObrigacao(
                                obrigacao
                            )
                    )
                    .join('');


            atualizarContador(
                obrigacoes.length
            );
        }


        function criarLinhaObrigacao(
            obrigacao
        ) {

            const hoje =
                inicioDoDia(
                    new Date()
                );


            const atrasada =
                isAtrasada(
                    obrigacao,
                    hoje
                );


            const tipo =
                formatarTipo(
                    obrigacao.tipo
                );


            const empresa =
                escaparHtml(
                    obrigacao.razaoSocialEmpresa
                    ??
                    '-'
                );


            const statusReal =
                atrasada
                    ? 'ATRASADA'
                    : String(
                        obrigacao.status
                        ??
                        'PENDENTE'
                    )
                        .toUpperCase();


            const vencimento =
                converterData(
                    obrigacao.dataVencimento
                );


            return `
                <tr
                    class="${
                atrasada
                    ? 'obrigacao-row-atrasada'
                    : ''
            }"
                >

                    <td>

                        <div class="obrigacao-main-cell">

                            <div
                                class="
                                    obrigacao-type-icon
                                    ${classeTipo(
                obrigacao.tipo
            )}
                                "
                            >
                                ${iconeObrigacao()}
                            </div>

                            <div>

                                <strong>
                                    ${escaparHtml(
                tipo
            )}
                                </strong>

                                <span>
                                    #${escaparHtml(
                obrigacao.id
                ??
                '-'
            )}
                                </span>

                            </div>

                        </div>

                    </td>


                    <td>

                        <div class="obrigacao-company">

                            <span
                                class="obrigacao-company-avatar"
                            >
                                ${escaparHtml(
                obterIniciais(
                    obrigacao.razaoSocialEmpresa
                )
            )}
                            </span>

                            <span>
                                ${empresa}
                            </span>

                        </div>

                    </td>


                    <td>
                        ${formatarCompetencia(
                obrigacao.competencia
            )}
                    </td>


                    <td>

                        <div class="obrigacao-vencimento">

                            <strong>
                                ${
                vencimento
                    ? formatarData(
                        vencimento
                    )
                    : '-'
            }
                            </strong>

                            ${
                vencimento
                    ? criarPrazo(
                        vencimento,
                        hoje,
                        statusReal
                    )
                    : ''
            }

                        </div>

                    </td>


                    <td>

                        <strong class="obrigacao-value">

                            ${formatarMoeda(
                obrigacao.valor
            )}

                        </strong>

                    </td>


                    <td>

                        <span
                            class="
                                obrigacao-status
                                ${classeStatus(
                statusReal
            )}
                            "
                        >
                            ${formatarStatus(
                statusReal
            )}
                        </span>

                    </td>

                </tr>
            `;
        }


        /*
         * =====================================================
         * INDICADORES
         * =====================================================
         */

        function atualizarIndicadores() {

            const hoje =
                inicioDoDia(
                    new Date()
                );


            const total =
                obrigacoesCarregadas.length;


            const atrasadas =
                obrigacoesCarregadas.filter(
                    item =>
                        isAtrasada(
                            item,
                            hoje
                        )
                ).length;


            const concluidas =
                obrigacoesCarregadas.filter(
                    item =>
                        isConcluida(
                            item.status
                        )
                ).length;


            const pendentes =
                obrigacoesCarregadas.filter(
                    item =>
                        !isConcluida(
                            item.status
                        )
                        &&
                        !isAtrasada(
                            item,
                            hoje
                        )
                ).length;


            definirTexto(
                'stat-total-obrigacoes',
                total
            );


            definirTexto(
                'stat-pendentes',
                pendentes
            );


            definirTexto(
                'stat-concluidas',
                concluidas
            );


            definirTexto(
                'stat-atrasadas',
                atrasadas
            );
        }


        /*
         * =====================================================
         * CADASTRO
         * =====================================================
         */

        form?.addEventListener(
            'submit',
            async event => {

                event.preventDefault();


                ocultarErro();


                /*
                 * Somente CONTADOR e ADMINISTRADOR
                 * podem enviar POST.
                 */

                if (
                    !podeCadastrarObrigacao
                ) {

                    exibirErro(
                        'Seu perfil não possui permissão para cadastrar obrigações.'
                    );


                    aplicarPermissoesObrigacoes();


                    return;
                }


                const idEmpresa =
                    Number(
                        document.getElementById(
                            'idEmpresa'
                        )?.value
                    );


                const tipo =
                    String(
                        document.getElementById(
                            'tipo'
                        )?.value
                        ??
                        ''
                    )
                        .toUpperCase();


                const competencia =
                    document.getElementById(
                        'competencia'
                    )?.value;


                const dataVencimento =
                    document.getElementById(
                        'dataVencimento'
                    )?.value;


                const valor =
                    document.getElementById(
                        'valor'
                    )?.value;


                const honorario =
                    document.getElementById(
                        'honorario'
                    )?.value;


                const status =
                    String(
                        document.getElementById(
                            'status'
                        )?.value
                        ??
                        ''
                    )
                        .toUpperCase();


                /*
                 * Validações básicas.
                 */

                if (
                    !Number.isInteger(
                        idEmpresa
                    )
                    ||
                    idEmpresa <= 0
                ) {

                    exibirErro(
                        'Selecione uma empresa.'
                    );


                    return;
                }


                const tiposPermitidos = [
                    'DAS',
                    'DARF',
                    'DEFIS',
                    'FOLHA',
                    'ISS',
                    'OUTRO'
                ];


                if (
                    !tiposPermitidos.includes(
                        tipo
                    )
                ) {

                    exibirErro(
                        'Selecione um tipo de obrigação válido.'
                    );


                    return;
                }


                if (
                    !competencia
                    ||
                    !dataVencimento
                ) {

                    exibirErro(
                        'Informe competência e vencimento.'
                    );


                    return;
                }


                const statusPermitidos = [
                    'PENDENTE',
                    'PAGA',
                    'ATRASADA'
                ];


                if (
                    status
                    &&
                    !statusPermitidos.includes(
                        status
                    )
                ) {

                    exibirErro(
                        'Selecione um status válido.'
                    );


                    return;
                }


                const payload = {

                    idEmpresa,

                    tipo,

                    competencia,

                    dataVencimento,

                    valor:
                        valor !== ''
                            ? Number(
                                valor
                            )
                            : null,

                    honorario:
                        honorario !== ''
                            ? Number(
                                honorario
                            )
                            : null
                };


                if (status) {

                    payload.status =
                        status;
                }


                console.log(
                    'Payload da obrigação:',
                    payload
                );


                alterarEstadoSalvar(
                    true
                );


                try {

                    if (idObrigacaoEdicao) {
                        await window.api.put(
                            `/obrigacoes/${idObrigacaoEdicao}`,
                            payload
                        );
                    } else {
                        await window.api.post(
                            '/obrigacoes',
                            payload
                        );
                    }



                    fecharFormulario();


                    await carregarObrigacoes();

                } catch (erro) {

                    console.error(
                        'Erro ao cadastrar obrigação:',
                        erro
                    );


                    exibirErro(
                        erro.message
                        ||
                        'Erro ao cadastrar obrigação.'
                    );

                } finally {

                    alterarEstadoSalvar(
                        false
                    );
                }
            }
        );


        function alterarEstadoSalvar(
            salvando
        ) {

            if (!btnSalvar) {

                return;
            }


            btnSalvar.disabled =
                salvando;


            btnSalvar.innerHTML =
                salvando
                    ? `
                        <span class="obrigacao-spinner"></span>

                        Salvando...
                    `
                    : 'Salvar obrigação';
        }


        /*
         * =====================================================
         * STATUS
         * =====================================================
         */

        function isConcluida(
            status
        ) {

            return (
                String(
                    status
                    ??
                    ''
                )
                    .toUpperCase()
                === 'PAGA'
            );
        }


        function isAtrasada(
            obrigacao,
            hoje
        ) {

            if (
                isConcluida(
                    obrigacao.status
                )
            ) {

                return false;
            }


            const data =
                converterData(
                    obrigacao.dataVencimento
                );


            return Boolean(
                data
                &&
                data < hoje
            );
        }


        /*
         * =====================================================
         * HELPERS
         * =====================================================
         */

        function criarPrazo(
            data,
            hoje,
            status
        ) {

            if (
                isConcluida(
                    status
                )
            ) {

                return `
                    <span class="prazo-ok">
                        Concluída
                    </span>
                `;
            }


            const dias =
                Math.round(
                    (
                        data.getTime()
                        -
                        hoje.getTime()
                    )
                    /
                    86400000
                );


            if (
                dias < 0
            ) {

                const quantidade =
                    Math.abs(
                        dias
                    );


                return `
                    <span class="prazo-atrasado">
                        Vencida há ${quantidade}
                        ${
                    quantidade === 1
                        ? 'dia'
                        : 'dias'
                }
                    </span>
                `;
            }


            if (
                dias === 0
            ) {

                return `
                    <span class="prazo-hoje">
                        Vence hoje
                    </span>
                `;
            }


            if (
                dias === 1
            ) {

                return `
                    <span class="prazo-alerta">
                        Amanhã
                    </span>
                `;
            }


            return `
                <span class="prazo-normal">
                    Em ${dias} dias
                </span>
            `;
        }


        function converterData(
            valor
        ) {

            if (!valor) {

                return null;
            }


            const partes =
                String(
                    valor
                )
                    .substring(
                        0,
                        10
                    )
                    .split(
                        '-'
                    );


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


        function inicioDoDia(
            data
        ) {

            return new Date(
                data.getFullYear(),
                data.getMonth(),
                data.getDate()
            );
        }


        function formatarData(
            data
        ) {

            return data.toLocaleDateString(
                'pt-BR'
            );
        }


        function formatarCompetencia(
            valor
        ) {

            const data =
                converterData(
                    valor
                );


            if (!data) {

                return '-';
            }


            return data.toLocaleDateString(
                'pt-BR',
                {
                    month: 'short',
                    year: 'numeric'
                }
            );
        }


        function formatarMoeda(
            valor
        ) {

            if (
                valor === null
                ||
                valor === undefined
                ||
                valor === ''
            ) {

                return 'R$ 0,00';
            }


            const numero =
                Number(
                    valor
                );


            if (
                Number.isNaN(
                    numero
                )
            ) {

                return 'R$ 0,00';
            }


            return numero.toLocaleString(
                'pt-BR',
                {
                    style: 'currency',
                    currency: 'BRL'
                }
            );
        }


        function formatarTipo(
            tipo
        ) {

            const valor =
                String(
                    tipo
                    ??
                    'Obrigação'
                )
                    .toUpperCase();


            const nomes = {

                DAS:
                    'DAS',

                DARF:
                    'DARF',

                DEFIS:
                    'DEFIS',

                FOLHA:
                    'Folha de pagamento',

                ISS:
                    'ISS',

                OUTRO:
                    'Outro'
            };


            return nomes[valor]
                ??
                valor;
        }


        function formatarStatus(
            status
        ) {

            const valor =
                String(
                    status
                    ??
                    ''
                )
                    .toUpperCase();


            const textos = {

                PENDENTE:
                    'Pendente',

                PAGA:
                    'Paga',

                ATRASADA:
                    'Atrasada'
            };


            return textos[valor]
                ??
                valor;
        }


        function classeStatus(
            status
        ) {

            const valor =
                String(
                    status
                    ??
                    ''
                )
                    .toUpperCase();


            if (
                valor === 'ATRASADA'
            ) {

                return 'status-atrasado';
            }


            if (
                valor === 'PAGA'
            ) {

                return 'status-concluido';
            }


            return 'status-pendente';
        }


        function classeTipo(
            tipo
        ) {

            const valor =
                String(
                    tipo
                    ??
                    ''
                )
                    .toUpperCase();


            if (
                [
                    'DAS',
                    'DARF'
                ].includes(
                    valor
                )
            ) {

                return 'obrigacao-type-blue';
            }


            if (
                [
                    'DEFIS',
                    'FOLHA'
                ].includes(
                    valor
                )
            ) {

                return 'obrigacao-type-green';
            }


            if (
                valor === 'ISS'
            ) {

                return 'obrigacao-type-purple';
            }


            return 'obrigacao-type-orange';
        }


        function atualizarContador(
            quantidade
        ) {

            if (!contador) {

                return;
            }


            contador.textContent =
                quantidade === 1
                    ? '1 obrigação encontrada'
                    : `${quantidade} obrigações encontradas`;
        }


        function normalizarTexto(
            valor
        ) {

            return String(
                valor
                ??
                ''
            )
                .normalize(
                    'NFD'
                )
                .replace(
                    /[\u0300-\u036f]/g,
                    ''
                )
                .toLowerCase();
        }


        function obterIniciais(
            valor
        ) {

            const partes =
                String(
                    valor
                    ??
                    ''
                )
                    .trim()
                    .split(
                        /\s+/
                    )
                    .filter(
                        Boolean
                    );


            if (!partes.length) {

                return '?';
            }


            return partes.length === 1
                ? partes[0]
                    .substring(
                        0,
                        2
                    )
                    .toUpperCase()
                : (
                    partes[0][0]
                    +
                    partes[
                    partes.length - 1
                        ][0]
                )
                    .toUpperCase();
        }


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


        function exibirErro(
            mensagem
        ) {

            if (!alerta) {

                alert(
                    mensagem
                );


                return;
            }


            alerta.textContent =
                mensagem;


            alerta.style.display =
                'block';
        }


        function ocultarErro() {

            if (!alerta) {

                return;
            }


            alerta.style.display =
                'none';


            alerta.textContent =
                '';
        }

        let idObrigacaoEdicao = null;

        function editarObrigacao(id) {

            if (!podeCadastrarObrigacao) {
                return;
            }

            const obrigacao =
                obrigacoesCarregadas.find(
                    item => Number(item.id) === Number(id)
                );

            if (!obrigacao) {
                exibirErro(
                    'Obrigação não encontrada.'
                );

                return;
            }


            idObrigacaoEdicao =
                obrigacao.id;


            document.getElementById('idEmpresa').value =
                obrigacao.idEmpresa ?? '';

            document.getElementById('tipo').value =
                obrigacao.tipo ?? '';

            document.getElementById('competencia').value =
                obrigacao.competencia ?? '';

            document.getElementById('dataVencimento').value =
                obrigacao.dataVencimento ?? '';

            document.getElementById('valor').value =
                obrigacao.valor ?? '';

            document.getElementById('honorario').value =
                obrigacao.honorario ?? '';

            document.getElementById('status').value =
                obrigacao.status ?? 'PENDENTE';


            abrirFormulario();


            const titulo =
                document.querySelector(
                    '.obrigacao-form-header h2'
                );

            if (titulo) {
                titulo.textContent =
                    'Editar obrigação';
            }


            if (btnSalvar) {
                btnSalvar.textContent =
                    'Salvar alterações';
            }
        }


        function escaparHtml(
            valor
        ) {

            return String(
                valor
                ??
                ''
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


        function iconeObrigacao() {

            return `
                <svg
                    width="18"
                    height="18"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                >

                    <circle
                        cx="12"
                        cy="12"
                        r="9"
                    ></circle>

                    <polyline
                        points="12 7 12 12 15 14"
                    ></polyline>

                </svg>
            `;
        }


        /*
         * =====================================================
         * INICIALIZAÇÃO
         * =====================================================
         */

        async function inicializar() {

            aplicarPermissoesObrigacoes();


            await Promise.all([
                carregarEmpresas(),
                carregarObrigacoes()
            ]);


            aplicarPermissoesObrigacoes();
        }


        inicializar();
    }
);