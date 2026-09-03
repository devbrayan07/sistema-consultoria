package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.MetodoPagamento;
import com.ivig.sistemaconsultoria.enums.StatusPagamento;
import com.ivig.sistemaconsultoria.enums.TipoPagamento;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagamentoResponseDTO {

    /*
     * ============================================================
     * IDENTIFICAÇÃO
     * ============================================================
     */

    private Integer id;


    /*
     * ============================================================
     * USUÁRIO
     * ============================================================
     */

    private Integer idUsuario;

    private String nomeUsuario;


    /*
     * ============================================================
     * EMPRESA
     * ============================================================
     */

    private Integer idEmpresa;

    private String razaoSocialEmpresa;


    /*
     * ============================================================
     * OBRIGAÇÃO
     * ============================================================
     */

    private Integer idObrigacao;


    /*
     * ============================================================
     * PAGAMENTO
     * ============================================================
     */

    private TipoPagamento tipoPagamento;

    private MetodoPagamento metodoPagamento;

    private StatusPagamento status;

    private String idOrderExterno;

    private String idTransacaoExterna;


    /*
     * ============================================================
     * VALORES
     * ============================================================
     */

    private BigDecimal valorOriginal;

    private BigDecimal valorCobrado;


    /*
     * ============================================================
     * GATEWAY
     * ============================================================
     */

    private String idPagamentoExterno;

    private String externalReference;

    private String urlPagamento;


    /*
     * ============================================================
     * PIX
     * ============================================================
     */

    private String codigoPix;

    private String qrCodePix;


    /*
     * ============================================================
     * BOLETO
     * ============================================================
     */

    private String boletoCodigoBarras;


    /*
     * ============================================================
     * DETALHES
     * ============================================================
     */

    private String motivoRecusa;


    /*
     * ============================================================
     * DATAS
     * ============================================================
     */

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    private LocalDateTime dataPagamento;

    private LocalDateTime dataExpiracao;

    private LocalDateTime dataCancelamento;
}