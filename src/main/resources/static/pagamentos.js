document.addEventListener(
    'DOMContentLoaded',
    iniciarPaginaPagamentos
);


let pagamentos = [];
let pagamentoSelecionado = null;


async function iniciarPaginaPagamentos() {

    configurarEventos();

    await carregarPagamentos();
}


function configurarEventos() {

    document
        .getElementById('filtro-status')
        ?.addEventListener(
            'change',
            renderizarPagamentos
        );


    document
        .getElementById('filtro-busca')
        ?.addEventListener(
            'input',
            renderizarPagamentos
        );


    document
        .getElementById('btn-fechar-modal')
        ?.addEventListener(
            'click',
            fecharModal
        );


    document
        .getElementById('modal-pagamento-backdrop')
        ?.addEventListener(
            'click',
            function (event) {

                if (
                    event.target ===
                    event.currentTarget
                ) {
                    fecharModal();
                }
            }
        );


    document
        .getElementById('btn-copiar-pix')
        ?.addEventListener(
            'click',
            copiarCodigoPix
        );
}


async function carregarPagamentos() {

    try {

        pagamentos =
            await window.api.get(
                '/pagamentos'
            );


        if (!Array.isArray(pagamentos)) {
            pagamentos = [];
        }


        atualizarResumo();

        renderizarPagamentos();


    } catch (erro) {

        console.error(
            'Erro ao carregar pagamentos:',
            erro
        );


        alert(
            erro.message ||
            'Não foi possível carregar os pagamentos.'
        );
    }
}


function atualizarResumo() {

    const total =
        pagamentos.length;


    const pendentes =
        pagamentos.filter(
            pagamento =>
                pagamento.status === 'PENDENTE' ||
                pagamento.status === 'PROCESSANDO'
        ).length;


    const pagos =
        pagamentos.filter(
            pagamento =>
                pagamento.status === 'PAGO'
        );


    const valorPago =
        pagos.reduce(
            (total, pagamento) =>
                total +
                Number(
                    pagamento.valor || 0
                ),
            0
        );


    document.getElementById(
        'resumo-total'
    ).textContent = total;


    document.getElementById(
        'resumo-pendentes'
    ).textContent = pendentes;


    document.getElementById(
        'resumo-pagos'
    ).textContent = pagos.length;


    document.getElementById(
        'resumo-valor-pago'
    ).textContent =
        formatarMoeda(valorPago);
}


function renderizarPagamentos() {

    const tbody =
        document.getElementById(
            'pagamentos-body'
        );


    const vazio =
        document.getElementById(
            'pagamentos-vazio'
        );


    if (!tbody) {
        return;
    }


    const status =
        document
            .getElementById('filtro-status')
            ?.value || '';


    const busca =
        document
            .getElementById('filtro-busca')
            ?.value
            ?.trim()
            ?.toLowerCase() || '';


    const filtrados =
        pagamentos.filter(
            pagamento => {

                if (
                    status &&
                    pagamento.status !== status
                ) {
                    return false;
                }


                if (busca) {

                    const texto =
                        [
                            pagamento.razaoSocialEmpresa,
                            pagamento.nomeUsuario,
                            pagamento.id,
                            pagamento.idObrigacao
                        ]
                            .filter(Boolean)
                            .join(' ')
                            .toLowerCase();


                    if (!texto.includes(busca)) {
                        return false;
                    }
                }


                return true;
            }
        );


    tbody.innerHTML = '';


    vazio.hidden =
        filtrados.length > 0;


    filtrados.forEach(
        pagamento => {

            const tr =
                document.createElement('tr');


            tr.innerHTML = `

            
                            ${podePagarObrigacao(obrigacao)
                        ? `
                <button
                    type="button"
                    class="btn-pagar-pix"
                    data-pagar-obrigacao="${obrigacao.id}"
                >
                    Pagar com PIX
                </button>
              `
                        : ''
                    }
            


                <td>
                    #${pagamento.id}
                </td>

                <td>
                    ${escaparHtml(
                pagamento.razaoSocialEmpresa ||
                '-'
            )}
                </td>

                <td>
                    ${escaparHtml(
                pagamento.nomeUsuario ||
                '-'
            )}
                </td>

                <td>
                    ${
                pagamento.idObrigacao
                    ? '#' + pagamento.idObrigacao
                    : '-'
            }
                </td>

                <td>
                    ${formatarMoeda(
                pagamento.valor
            )}
                </td>

                <td>
                    ${formatarMetodo(
                pagamento.metodoPagamento
            )}
                </td>

                <td>
                    ${criarBadgeStatus(
                pagamento.status
            )}
                </td>

                <td>
                    ${formatarData(
                pagamento.dataCriacao
            )}
                </td>

                <td>
                    ${formatarData(
                pagamento.dataPagamento
            )}
                </td>

                <td>

                    <div class="acoes-pagamento">

                        <button
                            type="button"
                            class="
                                btn-pagamento
                                btn-visualizar
                            "
                            data-acao="visualizar"
                            data-id="${pagamento.id}"
                        >
                            Detalhes
                        </button>

                        ${
                deveMostrarAprovacaoDev(
                    pagamento
                )
                    ? `
                                    <button
                                        type="button"
                                        class="
                                            btn-pagamento
                                            btn-aprovar-dev
                                        "
                                        data-acao="aprovar"
                                        data-id="${pagamento.id}"
                                    >
                                        Aprovar DEV
                                    </button>
                                `
                    : ''
            }

                    </div>

                </td>

            `;


            tbody.appendChild(tr);
        }
    );


    tbody
        .querySelectorAll(
            '[data-acao="visualizar"]'
        )
        .forEach(
            botao => {

                botao.addEventListener(
                    'click',
                    () => abrirPagamento(
                        Number(botao.dataset.id)
                    )
                );
            }
        );


    tbody
        .querySelectorAll(
            '[data-acao="aprovar"]'
        )
        .forEach(
            botao => {

                botao.addEventListener(
                    'click',
                    () => aprovarPagamentoDev(
                        Number(botao.dataset.id)
                    )
                );
            }
        );
}


