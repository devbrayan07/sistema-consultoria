package com.ivig.sistemaconsultoria.model;

import com.ivig.sistemaconsultoria.enums.MetodoPagamento;
import com.ivig.sistemaconsultoria.enums.StatusPagamento;
import com.ivig.sistemaconsultoria.enums.TipoPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pagamento")
    private Integer id;


    /*
     * ============================================================
     * RELACIONAMENTOS
     * ============================================================
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_usuario",
            nullable = false
    )
    private Usuario usuario;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_empresa",
            nullable = false
    )
    private Empresa empresa;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_obrigacao_fiscal"
    )
    private ObrigacaoFiscal obrigacao;


    /*
     * ============================================================
     * CLASSIFICAÇÃO
     * ============================================================
     */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_pagamento",
            nullable = false,
            length = 30
    )
    private TipoPagamento tipoPagamento;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "metodo_pagamento",
            nullable = false,
            length = 30
    )
    private MetodoPagamento metodoPagamento;


    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private StatusPagamento status =
            StatusPagamento.PENDENTE;


    /*
     * ============================================================
     * VALORES
     * ============================================================
     */

    @Column(
            name = "valor_original",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valorOriginal;


    @Column(
            name = "valor_cobrado",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valorCobrado;


    /*
     * ============================================================
     * INTEGRAÇÃO COM GATEWAY
     * ============================================================
     */

    @Column(
            name = "id_pagamento_externo",
            length = 150
    )
    private String idPagamentoExterno;


    @Column(
            name = "external_reference",
            length = 150,
            unique = true
    )
    private String externalReference;


    @Column(
            name = "idempotency_key",
            length = 100,
            unique = true
    )
    private String idempotencyKey;


    @Column(
            name = "url_pagamento",
            length = 1000
    )
    private String urlPagamento;


    /*
     * ============================================================
     * PIX
     * ============================================================
     */

    @Lob
    @Column(
            name = "codigo_pix",
            columnDefinition = "TEXT"
    )
    private String codigoPix;


    @Lob
    @Column(
            name = "qr_code_pix",
            columnDefinition = "LONGTEXT"
    )
    private String qrCodePix;


    /*
     * ============================================================
     * BOLETO
     * ============================================================
     */

    @Column(
            name = "boleto_codigo_barras",
            length = 255
    )
    private String boletoCodigoBarras;


    /*
     * ============================================================
     * MOTIVO / DETALHES
     * ============================================================
     */

    @Column(
            name = "motivo_recusa",
            length = 500
    )
    private String motivoRecusa;


    /*
     * ============================================================
     * DATAS
     * ============================================================
     */

    @Column(
            name = "data_criacao",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime dataCriacao =
            LocalDateTime.now();


    @Column(
            name = "data_atualizacao"
    )
    private LocalDateTime dataAtualizacao;


    @Column(
            name = "data_pagamento"
    )
    private LocalDateTime dataPagamento;


    @Column(
            name = "data_expiracao"
    )
    private LocalDateTime dataExpiracao;


    @Column(
            name = "data_cancelamento"
    )
    private LocalDateTime dataCancelamento;


    /*
     * ============================================================
     * CALLBACKS JPA
     * ============================================================
     */

    @PrePersist
    public void prePersist() {

        LocalDateTime agora =
                LocalDateTime.now();

        if (dataCriacao == null) {
            dataCriacao = agora;
        }

        dataAtualizacao = agora;

        if (status == null) {
            status =
                    StatusPagamento.PENDENTE;
        }
    }


    @PreUpdate
    public void preUpdate() {

        dataAtualizacao =
                LocalDateTime.now();
    }


    @Column(
            name = "id_order_externo",
            length = 150
    )
    private String idOrderExterno;

    @Column(
            name = "id_transacao_externa",
            length = 150
    )
    private String idTransacaoExterna;
}