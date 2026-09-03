package com.ivig.sistemaconsultoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RedefinirSenhaRequestDTO {

    @NotBlank(message = "O token é obrigatório.")
    private String token;

    @NotBlank(message = "A nova senha é obrigatória.")
    @Size(
            min = 8,
            message = "A senha deve possuir no mínimo 8 caracteres."
    )
    private String novaSenha;
}