function deveMostrarAprovacaoDev(
    pagamento
) {

    if (
        pagamento.status !== 'PENDENTE' &&
        pagamento.status !== 'PROCESSANDO'
    ) {
        return false;
    }


    const usuario =
        obterUsuarioLocal();


    const tipo =
        usuario?.tipo;


    return (
        tipo === 'CONTADOR' ||
        tipo === 'ADMINISTRADOR'
    );
}


function abrirPagamento(
    idPagamento
) {

    pagamentoSelecionado =
        pagamentos.find(
            pagamento =>
                pagamento.id === idPagamento
        );


    if (!pagamentoSelecionado) {
        return;
    }


    const backdrop =
        document.getElementById(
            'modal-pagamento-backdrop'
        );


    const qrContainer =
        document.getElementById(
            'pix-qr-container'
        );


    const codigoContainer =
        document.getElementById(
            'pix-codigo-container'
        );


    const codigo =
        document.getElementById(
            'pix-codigo'
        );


    const detalhes =
        document.getElementById(
            'pix-detalhes'
        );


    qrContainer.innerHTML = '';


    const qrCode =
        pagamentoSelecionado.qrCodePix;


    if (qrCode) {

        const imagem =
            document.createElement('img');


        imagem.alt =
            'QR Code PIX';


        imagem.src =
            montarQrCodeSrc(
                qrCode
            );


        qrContainer.appendChild(
            imagem
        );


        qrContainer.style.display =
            'flex';

    } else {

        qrContainer.style.display =
            'none';
    }


    if (pagamentoSelecionado.codigoPix) {

        codigo.value =
            pagamentoSelecionado.codigoPix;


        codigoContainer.style.display =
            'block';

    } else {

        codigo.value = '';

        codigoContainer.style.display =
            'none';
    }


    detalhes.innerHTML = `

        <div>
            <span>Status</span>

            <strong>
                ${formatarStatus(
        pagamentoSelecionado.status
    )}
            </strong>
        </div>


        <div>
            <span>Empresa</span>

            <strong>
                ${escaparHtml(
        pagamentoSelecionado
            .razaoSocialEmpresa ||
        '-'
    )}
            </strong>
        </div>


        <div>
            <span>Valor</span>

            <strong>
                ${formatarMoeda(
        pagamentoSelecionado.valor
    )}
            </strong>
        </div>


        <div>
            <span>Método</span>

            <strong>
                ${formatarMetodo(
        pagamentoSelecionado
            .metodoPagamento
    )}
            </strong>
        </div>


        <div>
            <span>Obrigação</span>

            <strong>
                ${
        pagamentoSelecionado
            .idObrigacao
            ? '#' +
            pagamentoSelecionado
                .idObrigacao
            : '-'
    }
            </strong>
        </div>


        <div>
            <span>ID externo</span>

            <strong>
                ${escaparHtml(
        pagamentoSelecionado
            .idPagamentoExterno ||
        '-'
    )}
            </strong>
        </div>


        <div>
            <span>Criado em</span>

            <strong>
                ${formatarData(
        pagamentoSelecionado
            .dataCriacao
    )}
            </strong>
        </div>


        <div>
            <span>Pago em</span>

            <strong>
                ${formatarData(
        pagamentoSelecionado
            .dataPagamento
    )}
            </strong>
        </div>

    `;


    backdrop.classList.add(
        'ativo'
    );
}


