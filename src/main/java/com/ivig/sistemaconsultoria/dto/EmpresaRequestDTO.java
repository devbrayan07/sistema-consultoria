package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.RegimeTributario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EmpresaRequestDTO {

    @NotBlank(message = "A razão social é obrigatória")
    private String razaoSocial;

    private String nomeFantasia;

    @NotBlank(message = "O CNPJ é obrigatório")
    @Pattern(regexp = "\\d{14}", message = "O CNPJ deve conter exatamente 14 dígitos.")
    private String cnpj;

    @NotNull(message = "O regime tributário é obrigatório")
    private RegimeTributario regimeTributario;


    private Integer idUsuarioCliente;


}