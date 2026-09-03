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
         * Compatibilidade com valores antigos.
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


        const btnFecharForm =
            document.getElementById(
                'btn-fechar-form'
            );


        const btnCancelarForm =
            document.getElementById(
                'btn-cancelar-form'
            );


        const btnEnviar =
            document.getElementById(
                'btn-enviar-documento'
            );


        const cardForm =
            document.getElementById(
                'card-form'
            );


        const formDocumento =
            document.getElementById(
                'form-documento'
            );


        const listaDocumentos =
            document.getElementById(
                'lista-documentos'
            );


        const selectEmpresa =
            document.getElementById(
                'idEmpresa'
            );


        const filtroEmpresa =
            document.getElementById(
                'filtro-empresa'
            );


        const filtroTipo =
            document.getElementById(
                'filtro-tipo'
            );


        const filtroPesquisa =
            document.getElementById(
                'filtro-documentos'
            );


        const alertError =
            document.getElementById(
                'error-alert'
            );


        const fileInput =
            document.getElementById(
                'arquivo'
            );


        const nomeArquivoSelecionado =
            document.getElementById(
                'nome-arquivo-selecionado'
            );


        const statTotal =
            document.getElementById(
                'stat-total-documentos'
            );


        const statEmpresas =
            document.getElementById(
                'stat-empresas-documentos'
            );


        const statMes =
            document.getElementById(
                'stat-documentos-mes'
            );


        const contador =
            document.getElementById(
                'documentos-contador'
            );


        let documentosCarregados = [];
        let empresasCarregadas = [];


        /*
         * =====================================================
         * PERMISSÕES
         * =====================================================
         */

        function aplicarPermissoesDocumentos() {

            /*
             * CONTADOR / ADMINISTRADOR
             */

            if (
                tipoUsuario !== 'USUARIO'
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
             * USUÁRIO COMUM
             *
             * Pode consultar e abrir documentos,
             * mas não pode enviar documentos.
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

            /*
             * Usuário comum nunca abre
             * o formulário de upload.
             */

            if (
                tipoUsuario === 'USUARIO'
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

            if (formDocumento) {
                formDocumento.reset();
            }

            ocultarErro();

            atualizarNomeArquivoSelecionado(
                null
            );

            aplicarPermissoesDocumentos();
        }

        btnToggle?.addEventListener(
            'click',
            abrirFormulario
        );


        btnFecharForm?.addEventListener(
            'click',
            fecharFormulario
        );


        btnCancelarForm?.addEventListener(
            'click',
            fecharFormulario
        );


        /*
         * =====================================================
         * ARQUIVO SELECIONADO
         * =====================================================
         */

        fileInput?.addEventListener(
            'change',
            () => {

                const arquivo =
                    fileInput.files?.[0];


                atualizarNomeArquivoSelecionado(
                    arquivo
                );
            }
        );


        function atualizarNomeArquivoSelecionado(
            arquivo
        ) {

            if (!nomeArquivoSelecionado) {
                return;
            }


            if (!arquivo) {

                nomeArquivoSelecionado.textContent =
                    'Nenhum arquivo selecionado';


                nomeArquivoSelecionado.classList.remove(
                    'arquivo-selecionado'
                );


                return;
            }


            nomeArquivoSelecionado.textContent =
                `${arquivo.name} • ${formatarTamanhoArquivo(
                    arquivo.size
                )}`;


            nomeArquivoSelecionado.classList.add(
                'arquivo-selecionado'
            );
        }


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


                preencherSelectEmpresas();

            } catch (erro) {

                console.error(
                    'Erro ao carregar empresas:',
                    erro
                );


                empresasCarregadas = [];


                if (selectEmpresa) {

                    selectEmpresa.innerHTML = `
                        <option value="">
                            Erro ao carregar empresas
                        </option>
                    `;
                }
            }
        }


        function preencherSelectEmpresas() {

            /*
             * Select usado no formulário.
             *
             * Para USUARIO ficará oculto
             * junto do formulário.
             */

            if (selectEmpresa) {

                selectEmpresa.innerHTML = `
                    <option value="">
                        Selecione a empresa
                    </option>
                `;


                empresasCarregadas.forEach(
                    empresa => {

                        const id =
                            empresa.idEmpresa
                            ??
                            empresa.id;


                        const option =
                            document.createElement(
                                'option'
                            );


                        option.value =
                            id;


                        option.textContent =
                            empresa.razaoSocial
                            ??
                            empresa.nomeFantasia
                            ??
                            `Empresa ${id}`;


                        selectEmpresa.appendChild(
                            option
                        );
                    }
                );
            }


            /*
             * Filtro continua disponível
             * para qualquer perfil.
             */

            if (filtroEmpresa) {

                filtroEmpresa.innerHTML = `
                    <option value="">
                        Todas as empresas
                    </option>
                `;


                empresasCarregadas.forEach(
                    empresa => {

                        const id =
                            empresa.idEmpresa
                            ??
                            empresa.id;


                        const option =
                            document.createElement(
                                'option'
                            );


                        option.value =
                            String(
                                id
                            );


                        option.textContent =
                            empresa.razaoSocial
                            ??
                            empresa.nomeFantasia
                            ??
                            `Empresa ${id}`;


                        filtroEmpresa.appendChild(
                            option
                        );
                    }
                );
            }
        }


        /*
         * =====================================================
         * DOCUMENTOS
         * =====================================================
         */

        async function carregarDocumentos() {

            if (!listaDocumentos) {
                return;
            }


            exibirCarregamento();


            try {

                const documentos =
                    await window.api.get(
                        '/documentos'
                    );


                documentosCarregados =
                    Array.isArray(
                        documentos
                    )
                        ? documentos
                        : [];


                atualizarIndicadores();


                aplicarFiltros();

            } catch (erro) {

                console.error(
                    'Erro ao carregar documentos:',
                    erro
                );


                listaDocumentos.innerHTML = `
                    <tr>

                        <td
                            colspan="6"
                            class="documentos-loading"
                        >
                            Não foi possível carregar
                            os documentos.
                        </td>

                    </tr>
                `;


                if (contador) {

                    contador.textContent =
                        'Erro ao carregar documentos';
                }
            }
        }


        function exibirCarregamento() {

            if (!listaDocumentos) {
                return;
            }


            listaDocumentos.innerHTML = `
                <tr>

                    <td
                        colspan="6"
                        class="documentos-loading"
                    >
                        Carregando documentos...
                    </td>

                </tr>
            `;
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


            const empresaSelecionada =
                filtroEmpresa?.value
                ?? '';


            const tipoSelecionado =
                filtroTipo?.value
                ?? '';


            const documentosFiltrados =
                documentosCarregados.filter(
                    documento => {

                        const nomeArquivo =
                            normalizarTexto(
                                documento.nomeArquivo
                            );


                        const empresa =
                            normalizarTexto(
                                documento.razaoSocialEmpresa
                            );


                        const tipo =
                            String(
                                documento.tipo
                                ?? ''
                            );


                        const idEmpresa =
                            String(
                                documento.idEmpresa
                                ?? ''
                            );


                        const correspondePesquisa =
                            !pesquisa
                            ||
                            nomeArquivo.includes(
                                pesquisa
                            )
                            ||
                            empresa.includes(
                                pesquisa
                            )
                            ||
                            normalizarTexto(
                                formatarTipoDocumento(
                                    tipo
                                )
                            ).includes(
                                pesquisa
                            );


                        const correspondeEmpresa =
                            !empresaSelecionada
                            ||
                            idEmpresa ===
                            empresaSelecionada;


                        const correspondeTipo =
                            !tipoSelecionado
                            ||
                            tipo ===
                            tipoSelecionado;


                        return (
                            correspondePesquisa
                            &&
                            correspondeEmpresa
                            &&
                            correspondeTipo
                        );
                    }
                );


            renderizarDocumentos(
                documentosFiltrados
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


        filtroTipo?.addEventListener(
            'change',
            aplicarFiltros
        );


        /*
         * =====================================================
         * RENDERIZAÇÃO
         * =====================================================
         */

        function renderizarDocumentos(
            documentos
        ) {

            if (!listaDocumentos) {
                return;
            }


            if (
                !Array.isArray(
                    documentos
                )
                ||
                documentos.length === 0
            ) {

                listaDocumentos.innerHTML = `
                    <tr>

                        <td
                            colspan="6"
                            class="documentos-empty"
                        >

                            <div
                                class="documentos-empty-icon"
                            >
                                ${iconeDocumento()}
                            </div>

                            <strong>
                                Nenhum documento encontrado
                            </strong>

                            <span>
                                ${
                    tipoUsuario === 'USUARIO'
                        ? 'Nenhum documento foi disponibilizado para sua empresa.'
                        : 'Altere os filtros ou envie um novo documento.'
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


            listaDocumentos.innerHTML =
                documentos
                    .map(
                        documento =>
                            criarLinhaDocumento(
                                documento
                            )
                    )
                    .join('');


            atualizarContador(
                documentos.length
            );
        }


        function criarLinhaDocumento(
            documento
        ) {

            const id =
                Number(
                    documento.id
                );


            const nomeArquivo =
                documento.nomeArquivo
                ?? 'documento';


            const nomeSeguro =
                escaparHtml(
                    nomeArquivo
                );


            const nomeJs =
                escaparJs(
                    nomeArquivo
                );


            const empresa =
                escaparHtml(
                    documento.razaoSocialEmpresa
                    ?? '-'
                );


            const remetente =
                escaparHtml(
                    documento.nomeEnviadoPor
                    ?? '-'
                );


            const tipo =
                documento.tipo
                ?? '';


            const competencia =
                formatarCompetencia(
                    documento.competencia
                );


            const extensao =
                obterExtensao(
                    nomeArquivo
                );


            const icone =
                obterIconeArquivo(
                    extensao
                );


            const classeTipo =
                obterClasseTipo(
                    tipo
                );


            return `
                <tr>

                    <td>

                        <div class="documento-file-cell">

                            <div
                                class="
                                    documento-file-icon
                                    ${obterClasseArquivo(
                extensao
            )}
                                "
                            >
                                ${icone}
                            </div>


                            <div
                                class="documento-file-info"
                            >

                                <strong
                                    title="${nomeSeguro}"
                                >
                                    ${nomeSeguro}
                                </strong>

                                <span>
                                    ${
                extensao
                    ? extensao.toUpperCase()
                    : 'ARQUIVO'
            }
                                </span>

                            </div>

                        </div>

                    </td>


                    <td>

                        <div
                            class="documento-company-cell"
                        >

                            <span
                                class="documento-company-avatar"
                            >
                                ${obterInicial(
                documento.razaoSocialEmpresa
            )}
                            </span>

                            <span>
                                ${empresa}
                            </span>

                        </div>

                    </td>


                    <td>

                        <span
                            class="
                                documento-type-badge
                                ${classeTipo}
                            "
                        >
                            ${escaparHtml(
                formatarTipoDocumento(
                    tipo
                )
            )}
                        </span>

                    </td>


                    <td>

                        <span
                            class="documento-competencia"
                        >
                            ${competencia}
                        </span>

                    </td>


                    <td>

                        <span
                            class="documento-remetente"
                        >
                            ${remetente}
                        </span>

                    </td>


                    <td>

                        <div
                            class="documento-row-actions"
                        >

                            <button
                                type="button"
                                class="
                                    documento-action-button
                                    documento-action-open
                                "
                                onclick="
                                    abrirDocumento(
                                        ${id},
                                        '${nomeJs}'
                                    )
                                "
                                title="Abrir documento"
                            >

                                ${iconeAbrir()}

                                <span>
                                    Abrir
                                </span>

                            </button>

                        </div>

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
                documentosCarregados.length;


            const empresas =
                new Set(
                    documentosCarregados
                        .map(
                            documento =>
                                documento.idEmpresa
                        )
                        .filter(
                            id =>
                                id !== null
                                &&
                                id !== undefined
                        )
                );


            const agora =
                new Date();


            const mesAtual =
                agora.getMonth();


            const anoAtual =
                agora.getFullYear();


            const adicionadosMes =
                documentosCarregados
                    .filter(
                        documento => {

                            if (
                                !documento.criadoEm
                            ) {
                                return false;
                            }


                            const data =
                                new Date(
                                    documento.criadoEm
                                );


                            if (
                                Number.isNaN(
                                    data.getTime()
                                )
                            ) {
                                return false;
                            }


                            return (
                                data.getMonth()
                                === mesAtual
                                &&
                                data.getFullYear()
                                === anoAtual
                            );
                        }
                    )
                    .length;


            if (statTotal) {

                statTotal.textContent =
                    total;
            }


            if (statEmpresas) {

                statEmpresas.textContent =
                    empresas.size;
            }


            if (statMes) {

                statMes.textContent =
                    adicionadosMes;
            }
        }


        function atualizarContador(
            quantidade
        ) {

            if (!contador) {
                return;
            }


            contador.textContent =
                quantidade === 1
                    ? '1 documento encontrado'
                    : `${quantidade} documentos encontrados`;
        }


        /*
         * =====================================================
         * UPLOAD
         * =====================================================
         */

        formDocumento?.addEventListener(
            'submit',
            async event => {

                event.preventDefault();


                ocultarErro();


                /*
                 * USUARIO não pode enviar documento.
                 */

                if (
                    tipoUsuario === 'USUARIO'
                ) {

                    aplicarPermissoesDocumentos();

                    return;
                }


                const arquivo =
                    fileInput?.files?.[0];


                const idEmpresaValor =
                    document.getElementById(
                        'idEmpresa'
                    )?.value;


                const tipo =
                    document.getElementById(
                        'tipo'
                    )?.value;


                const competencia =
                    document.getElementById(
                        'competencia'
                    )?.value;


                if (!arquivo) {

                    exibirErro(
                        'Selecione um arquivo.'
                    );

                    return;
                }


                const idEmpresa =
                    Number(
                        idEmpresaValor
                    );


                if (
                    !idEmpresa
                    ||
                    Number.isNaN(
                        idEmpresa
                    )
                ) {

                    exibirErro(
                        'Selecione uma empresa válida.'
                    );

                    return;
                }


                if (!tipo) {

                    exibirErro(
                        'Selecione o tipo do documento.'
                    );

                    return;
                }


                if (!competencia) {

                    exibirErro(
                        'Informe a competência.'
                    );

                    return;
                }


                /*
                 * IMPORTANTE:
                 *
                 * Não enviamos mais idEnviadoPor.
                 *
                 * O backend deve identificar o usuário
                 * através do JWT autenticado.
                 */

                const formData =
                    new FormData();


                formData.append(
                    'file',
                    arquivo
                );


                formData.append(
                    'idEmpresa',
                    idEmpresa
                );


                formData.append(
                    'tipo',
                    tipo
                );


                formData.append(
                    'competencia',
                    competencia
                );


                alterarEstadoEnvio(
                    true
                );


                try {

                    await window.api.upload(
                        '/documentos',
                        formData
                    );


                    fecharFormulario();


                    await carregarDocumentos();

                } catch (erro) {

                    console.error(
                        'Erro ao realizar upload:',
                        erro
                    );


                    exibirErro(
                        erro.message
                        ??
                        'Erro ao realizar upload.'
                    );

                } finally {

                    alterarEstadoEnvio(
                        false
                    );
                }
            }
        );


        function alterarEstadoEnvio(
            enviando
        ) {

            if (!btnEnviar) {
                return;
            }


            btnEnviar.disabled =
                enviando;


            btnEnviar.innerHTML =
                enviando
                    ? `
                        <span
                            class="documento-spinner"
                        ></span>

                        Enviando...
                    `
                    : `
                        ${iconeUpload()}

                        Enviar documento
                    `;
        }


        /*
         * =====================================================
         * ABRIR / BAIXAR DOCUMENTO
         * =====================================================
         */

        window.abrirDocumento =
            async (
                id,
                nomeArquivo
            ) => {

                try {

                    if (!id) {

                        throw new Error(
                            'Documento inválido.'
                        );
                    }


                    const blob =
                        await window.api.getBlob(
                            `/documentos/${id}/download`
                        );


                    if (!blob) {

                        throw new Error(
                            'Arquivo não retornado pelo servidor.'
                        );
                    }


                    const fileURL =
                        URL.createObjectURL(
                            blob
                        );


                    const extensao =
                        obterExtensao(
                            nomeArquivo
                        );


                    const visualizaveis = [
                        'pdf',
                        'png',
                        'jpg',
                        'jpeg',
                        'gif',
                        'webp',
                        'txt'
                    ];


                    if (
                        visualizaveis.includes(
                            extensao
                        )
                    ) {

                        const aba =
                            window.open(
                                fileURL,
                                '_blank'
                            );


                        if (!aba) {

                            URL.revokeObjectURL(
                                fileURL
                            );


                            throw new Error(
                                'O navegador bloqueou a abertura do documento.'
                            );
                        }


                        setTimeout(
                            () => {

                                URL.revokeObjectURL(
                                    fileURL
                                );

                            },
                            60000
                        );


                        return;
                    }


                    const link =
                        document.createElement(
                            'a'
                        );


                    link.href =
                        fileURL;


                    link.download =
                        nomeArquivo
                        ||
                        'documento';


                    document.body.appendChild(
                        link
                    );


                    link.click();


                    link.remove();


                    setTimeout(
                        () => {

                            URL.revokeObjectURL(
                                fileURL
                            );

                        },
                        2000
                    );

                } catch (erro) {

                    console.error(
                        'Erro ao abrir documento:',
                        erro
                    );


                    alert(
                        erro.message
                        ??
                        'Erro ao abrir o arquivo.'
                    );
                }
            };


        /*
         * =====================================================
         * HELPERS
         * =====================================================
         */

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


        function escaparJs(
            valor
        ) {

            return String(
                valor ?? ''
            )
                .replace(
                    /\\/g,
                    '\\\\'
                )
                .replace(
                    /'/g,
                    "\\'"
                )
                .replace(
                    /\r/g,
                    ''
                )
                .replace(
                    /\n/g,
                    ''
                );
        }


        function obterExtensao(
            nomeArquivo
        ) {

            const nome =
                String(
                    nomeArquivo ?? ''
                );


            if (
                !nome.includes(
                    '.'
                )
            ) {
                return '';
            }


            return nome
                .split(
                    '.'
                )
                .pop()
                .toLowerCase();
        }


        function formatarTipoDocumento(
            tipo
        ) {

            const tipos = {

                NOTA_FISCAL:
                    'Nota Fiscal',

                COMPROVANTE:
                    'Comprovante',

                GUIA:
                    'Guia',

                CONTRATO:
                    'Contrato',

                DECLARACAO:
                    'Declaração',

                OUTRO:
                    'Outro'
            };


            return tipos[tipo]
                ?? tipo
                ?? '-';
        }


        function formatarCompetencia(
            data
        ) {

            if (!data) {
                return '-';
            }


            const partes =
                String(
                    data
                )
                    .split(
                        '-'
                    );


            if (
                partes.length < 2
            ) {
                return data;
            }


            const ano =
                Number(
                    partes[0]
                );


            const mes =
                Number(
                    partes[1]
                );


            if (
                !ano
                ||
                !mes
            ) {
                return data;
            }


            const nomesMeses = [
                'Jan',
                'Fev',
                'Mar',
                'Abr',
                'Mai',
                'Jun',
                'Jul',
                'Ago',
                'Set',
                'Out',
                'Nov',
                'Dez'
            ];


            return `${
                nomesMeses[
                mes - 1
                    ]
            }/${ano}`;
        }


        function formatarTamanhoArquivo(
            bytes
        ) {

            if (
                bytes === 0
            ) {
                return '0 B';
            }


            if (!bytes) {
                return '';
            }


            const unidades = [
                'B',
                'KB',
                'MB',
                'GB'
            ];


            const indice =
                Math.floor(
                    Math.log(
                        bytes
                    )
                    /
                    Math.log(
                        1024
                    )
                );


            const tamanho =
                bytes
                /
                Math.pow(
                    1024,
                    indice
                );


            return `${
                tamanho.toFixed(
                    indice === 0
                        ? 0
                        : 1
                )
            } ${
                unidades[
                    indice
                    ]
            }`;
        }


        function obterInicial(
            nome
        ) {

            const texto =
                String(
                    nome ?? ''
                )
                    .trim();


            return texto
                ? texto.charAt(
                    0
                )
                    .toUpperCase()
                : '?';
        }


        function obterClasseTipo(
            tipo
        ) {

            const classes = {

                NOTA_FISCAL:
                    'type-nota',

                COMPROVANTE:
                    'type-comprovante',

                GUIA:
                    'type-guia',

                CONTRATO:
                    'type-contrato',

                DECLARACAO:
                    'type-declaracao',

                OUTRO:
                    'type-outro'
            };


            return classes[tipo]
                ?? 'type-outro';
        }


        function obterClasseArquivo(
            extensao
        ) {

            if (
                extensao === 'pdf'
            ) {
                return 'file-pdf';
            }


            if (
                extensao === 'doc'
                ||
                extensao === 'docx'
            ) {
                return 'file-word';
            }


            if (
                extensao === 'xls'
                ||
                extensao === 'xlsx'
            ) {
                return 'file-excel';
            }


            if (
                extensao === 'png'
                ||
                extensao === 'jpg'
                ||
                extensao === 'jpeg'
                ||
                extensao === 'webp'
            ) {
                return 'file-image';
            }


            return 'file-default';
        }


        function obterIconeArquivo() {

            return `
                <svg
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                >
                    <path
                        d="
                            M14 2H6
                            a2 2 0 0 0-2 2
                            v16
                            a2 2 0 0 0 2 2
                            h12
                            a2 2 0 0 0 2-2
                            V8z
                        "
                    ></path>

                    <polyline
                        points="14 2 14 8 20 8"
                    ></polyline>
                </svg>
            `;
        }


        function iconeDocumento() {

            return obterIconeArquivo();
        }


        function iconeAbrir() {

            return `
                <svg
                    width="16"
                    height="16"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                >
                    <path
                        d="
                            M1 12
                            s4-8 11-8
                            11 8 11 8
                            -4 8-11 8
                            S1 12 1 12z
                        "
                    ></path>

                    <circle
                        cx="12"
                        cy="12"
                        r="3"
                    ></circle>
                </svg>
            `;
        }


        function iconeUpload() {

            return `
                <svg
                    width="17"
                    height="17"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                >
                    <path
                        d="
                            M21 15v4
                            a2 2 0 0 1-2 2
                            H5
                            a2 2 0 0 1-2-2
                            v-4
                        "
                    ></path>

                    <polyline
                        points="17 8 12 3 7 8"
                    ></polyline>

                    <line
                        x1="12"
                        y1="3"
                        x2="12"
                        y2="15"
                    ></line>
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
             * Aplica imediatamente para evitar
             * o botão aparecer durante o carregamento.
             */

            aplicarPermissoesDocumentos();


            await carregarEmpresas();


            await carregarDocumentos();


            aplicarPermissoesDocumentos();
        }


        inicializar();
    }
);