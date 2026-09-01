package com.ivig.sistemaconsultoria.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponseDTO {

    private Integer id;
    private String nome;
    private String token;
    private String tipo;
    private String email;
}
