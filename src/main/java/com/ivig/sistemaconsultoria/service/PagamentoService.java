package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.dto.PagamentoRequestDTO;
import com.ivig.sistemaconsultoria.dto.PagamentoResponseDTO;
import com.ivig.sistemaconsultoria.enums.MetodoPagamento;
import com.ivig.sistemaconsultoria.enums.StatusObrigacao;
import com.ivig.sistemaconsultoria.enums.StatusPagamento;
import com.ivig.sistemaconsultoria.enums.TipoPagamento;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.model.Empresa;
import com.ivig.sistemaconsultoria.model.ObrigacaoFiscal;
import com.ivig.sistemaconsultoria.model.Pagamento;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.ObrigacaoFiscalRepository;
import com.ivig.sistemaconsultoria.repository.PagamentoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;

    private final ObrigacaoFiscalRepository obrigacaoFiscalRepository;

    private final MercadoPagoService mercadoPagoService;


    /*
     * ============================================================
     * CRIAR PAGAMENTO
     * ============================================================
     */

    @Transactional
    public PagamentoResponseDTO criarPagamento(
            PagamentoRequestDTO dto,
            Usuario usuarioAutenticado
    ) {

        /*
         * ========================================================
         * VALIDAR USUÁRIO
         * ========================================================
         */

        if (usuarioAutenticado == null) {

            throw new AccessDeniedException(
                    "Usuário não autenticado."
            );
        }

        if (!Boolean.TRUE.equals(usuarioAutenticado.getAtivo())) {

            throw new AccessDeniedException(
                    "Usuário inativo."
            );
        }


        /*
         * ========================================================
         * BUSCAR OBRIGAÇÃO
         * ========================================================
         */

        ObrigacaoFiscal obrigacao =
                obrigacaoFiscalRepository
                        .findById(dto.getIdObrigacao())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Obrigação fiscal não encontrada."
                                )
                        );


        /*
         * ========================================================
         * EMPRESA
         * ========================================================
         */

        Empresa empresa =
                obrigacao.getEmpresa();

        if (empresa == null) {

            throw new IllegalArgumentException(
                    "A obrigação não possui uma empresa vinculada."
            );
        }


        /*
         * ========================================================
         * CLIENTE RESPONSÁVEL
         * ========================================================
         */

        Usuario cliente =
                empresa.getCliente();

        if (cliente == null) {

            throw new IllegalArgumentException(
                    "A empresa não possui um cliente responsável."
            );
        }


        /*
         * ========================================================
         * AUTORIZAÇÃO DO CLIENTE
         * ========================================================
         *
         * Um USUARIO comum só pode pagar obrigações pertencentes
         * à própria empresa.
         */

        if (
                usuarioAutenticado.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            if (
                    cliente.getId()
                            != usuarioAutenticado.getId()
            ) {

                throw new AccessDeniedException(
                        "Você não possui permissão para pagar esta obrigação."
                );
            }
        }


        /*
         * ========================================================
         * OBRIGAÇÃO JÁ PAGA
         * ========================================================
         */

        if (
                obrigacao.getStatus()
                        == StatusObrigacao.PAGA
        ) {

            throw new IllegalArgumentException(
                    "Esta obrigação já está paga."
            );
        }


        /*
         * ========================================================
         * MÉTODO DE PAGAMENTO
         * ========================================================
         *
         * Neste momento o sistema trabalha somente com PIX.
         */

        if (
                dto.getMetodoPagamento()
                        != MetodoPagamento.PIX
        ) {

            throw new IllegalArgumentException(
                    "Neste momento, apenas pagamentos via PIX estão disponíveis."
            );
        }


        /*
         * ========================================================
         * VALOR
         * ========================================================
         */

        BigDecimal valor =
                obrigacao.getValor();

        if (
                valor == null
                        ||
                        valor.compareTo(
                                BigDecimal.ZERO
                        ) <= 0
        ) {

            throw new IllegalArgumentException(
                    "A obrigação não possui um valor válido para pagamento."
            );
        }


        /*
         * ========================================================
         * VERIFICAR PAGAMENTO PENDENTE
         * ========================================================
         *
         * Evita gerar vários PIX para a mesma obrigação enquanto
         * já houver um pagamento em aberto.
         */

        boolean existePagamentoEmAberto =
                pagamentoRepository
                        .existsByObrigacao_IdAndStatusIn(
                                obrigacao.getId(),
                                List.of(
                                        StatusPagamento.PENDENTE,
                                        StatusPagamento.PROCESSANDO
                                )
                        );

        if (existePagamentoEmAberto) {

            throw new IllegalArgumentException(
                    "Já existe um pagamento pendente para esta obrigação."
            );
        }


        /*
         * ========================================================
         * DESCRIÇÃO
         * ========================================================
         */

        String descricao =
                "Obrigação "
                        + obrigacao.getTipo()
                        + " - "
                        + empresa.getRazaoSocial();


        /*
         * ========================================================
         * MERCADO PAGO
         * ========================================================
         *
         * Agora utilizamos a Orders API.
         */

        MercadoPagoService.MercadoPagoPixResponse pagamentoMercadoPago =
                mercadoPagoService.criarPagamentoPix(
                        valor,
                        descricao,
                        "test_user_123@testuser.com"
                );


        /*
         * ========================================================
         * DADOS DO PIX
         * ========================================================
         */

        String codigoPix =
                pagamentoMercadoPago.getCodigoPix();

        String qrCodeBase64 =
                pagamentoMercadoPago.getQrCodeBase64();

        String orderId =
                pagamentoMercadoPago.getOrderId();


        /*
         * ========================================================
         * VALIDAR RETORNO DO MERCADO PAGO
         * ========================================================
         */

        if (
                orderId == null
                        ||
                        orderId.isBlank()
        ) {

            throw new IllegalStateException(
                    "O Mercado Pago não retornou o identificador da cobrança."
            );
        }


        if (
                codigoPix == null
                        ||
                        codigoPix.isBlank()
        ) {

            throw new IllegalStateException(
                    "O Mercado Pago não retornou o código PIX."
            );
        }


        /*
         * ========================================================
         * STATUS INICIAL
         * ========================================================
         *
         * action_required + waiting_transfer significa que
         * aguardamos o cliente realizar a transferência PIX.
         */

        StatusPagamento statusPagamento =
                StatusPagamento.PENDENTE;


        /*
         * ========================================================
         * CRIAR PAGAMENTO LOCAL
         * ========================================================
         */

        Pagamento pagamento =
                Pagamento.builder()

                        .usuario(
                                cliente
                        )

                        .empresa(
                                empresa
                        )

                        .obrigacao(
                                obrigacao
                        )

                        .tipoPagamento(
                                TipoPagamento.OBRIGACAO
                        )

                        .metodoPagamento(
                                MetodoPagamento.PIX
                        )

                        .status(
                                statusPagamento
                        )

                        .valor(
                                valor
                        )

                        /*
                         * Guardamos o ID da Order do Mercado Pago.
                         *
                         * Exemplo:
                         * ORDTST01...
                         */
                        .idPagamentoExterno(
                                orderId
                        )

                        .codigoPix(
                                codigoPix
                        )

                        .qrCodePix(
                                qrCodeBase64
                        )

                        .dataCriacao(
                                LocalDateTime.now()
                        )

                        .build();


        /*
         * ========================================================
         * SALVAR
         * ========================================================
         */

        Pagamento salvo =
                pagamentoRepository.save(
                        pagamento
                );


        /*
         * ========================================================
         * RETORNAR DTO
         * ========================================================
         */

        return converterParaDTO(
                salvo
        );
    }


    /*
     * ============================================================
     * LISTAR TODOS
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarTodos() {

        return pagamentoRepository
                .findAll()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * LISTAR POR USUÁRIO
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPorUsuario(
            Integer idUsuario
    ) {

        return pagamentoRepository
                .findByUsuario_IdOrderByDataCriacaoDesc(
                        idUsuario
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * LISTAR POR EMPRESA
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPorEmpresa(
            Integer idEmpresa
    ) {

        return pagamentoRepository
                .findByEmpresa_IdEmpresaOrderByDataCriacaoDesc(
                        idEmpresa
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * LISTAR POR OBRIGAÇÃO
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPorObrigacao(
            Integer idObrigacao
    ) {

        return pagamentoRepository
                .findByObrigacao_IdOrderByDataCriacaoDesc(
                        idObrigacao
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * BUSCAR POR ID
     * ============================================================
     */

    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarPorId(
            Integer idPagamento
    ) {

        Pagamento pagamento =
                pagamentoRepository
                        .findById(idPagamento)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Pagamento não encontrado."
                                )
                        );


        return converterParaDTO(
                pagamento
        );
    }


    /*
     * ============================================================
     * BUSCAR ENTIDADE
     * ============================================================
     *
     * Pode ser útil posteriormente para webhook e atualização
     * do status do pagamento.
     */

    @Transactional(readOnly = true)
    public Pagamento buscarEntidadePorId(
            Integer idPagamento
    ) {

        return pagamentoRepository
                .findById(idPagamento)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Pagamento não encontrado."
                        )
                );
    }


    /*
     * ============================================================
     * ÚLTIMO PAGAMENTO DA OBRIGAÇÃO
     * ============================================================
     */

    @Transactional(readOnly = true)
    public PagamentoResponseDTO buscarUltimoPagamentoDaObrigacao(
            Integer idObrigacao
    ) {

        Pagamento pagamento =
                pagamentoRepository
                        .findFirstByObrigacao_IdOrderByDataCriacaoDesc(
                                idObrigacao
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Nenhum pagamento encontrado para esta obrigação."
                                )
                        );


        return converterParaDTO(
                pagamento
        );
    }


    /*
     * ============================================================
     * LISTAR POR STATUS
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<PagamentoResponseDTO> listarPorStatus(
            StatusPagamento status
    ) {

        return pagamentoRepository
                .findByStatusOrderByDataCriacaoDesc(
                        status
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * CONVERTER ENTITY -> DTO
     * ============================================================
     */

    private PagamentoResponseDTO converterParaDTO(
            Pagamento pagamento
    ) {

        Usuario usuario =
                pagamento.getUsuario();

        Empresa empresa =
                pagamento.getEmpresa();

        ObrigacaoFiscal obrigacao =
                pagamento.getObrigacao();


        return PagamentoResponseDTO
                .builder()

                .id(
                        pagamento.getId()
                )

                .idUsuario(
                        usuario != null
                                ? usuario.getId()
                                : null
                )

                .nomeUsuario(
                        usuario != null
                                ? usuario.getNome()
                                : null
                )

                .idEmpresa(
                        empresa != null
                                ? empresa.getIdEmpresa()
                                : null
                )

                .razaoSocialEmpresa(
                        empresa != null
                                ? empresa.getRazaoSocial()
                                : null
                )

                .idObrigacao(
                        obrigacao != null
                                ? obrigacao.getId()
                                : null
                )

                .tipoPagamento(
                        pagamento.getTipoPagamento()
                )

                .metodoPagamento(
                        pagamento.getMetodoPagamento()
                )

                .status(
                        pagamento.getStatus()
                )

                .valor(
                        pagamento.getValor()
                )

                .idPagamentoExterno(
                        pagamento.getIdPagamentoExterno()
                )

                .codigoPix(
                        pagamento.getCodigoPix()
                )

                .qrCodePix(
                        pagamento.getQrCodePix()
                )

                .dataCriacao(
                        pagamento.getDataCriacao()
                )

                .dataPagamento(
                        pagamento.getDataPagamento()
                )

                .dataExpiracao(
                        pagamento.getDataExpiracao()
                )

                .build();
    }
}