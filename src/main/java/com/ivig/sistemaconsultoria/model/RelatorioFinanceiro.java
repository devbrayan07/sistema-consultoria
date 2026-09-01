package com.ivig.sistemaconsultoria.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "relatorio_financeiro")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RelatorioFinanceiro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_relatorio_financeiro")
    private int id;

    private LocalDate periodo;

    private BigDecimal receita;

    private BigDecimal despesa;

    @Column(name = "impostos_pagos")
    private BigDecimal impostosPagos;

    @Column(name = "gerado_em", insertable = false, updatable = false)
    private LocalDateTime geradoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gerado_por")
    private Usuario geradoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;
}