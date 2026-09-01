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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id",
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
            nullable = false,
            length = 30
    )
    @Builder.Default
    private StatusPagamento status =
            StatusPagamento.PENDENTE;

    @Column(
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valor;

    @Column(
            name = "id_pagamento_externo",
            length = 150
    )
    private String idPagamentoExterno;

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

    @Column(
            name = "data_criacao",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime dataCriacao =
            LocalDateTime.now();

    @Column(
            name = "data_pagamento"
    )
    private LocalDateTime dataPagamento;

    @Column(
            name = "data_expiracao"
    )
    private LocalDateTime dataExpiracao;
}

