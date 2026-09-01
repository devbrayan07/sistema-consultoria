package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.StatusObrigacao;
import com.ivig.sistemaconsultoria.enums.TipoObrigacao; // <-- Verifique o seu Enum de tipo
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObrigacaoFiscalResponseDTO {

    private Integer id;
    private TipoObrigacao tipo; // <-- Garanta que esse campo existe
    private LocalDate competencia;
    private LocalDate dataVencimento;
    private LocalDate dataPagamento;
    private BigDecimal valor;
    private BigDecimal honorario;
    private StatusObrigacao status;
    private Integer idEmpresa;
    private String razaoSocialEmpresa;
    private LocalDateTime criadoEm;
}