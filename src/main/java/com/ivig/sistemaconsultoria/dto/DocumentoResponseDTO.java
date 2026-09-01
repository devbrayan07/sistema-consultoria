package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoResponseDTO {

    private int id;
    private TipoDocumento tipo;
    private String nomeArquivo;
    private String urlArquivo;
    private LocalDate competencia;
    private Integer idEnviadoPor;
    private String nomeEnviadoPor;
    private Integer idEmpresa;
    private String razaoSocialEmpresa;
    private LocalDateTime criadoEm;
}