function fecharModal() {

    document
        .getElementById(
            'modal-pagamento-backdrop'
        )
        ?.classList
        .remove('ativo');


    pagamentoSelecionado = null;
}


async function copiarCodigoPix() {

    if (
        !pagamentoSelecionado ||
        !pagamentoSelecionado.codigoPix
    ) {
        return;
    }


    try {

        await navigator.clipboard.writeText(
            pagamentoSelecionado.codigoPix
        );


        const botao =
            document.getElementById(
                'btn-copiar-pix'
            );


        const textoOriginal =
            botao.textContent;


        botao.textContent =
            'Copiado!';


        setTimeout(
            () => {

                botao.textContent =
                    textoOriginal;

            },
            1800
        );


    } catch (erro) {

        console.error(
            'Erro ao copiar PIX:',
            erro
        );


        alert(
            'Não foi possível copiar o código PIX.'
        );
    }
}


async function aprovarPagamentoDev(
    idPagamento
) {

    const confirmar =
        window.confirm(
            'Simular a aprovação deste pagamento?'
        );


    if (!confirmar) {
        return;
    }


    try {

        await window.api.post(
            `/dev/pagamentos/${idPagamento}/aprovar`
        );


        alert(
            'Pagamento aprovado no ambiente de desenvolvimento.'
        );


        await carregarPagamentos();


    } catch (erro) {

        console.error(
            'Erro ao aprovar pagamento:',
            erro
        );


        alert(
            erro.message ||
            'Não foi possível aprovar o pagamento.'
        );
    }
}


function criarBadgeStatus(
    status
) {

    const classe =
        String(status || '')
            .toLowerCase();


    return `

        <span
            class="
                status-pagamento
                status-${classe}
            "
        >
            ${formatarStatus(status)}
        </span>

    `;
}


function formatarStatus(
    status
) {

    const statusMap = {

        PENDENTE:
            'Pendente',

        PROCESSANDO:
            'Processando',

        PAGO:
            'Pago',

        RECUSADO:
            'Recusado',

        CANCELADO:
            'Cancelado',

        EXPIRADO:
            'Expirado',

        ESTORNADO:
            'Estornado'

    };


    return (
        statusMap[status] ||
        status ||
        '-'
    );
}


function formatarMetodo(
    metodo
) {

    const metodoMap = {

        PIX:
            'PIX',

        CARTAO:
            'Cartão',

        BOLETO:
            'Boleto'

    };


    return (
        metodoMap[metodo] ||
        metodo ||
        '-'
    );
}


function formatarMoeda(
    valor
) {

    const numero =
        Number(valor || 0);


    return numero.toLocaleString(
        'pt-BR',
        {
            style: 'currency',
            currency: 'BRL'
        }
    );
}


function formatarData(
    data
) {

    if (!data) {
        return '-';
    }


    const objetoData =
        new Date(data);


    if (
        Number.isNaN(
            objetoData.getTime()
        )
    ) {
        return '-';
    }


    return objetoData.toLocaleString(
        'pt-BR',
        {
            dateStyle: 'short',
            timeStyle: 'short'
        }
    );
}


function montarQrCodeSrc(
    qrCode
) {

    if (
        qrCode.startsWith('data:image')
    ) {
        return qrCode;
    }


    return (
        'data:image/png;base64,' +
        qrCode
    );
}


function obterUsuarioLocal() {

    try {

        const usuario =
            localStorage.getItem(
                'usuario'
            );


        if (!usuario) {
            return null;
        }


        return JSON.parse(
            usuario
        );


    } catch (erro) {

        console.error(
            'Erro ao recuperar usuário:',
            erro
        );


        return null;
    }
}


function escaparHtml(
    valor
) {

    const div =
        document.createElement('div');


    div.textContent =
        String(valor ?? '');


    return div.innerHTML;
}