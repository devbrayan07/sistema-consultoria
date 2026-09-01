package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.StatusObrigacao;
import com.ivig.sistemaconsultoria.enums.TipoObrigacao;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ObrigacaoFiscalRequestDTO {

    @NotNull(message = "O tipo da obrigação é obrigatório")
    private TipoObrigacao tipo;

    @NotNull(message = "A data de vencimento é obrigatória")
    private LocalDate dataVencimento;

    private LocalDate competencia;

    @NotNull(message = "O ID da empresa é obrigatório")
    private Integer idEmpresa;
    private BigDecimal valor;
    private BigDecimal honorario;
    private StatusObrigacao status;
}
