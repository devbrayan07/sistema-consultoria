package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UsuarioResponseDTO {


    private Integer id;
    private String nome;
    private String email;
    private TipoUsuario tipo;
    private Boolean ativo;
    private LocalDateTime criadoEm;
}
