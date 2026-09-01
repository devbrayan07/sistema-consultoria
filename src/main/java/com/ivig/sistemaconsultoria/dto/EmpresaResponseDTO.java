package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.RegimeTributario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponseDTO {

    private Integer idEmpresa;
    private String razaoSocial;
    private String nomeFantasia;
    private String cnpj;
    private RegimeTributario regimeTributario;
    private Integer idUsuarioCliente;
    private String nomeCliente;
    private LocalDateTime criadoEm;
}