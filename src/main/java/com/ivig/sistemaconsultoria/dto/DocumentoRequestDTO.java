package com.ivig.sistemaconsultoria.dto;

import com.ivig.sistemaconsultoria.enums.TipoDocumento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DocumentoRequestDTO {

    @NotNull(message = "O tipo do documento é obrigatório")
    private TipoDocumento tipo;

    @NotBlank(message = "O nome do arquivo é obrigatório")
    private String nomeArquivo;

    @NotBlank(message = "A URL do arquivo é obrigatória")
    private String urlArquivo;

    @NotNull(message = "A data de competência é obrigatória")
    private LocalDate competencia;

    @NotNull(message = "O ID do usuário remetente é obrigatório")
    private Integer idEnviadoPor;

    @NotNull(message = "O ID da empresa é obrigatório")
    private Integer idEmpresa;
}