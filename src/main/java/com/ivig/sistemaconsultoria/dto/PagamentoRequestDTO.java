package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.MetodoPagamento;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PagamentoRequestDTO {

    @NotNull(message = "O ID da obrigação é obrigatório.")
    private Integer idObrigacao;

    @NotNull(message = "O método de pagamento é obrigatório.")
    private MetodoPagamento metodoPagamento;
}
