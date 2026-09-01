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


        /*
         * =====================================================
         * NORMALIZAÇÃO DO PERFIL
         * =====================================================
         */

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
         * Compatibilidade com valores antigos.
         */

        if (
            tipoUsuario === 'CLIENTE'
        ) {

            tipoUsuario =
                'USUARIO';
        }


        if (
            tipoUsuario === 'EQUIPE'
        ) {

            tipoUsuario =
                'CONTADOR';
        }


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
         * ELEMENTOS DA PÁGINA
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
                'btn-salvar-empresa'
            );


        const cardForm =
            document.getElementById(
                'card-form'
            );


        const formEmpresa =
            document.getElementById(
                'form-empresa'
            );


        const listaEmpresas =
            document.getElementById(
                'lista-empresas'
            );


        const alertError =
            document.getElementById(
                'error-alert'
            );


        const campoCliente =
            document.getElementById(
                'campo-cliente-responsavel'
            );


        const selectCliente =
            document.getElementById(
                'idUsuarioCliente'
            );


        const inputCnpj =
            document.getElementById(
                'cnpj'
            );


        const filtroPesquisa =
            document.getElementById(
                'filtro-empresas'
            );


        const filtroRegime =
            document.getElementById(
                'filtro-regime'
            );


        const contador =
            document.getElementById(
                'empresas-contador'
            );


        const indicadoresEmpresas =
            document.getElementById(
                'indicadores-empresas'
            );


        const empresasSubtitulo =
            document.getElementById(
                'empresas-subtitulo'
            );


        let empresasCarregadas = [];


        /*
         * =====================================================
         * PERMISSÕES DA PÁGINA
         * =====================================================
         */

        function aplicarPermissoesEmpresas() {

            /*
             * =================================================
             * CONTADOR / ADMINISTRADOR
             * =================================================
             */

            if (
                tipoUsuario !== 'USUARIO'
            ) {

                if (indicadoresEmpresas) {

                    indicadoresEmpresas.style.display =
                        '';
                }


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
             * =================================================
             * USUÁRIO COMUM
             * =================================================
             *
             * Não precisa visualizar os indicadores
             * administrativos de quantidade de empresas.
             */

            if (indicadoresEmpresas) {

                indicadoresEmpresas.style.display =
                    'none';
            }


            const possuiEmpresa =
                empresasCarregadas.length > 0;


            /*
             * Atualiza o texto do cabeçalho,
             * caso o HTML possua o ID.
             */

            if (empresasSubtitulo) {

                empresasSubtitulo.textContent =
                    possuiEmpresa

                        ? 'Consulte os dados da sua empresa cadastrada.'

                        : 'Cadastre sua empresa para utilizar os recursos do sistema.';
            }


            /*
             * =================================================
             * JÁ POSSUI EMPRESA
             * =================================================
             */

            if (possuiEmpresa) {

                /*
                 * Esconde permanentemente o botão
                 * de cadastro para o usuário comum.
                 */

                if (btnToggle) {

                    btnToggle.style.display =
                        'none';
                }


                /*
                 * Garante que o formulário
                 * também fique fechado.
                 */

                if (cardForm) {

                    cardForm.style.display =
                        'none';
                }


                return;
            }


            /*
             * =================================================
             * AINDA NÃO POSSUI EMPRESA
             * =================================================
             */

            if (
                btnToggle
                &&
                cardForm?.style.display !== 'block'
            ) {

                btnToggle.style.display =
                    'inline-flex';
            }
        }


        /*
         * =====================================================
         * PERMISSÕES DO CAMPO CLIENTE
         * =====================================================
         */

        function configurarCampoCliente() {

            /*
             * =================================================
             * USUÁRIO COMUM
             * =================================================
             *
             * Não escolhe cliente.
             *
             * O backend vincula automaticamente a empresa
             * ao próprio usuário autenticado.
             */

            if (
                tipoUsuario === 'USUARIO'
            ) {

                if (campoCliente) {

                    campoCliente.style.display =
                        'none';
                }


                if (selectCliente) {

                    selectCliente.required =
                        false;

                    selectCliente.disabled =
                        true;

                    selectCliente.value =
                        '';
                }


                return;
            }


            /*
             * =================================================
             * CONTADOR / ADMINISTRADOR
             * =================================================
             */

            if (campoCliente) {

                campoCliente.style.display =
                    '';
            }


            if (selectCliente) {

                selectCliente.disabled =
                    false;

                selectCliente.required =
                    true;
            }
        }


        /*
         * =====================================================
         * FORMULÁRIO
         * =====================================================
         */

        function abrirFormulario() {

            /*
             * Usuário comum não pode abrir
             * cadastro de segunda empresa.
             */

            if (
                tipoUsuario === 'USUARIO'
                &&
                empresasCarregadas.length > 0
            ) {

                exibirErro(
                    'Você já possui uma empresa cadastrada.'
                );

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


            configurarCampoCliente();


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


            formEmpresa?.reset();


            /*
             * Após resetar o formulário,
             * reaplica as regras do cliente.
             */

            configurarCampoCliente();


            ocultarErro();


            /*
             * Decide se o botão "Nova empresa"
             * deve aparecer novamente.
             */

            aplicarPermissoesEmpresas();
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
         * MÁSCARA DO CNPJ
         * =====================================================
         */

        inputCnpj?.addEventListener(
            'input',
            event => {

                let valor =
                    event.target.value
                        .replace(
                            /\D/g,
                            ''
                        )
                        .slice(
                            0,
                            14
                        );


                valor =
                    valor.replace(
                        /^(\d{2})(\d)/,
                        '$1.$2'
                    );


                valor =
                    valor.replace(
                        /^(\d{2})\.(\d{3})(\d)/,
                        '$1.$2.$3'
                    );


                valor =
                    valor.replace(
                        /\.(\d{3})(\d)/,
                        '.$1/$2'
                    );


                valor =
                    valor.replace(
                        /(\d{4})(\d)/,
                        '$1-$2'
                    );


                event.target.value =
                    valor;
            }
        );


        /*
         * =====================================================
         * CLIENTES
         * =====================================================
         */

        async function carregarClientes() {

            /*
             * USUARIO nunca chama /usuarios.
             */

            if (
                tipoUsuario === 'USUARIO'
            ) {

                return;
            }


            if (!selectCliente) {
                return;
            }


            try {

                selectCliente.innerHTML = `
                    <option value="">
                        Carregando clientes...
                    </option>
                `;


                const usuarios =
                    await window.api.get(
                        '/usuarios'
                    );


                const clientes =
                    Array.isArray(
                        usuarios
                    )
                        ? usuarios.filter(
                            usuario => {

                                const tipo =
                                    String(
                                        usuario.tipo
                                        ?? ''
                                    )
                                        .replace(
                                            'ROLE_',
                                            ''
                                        )
                                        .toUpperCase()
                                        .trim();


                                return (
                                    tipo === 'USUARIO'
                                    ||
                                    tipo === 'CLIENTE'
                                );
                            }
                        )
                        : [];


                selectCliente.innerHTML = `
                    <option value="">
                        Selecione o cliente
                    </option>
                `;


                clientes.forEach(
                    cliente => {

                        const option =
                            document.createElement(
                                'option'
                            );


                        option.value =
                            cliente.id;


                        option.textContent =
                            cliente.email

                                ? `${cliente.nome} — ${cliente.email}`

                                : cliente.nome;


                        selectCliente.appendChild(
                            option
                        );
                    }
                );


                if (
                    clientes.length === 0
                ) {

                    selectCliente.innerHTML = `
                        <option value="">
                            Nenhum cliente disponível
                        </option>
                    `;
                }

            } catch (erro) {

                console.error(
                    'Erro ao carregar clientes:',
                    erro
                );


                selectCliente.innerHTML = `
                    <option value="">
                        Erro ao carregar clientes
                    </option>
                `;
            }
        }


        /*
         * =====================================================
         * EMPRESAS
         * =====================================================
         */

        async function carregarEmpresas() {

            if (!listaEmpresas) {
                return;
            }


            listaEmpresas.innerHTML = `
                <tr>

                    <td
                        colspan="5"
                        class="empresas-loading"
                    >
                        Carregando empresas...
                    </td>

                </tr>
            `;


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


                /*
                 * Muito importante:
                 *
                 * primeiro atualizamos as permissões porque
                 * agora sabemos se o USUARIO já possui empresa.
                 */

                aplicarPermissoesEmpresas();


                atualizarIndicadores();


                aplicarFiltros();

            } catch (erro) {

                console.error(
                    'Erro ao carregar empresas:',
                    erro
                );


                listaEmpresas.innerHTML = `
                    <tr>

                        <td
                            colspan="5"
                            class="
                                empresas-loading
                                empresas-load-error
                            "
                        >
                            Não foi possível carregar as empresas.
                        </td>

                    </tr>
                `;
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


            const regime =
                filtroRegime?.value
                ?? '';


            const empresas =
                empresasCarregadas.filter(
                    empresa => {

                        const razao =
                            normalizarTexto(
                                empresa.razaoSocial
                            );


                        const fantasia =
                            normalizarTexto(
                                empresa.nomeFantasia
                            );


                        const cnpj =
                            normalizarTexto(
                                empresa.cnpj
                            );


                        const cliente =
                            normalizarTexto(
                                empresa.nomeCliente
                            );


                        const correspondePesquisa =
                            !pesquisa
                            ||
                            razao.includes(
                                pesquisa
                            )
                            ||
                            fantasia.includes(
                                pesquisa
                            )
                            ||
                            cnpj.includes(
                                pesquisa
                            )
                            ||
                            cliente.includes(
                                pesquisa
                            );


                        const correspondeRegime =
                            !regime
                            ||
                            empresa.regimeTributario
                            === regime;


                        return (
                            correspondePesquisa
                            &&
                            correspondeRegime
                        );
                    }
                );


            renderizarEmpresas(
                empresas
            );
        }


        filtroPesquisa?.addEventListener(
            'input',
            aplicarFiltros
        );


        filtroRegime?.addEventListener(
            'change',
            aplicarFiltros
        );


        /*
         * =====================================================
         * RENDERIZAÇÃO
         * =====================================================
         */

        function renderizarEmpresas(
            empresas
        ) {

            if (!listaEmpresas) {
                return;
            }


            if (
                !Array.isArray(
                    empresas
                )
                ||
                empresas.length === 0
            ) {

                listaEmpresas.innerHTML = `
                    <tr>

                        <td
                            colspan="5"
                            class="empresas-empty"
                        >

                            <div
                                class="empresas-empty-icon"
                            >
                                ${iconeEmpresa()}
                            </div>

                            <strong>
                                Nenhuma empresa encontrada
                            </strong>

                            <span>
                                ${
                    tipoUsuario === 'USUARIO'
                        ? 'Cadastre sua empresa para começar.'
                        : 'Ajuste os filtros ou cadastre uma nova empresa.'
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


            listaEmpresas.innerHTML =
                empresas
                    .map(
                        empresa =>
                            criarLinhaEmpresa(
                                empresa
                            )
                    )
                    .join('');


            atualizarContador(
                empresas.length
            );
        }


        function criarLinhaEmpresa(
            empresa
        ) {

            const razao =
                escaparHtml(
                    empresa.razaoSocial
                    ?? '-'
                );


            const fantasia =
                escaparHtml(
                    empresa.nomeFantasia
                    ?? ''
                );


            const cliente =
                escaparHtml(
                    empresa.nomeCliente
                    ??
                    (
                        empresa.idUsuarioCliente
                            ? `Cliente #${empresa.idUsuarioCliente}`
                            : '-'
                    )
                );


            const cnpj =
                formatarCnpj(
                    empresa.cnpj
                );


            const regime =
                empresa.regimeTributario;


            const data =
                formatarData(
                    empresa.criadoEm
                );


            return `
                <tr>

                    <td>

                        <div
                            class="empresa-table-company"
                        >

                            <div
                                class="empresa-avatar"
                            >
                                ${obterIniciais(
                empresa.razaoSocial
            )}
                            </div>


                            <div
                                class="empresa-company-info"
                            >

                                <strong>
                                    ${razao}
                                </strong>

                                ${
                fantasia
                    ? `
                                            <span>
                                                ${fantasia}
                                            </span>
                                        `
                    : ''
            }

                            </div>

                        </div>

                    </td>


                    <td>

                        <span
                            class="empresa-cnpj"
                        >
                            ${cnpj}
                        </span>

                    </td>


                    <td>

                        <span
                            class="
                                empresa-regime-badge
                                ${classeRegime(
                regime
            )}
                            "
                        >
                            ${formatarRegime(
                regime
            )}
                        </span>

                    </td>


                    <td>

                        <div
                            class="empresa-client-cell"
                        >

                            <div
                                class="empresa-client-avatar"
                            >
                                ${obterIniciais(
                empresa.nomeCliente
            )}
                            </div>

                            <span>
                                ${cliente}
                            </span>

                        </div>

                    </td>


                    <td>

                        <span
                            class="empresa-date"
                        >
                            ${data}
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

            const total =
                empresasCarregadas.length;


            const simples =
                empresasCarregadas.filter(
                    empresa =>
                        empresa.regimeTributario
                        === 'SIMPLES_NACIONAL'
                ).length;


            const mei =
                empresasCarregadas.filter(
                    empresa =>
                        empresa.regimeTributario
                        === 'MEI'
                ).length;


            const outros =
                empresasCarregadas.filter(
                    empresa =>
                        [
                            'LUCRO_PRESUMIDO',
                            'LUCRO_REAL'
                        ].includes(
                            empresa.regimeTributario
                        )
                ).length;


            definirTexto(
                'stat-total-empresas',
                total
            );


            definirTexto(
                'stat-simples',
                simples
            );


            definirTexto(
                'stat-mei',
                mei
            );


            definirTexto(
                'stat-outros-regimes',
                outros
            );
        }


        /*
         * =====================================================
         * CADASTRO
         * =====================================================
         */

        formEmpresa?.addEventListener(
            'submit',
            async event => {

                event.preventDefault();


                ocultarErro();


                /*
                 * Segurança visual adicional.
                 */

                if (
                    tipoUsuario === 'USUARIO'
                    &&
                    empresasCarregadas.length > 0
                ) {

                    exibirErro(
                        'Você já possui uma empresa cadastrada.'
                    );

                    aplicarPermissoesEmpresas();

                    return;
                }


                const razaoSocial =
                    document.getElementById(
                        'razaoSocial'
                    )
                        ?.value
                        .trim();


                const nomeFantasia =
                    document.getElementById(
                        'nomeFantasia'
                    )
                        ?.value
                        .trim();


                const cnpj =
                    document.getElementById(
                        'cnpj'
                    )
                        ?.value
                        .replace(
                            /\D/g,
                            ''
                        );


                const regimeTributario =
                    document.getElementById(
                        'regimeTributario'
                    )
                        ?.value;


                /*
                 * =================================================
                 * VALIDAÇÕES
                 * =================================================
                 */

                if (!razaoSocial) {

                    exibirErro(
                        'Informe a razão social.'
                    );

                    return;
                }


                if (
                    !cnpj
                    ||
                    cnpj.length !== 14
                ) {

                    exibirErro(
                        'O CNPJ deve conter exatamente 14 dígitos.'
                    );

                    return;
                }


                if (!regimeTributario) {

                    exibirErro(
                        'Selecione o regime tributário.'
                    );

                    return;
                }


                /*
                 * =================================================
                 * PAYLOAD
                 * =================================================
                 */

                const payload = {

                    razaoSocial,

                    nomeFantasia:
                        nomeFantasia
                        || null,

                    cnpj,

                    regimeTributario
                };


                /*
                 * CONTADOR / ADMINISTRADOR
                 *
                 * precisam informar o cliente.
                 */

                if (
                    tipoUsuario !== 'USUARIO'
                ) {

                    const idUsuarioCliente =
                        Number(
                            selectCliente?.value
                        );


                    if (
                        !idUsuarioCliente
                        ||
                        Number.isNaN(
                            idUsuarioCliente
                        )
                    ) {

                        exibirErro(
                            'Selecione um cliente válido.'
                        );

                        return;
                    }


                    payload.idUsuarioCliente =
                        idUsuarioCliente;
                }


                alterarEstadoSalvamento(
                    true
                );


                try {

                    await window.api.post(
                        '/empresas',
                        payload
                    );


                    /*
                     * Limpa e fecha o formulário.
                     */

                    fecharFormulario();


                    /*
                     * Recarrega a lista.
                     *
                     * Para USUARIO, isso fará
                     * o botão "Nova empresa" desaparecer.
                     */

                    await carregarEmpresas();

                } catch (erro) {

                    console.error(
                        'Erro ao cadastrar empresa:',
                        erro
                    );


                    exibirErro(
                        erro.message
                        ||
                        'Erro ao cadastrar empresa.'
                    );

                } finally {

                    alterarEstadoSalvamento(
                        false
                    );
                }
            }
        );


        /*
         * =====================================================
         * ESTADO DO BOTÃO SALVAR
         * =====================================================
         */

        function alterarEstadoSalvamento(
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
                        <span
                            class="empresa-spinner"
                        ></span>

                        Salvando...
                    `
                    : `
                        ${iconeSalvar()}

                        Salvar empresa
                    `;
        }


        /*
         * =====================================================
         * HELPERS
         * =====================================================
         */

        function atualizarContador(
            quantidade
        ) {

            if (!contador) {
                return;
            }


            if (
                tipoUsuario === 'USUARIO'
            ) {

                contador.textContent =
                    quantidade === 1
                        ? 'Sua empresa cadastrada'
                        : 'Nenhuma empresa cadastrada';

                return;
            }


            contador.textContent =
                quantidade === 1
                    ? '1 empresa encontrada'
                    : `${quantidade} empresas encontradas`;
        }


        function exibirErro(
            mensagem
        ) {

            if (!alertError) {

                alert(
                    mensagem
                );

                return;
            }


            alertError.textContent =
                mensagem;


            alertError.style.display =
                'block';
        }


        function ocultarErro() {

            if (!alertError) {
                return;
            }


            alertError.style.display =
                'none';


            alertError.textContent =
                '';
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


        function normalizarTexto(
            valor
        ) {

            return String(
                valor ?? ''
            )
                .normalize(
                    'NFD'
                )
                .replace(
                    /[\u0300-\u036f]/g,
                    ''
                )
                .toLowerCase()
                .trim();
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


        function formatarCnpj(
            valor
        ) {

            const numeros =
                String(
                    valor ?? ''
                )
                    .replace(
                        /\D/g,
                        ''
                    );


            if (
                numeros.length !== 14
            ) {

                return escaparHtml(
                    valor
                    ?? '-'
                );
            }


            return numeros.replace(
                /^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/,
                '$1.$2.$3/$4-$5'
            );
        }


        function formatarRegime(
            regime
        ) {

            const regimes = {

                SIMPLES_NACIONAL:
                    'Simples Nacional',

                LUCRO_PRESUMIDO:
                    'Lucro Presumido',

                LUCRO_REAL:
                    'Lucro Real',

                MEI:
                    'MEI'
            };


            return regimes[regime]
                ?? regime
                ?? '-';
        }


        function classeRegime(
            regime
        ) {

            const classes = {

                SIMPLES_NACIONAL:
                    'regime-simples',

                LUCRO_PRESUMIDO:
                    'regime-presumido',

                LUCRO_REAL:
                    'regime-real',

                MEI:
                    'regime-mei'
            };


            return classes[regime]
                ?? 'regime-default';
        }


        function formatarData(
            valor
        ) {

            if (!valor) {
                return '-';
            }


            const data =
                new Date(
                    valor
                );


            if (
                Number.isNaN(
                    data.getTime()
                )
            ) {

                return '-';
            }


            return data.toLocaleDateString(
                'pt-BR',
                {
                    day: '2-digit',
                    month: '2-digit',
                    year: 'numeric'
                }
            );
        }


        function obterIniciais(
            nome
        ) {

            const partes =
                String(
                    nome ?? ''
                )
                    .trim()
                    .split(
                        /\s+/
                    )
                    .filter(
                        Boolean
                    );


            if (
                partes.length === 0
            ) {

                return '?';
            }


            if (
                partes.length === 1
            ) {

                return partes[0]
                    .substring(
                        0,
                        2
                    )
                    .toUpperCase();
            }


            return (
                partes[0][0]
                +
                partes[
                partes.length - 1
                    ][0]
            )
                .toUpperCase();
        }


        function iconeEmpresa() {

            return `
                <svg
                    width="22"
                    height="22"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                >
                    <rect
                        x="3"
                        y="9"
                        width="18"
                        height="12"
                        rx="2"
                    ></rect>

                    <path
                        d="M9 21V7a3 3 0 0 1 6 0v14"
                    ></path>
                </svg>
            `;
        }


        function iconeSalvar() {

            return `
                <svg
                    width="17"
                    height="17"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                >
                    <path
                        d="
                            M19 21H5
                            a2 2 0 0 1-2-2
                            V5
                            a2 2 0 0 1 2-2
                            h11
                            l5 5
                            v11
                            a2 2 0 0 1-2 2z
                        "
                    ></path>

                    <polyline
                        points="17 21 17 13 7 13 7 21"
                    ></polyline>

                    <polyline
                        points="7 3 7 8 15 8"
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

            /*
             * Primeiro aplica a regra do
             * campo Cliente responsável.
             */

            configurarCampoCliente();


            /*
             * Usuário comum já começa
             * sem indicadores administrativos.
             */

            if (
                tipoUsuario === 'USUARIO'
            ) {

                if (indicadoresEmpresas) {

                    indicadoresEmpresas.style.display =
                        'none';
                }


                await carregarEmpresas();

                return;
            }


            /*
             * CONTADOR / ADMINISTRADOR
             */

            await Promise.all([
                carregarClientes(),
                carregarEmpresas()
            ]);
        }


        inicializar();
    }
);