package com.ivig.sistemaconsultoria.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RelatorioFinanceiroRequestDTO {

    @NotNull(message = "O ID da empresa é obrigatório")
    private Integer idEmpresa;

    @NotNull(message = "O período é obrigatório")
    private LocalDate periodo;

    @NotNull(message = "A receita é obrigatória")
    private BigDecimal receita;

    @NotNull(message = "A despesa é obrigatória")
    private BigDecimal despesa;

    private BigDecimal impostosPagos;
}