package com.ivig.sistemaconsultoria.model;

import com.ivig.sistemaconsultoria.enums.StatusObrigacao;
import com.ivig.sistemaconsultoria.enums.TipoObrigacao;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "obrigacao_fiscal")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ObrigacaoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_obrigacao_fiscal")
    private int id;

    @Enumerated(EnumType.STRING)
    private TipoObrigacao tipo;

    private LocalDate competencia;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa", nullable = false)
    private Empresa empresa;

    private BigDecimal valor;

    private BigDecimal honorario;

    @Enumerated(EnumType.STRING)
    private StatusObrigacao status;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;



}