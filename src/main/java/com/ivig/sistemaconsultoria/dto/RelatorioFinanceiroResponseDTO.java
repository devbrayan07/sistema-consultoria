package com.ivig.sistemaconsultoria.dto;

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
public class RelatorioFinanceiroResponseDTO {

    private Integer id;
    private Integer idEmpresa;
    private String razaoSocialEmpresa;
    private LocalDate periodo;
    private BigDecimal receita;
    private BigDecimal despesa;
    private BigDecimal impostosPagos;
    private LocalDateTime geradoEm;
    private String nomeGeradoPor;
}