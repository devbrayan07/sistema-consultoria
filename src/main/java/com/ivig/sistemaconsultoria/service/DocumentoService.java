package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.dto.DocumentoResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoDocumento;
import com.ivig.sistemaconsultoria.exceptions.ResourceNotFoundException;
import com.ivig.sistemaconsultoria.model.Documento;
import com.ivig.sistemaconsultoria.model.Empresa;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.DocumentoRepository;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final EmpresaService empresaService;
    private final UsuarioRepository usuarioRepository;


    /*
     * ============================================================
     * CRIAR DOCUMENTO
     * ============================================================
     */

    @Transactional
    public DocumentoResponseDTO criarComArquivo(
            MultipartFile file,
            Integer idEmpresa,
            Integer idEnviadoPor,
            String tipo,
            String competencia
    ) {

        /*
         * Validação básica do arquivo.
         */
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "O arquivo do documento é obrigatório."
            );
        }


        Empresa empresa =
                empresaService.buscarEntidadePorId(
                        idEmpresa
                );


        Usuario enviadoPor =
                usuarioRepository
                        .findById(idEnviadoPor)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Usuário remetente não encontrado."
                                )
                        );


        /*
         * ========================================================
         * TIPO DO DOCUMENTO
         * ========================================================
         */

        TipoDocumento tipoDocumento;

        try {

            tipoDocumento =
                    TipoDocumento.valueOf(
                            tipo
                                    .trim()
                                    .toUpperCase()
                    );

        } catch (
                NullPointerException
                |
                IllegalArgumentException e
        ) {

            throw new IllegalArgumentException(
                    "Tipo de documento inválido: " + tipo
            );
        }


        /*
         * ========================================================
         * COMPETÊNCIA
         * ========================================================
         */

        LocalDate competenciaDocumento;

        try {

            competenciaDocumento =
                    LocalDate.parse(
                            competencia
                    );

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Competência inválida."
            );
        }


        /*
         * ========================================================
         * ARQUIVO FÍSICO
         * ========================================================
         */

        String caminhoSalvo =
                salvarArquivoNoDisco(
                        file
                );


        Documento documento =
                Documento.builder()
                        .tipo(
                                tipoDocumento
                        )
                        .nomeArquivo(
                                file.getOriginalFilename()
                        )
                        .urlArquivo(
                                caminhoSalvo
                        )
                        .competencia(
                                competenciaDocumento
                        )
                        .enviadoPor(
                                enviadoPor
                        )
                        .empresa(
                                empresa
                        )
                        .build();


        Documento documentoSalvo =
                documentoRepository.save(
                        documento
                );


        return converterParaDTO(
                documentoSalvo
        );
    }


    /*
     * ============================================================
     * LISTAR TODOS
     *
     * CONTADOR / ADMINISTRADOR
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<DocumentoResponseDTO> listarTodos() {

        return documentoRepository
                .findAll()
                .stream()
                .map(
                        this::converterParaDTO
                )
                .toList();
    }


    /*
     * ============================================================
     * LISTAR DOCUMENTOS DO USUÁRIO
     * ============================================================
     *
     * Retorna somente documentos pertencentes às empresas
     * vinculadas ao usuário.
     */

    @Transactional(readOnly = true)
    public List<DocumentoResponseDTO> listarPorUsuario(
            Integer idUsuario
    ) {

        return documentoRepository
                .findByEmpresa_Cliente_Id(
                        idUsuario
                )
                .stream()
                .map(
                        this::converterParaDTO
                )
                .toList();
    }


    /*
     * ============================================================
     * LISTAR POR EMPRESA
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<DocumentoResponseDTO> listarPorEmpresa(
            Integer idEmpresa
    ) {

        return documentoRepository
                .findByEmpresaIdEmpresa(
                        idEmpresa
                )
                .stream()
                .map(
                        this::converterParaDTO
                )
                .toList();
    }


    /*
     * ============================================================
     * BUSCAR DOCUMENTO
     * ============================================================
     */

    @Transactional(readOnly = true)
    public DocumentoResponseDTO buscarPorId(
            Integer id
    ) {

        Documento documento =
                buscarEntidadePorId(
                        id
                );

        return converterParaDTO(
                documento
        );
    }


    /*
     * ============================================================
     * BUSCAR ENTIDADE DOCUMENTO
     * ============================================================
     */

    @Transactional(readOnly = true)
    public Documento buscarEntidadePorId(
            Integer id
    ) {

        return documentoRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Documento não encontrado com ID: " + id
                        )
                );
    }


    /*
     * ============================================================
     * EMPRESA PERTENCE AO USUÁRIO?
     * ============================================================
     */

    @Transactional(readOnly = true)
    public boolean empresaPertenceAoUsuario(
            Integer idEmpresa,
            Integer idUsuario
    ) {

        return empresaService
                .empresaPertenceAoUsuario(
                        idEmpresa,
                        idUsuario
                );
    }


    /*
     * ============================================================
     * DOCUMENTO PERTENCE AO USUÁRIO?
     * ============================================================
     *
     * Documento
     *    ↓
     * Empresa
     *    ↓
     * Cliente
     *    ↓
     * usuário autenticado
     */

    @Transactional(readOnly = true)
    public boolean documentoPertenceAoUsuario(
            Integer idDocumento,
            Integer idUsuario
    ) {

        Documento documento =
                buscarEntidadePorId(
                        idDocumento
                );


        if (documento.getEmpresa() == null) {
            return false;
        }


        Empresa empresa =
                documento.getEmpresa();


        if (empresa.getCliente() == null) {
            return false;
        }


        return Integer.valueOf(
                empresa
                        .getCliente()
                        .getId()
        ).equals(
                idUsuario
        );
    }


    /*
     * ============================================================
     * SALVAR ARQUIVO NO SERVIDOR
     * ============================================================
     */

    private String salvarArquivoNoDisco(
            MultipartFile file
    ) {

        try {

            Path diretorio =
                    Paths
                            .get("uploads")
                            .toAbsolutePath()
                            .normalize();


            if (!Files.exists(diretorio)) {

                Files.createDirectories(
                        diretorio
                );
            }


            String nomeOriginal =
                    file.getOriginalFilename();


            if (
                    nomeOriginal == null
                            ||
                            nomeOriginal.isBlank()
            ) {

                nomeOriginal =
                        "arquivo";
            }


            /*
             * Evita path traversal.
             *
             * Exemplo bloqueado:
             *
             * ../../arquivo.pdf
             */

            nomeOriginal =
                    Paths
                            .get(nomeOriginal)
                            .getFileName()
                            .toString();


            /*
             * Nome físico único no servidor.
             */

            String nomeUnico =
                    System.currentTimeMillis()
                            +
                            "_"
                            +
                            nomeOriginal;


            Path caminhoCompleto =
                    diretorio
                            .resolve(
                                    nomeUnico
                            )
                            .normalize();


            /*
             * Garante que o arquivo continue dentro
             * da pasta uploads.
             */

            if (
                    !caminhoCompleto.startsWith(
                            diretorio
                    )
            ) {

                throw new IllegalArgumentException(
                        "Nome de arquivo inválido."
                );
            }


            Files.copy(
                    file.getInputStream(),
                    caminhoCompleto,
                    StandardCopyOption.REPLACE_EXISTING
            );


            return caminhoCompleto
                    .toString();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Erro ao salvar arquivo físico no servidor.",
                    e
            );
        }
    }


    /*
     * ============================================================
     * CARREGAR ARQUIVO
     * ============================================================
     */

    @Transactional(readOnly = true)
    public Resource carregarArquivoComoRecurso(
            Integer id
    ) {

        Documento documento =
                buscarEntidadePorId(
                        id
                );


        if (
                documento.getUrlArquivo() == null
                        ||
                        documento.getUrlArquivo().isBlank()
        ) {

            throw new ResourceNotFoundException(
                    "O documento não possui arquivo associado."
            );
        }


        try {

            Path caminho =
                    Paths
                            .get(
                                    documento.getUrlArquivo()
                            )
                            .toAbsolutePath()
                            .normalize();


            Resource resource =
                    new UrlResource(
                            caminho.toUri()
                    );


            if (
                    resource.exists()
                            &&
                            resource.isReadable()
            ) {

                return resource;
            }


            throw new ResourceNotFoundException(
                    "Arquivo físico não encontrado no servidor."
            );

        } catch (MalformedURLException e) {

            throw new RuntimeException(
                    "Caminho do arquivo inválido.",
                    e
            );
        }
    }


    /*
     * ============================================================
     * ENTIDADE -> DTO
     * ============================================================
     */

    private DocumentoResponseDTO converterParaDTO(
            Documento documento
    ) {

        return DocumentoResponseDTO
                .builder()

                .id(
                        documento.getId()
                )

                .tipo(
                        documento.getTipo()
                )

                .nomeArquivo(
                        documento.getNomeArquivo()
                )

                .urlArquivo(
                        documento.getUrlArquivo()
                )

                .competencia(
                        documento.getCompetencia()
                )

                .idEnviadoPor(
                        documento.getEnviadoPor() != null
                                ? documento
                                .getEnviadoPor()
                                .getId()
                                : null
                )

                .nomeEnviadoPor(
                        documento.getEnviadoPor() != null
                                ? documento
                                .getEnviadoPor()
                                .getNome()
                                : null
                )

                .idEmpresa(
                        documento.getEmpresa() != null
                                ? documento
                                .getEmpresa()
                                .getIdEmpresa()
                                : null
                )

                .razaoSocialEmpresa(
                        documento.getEmpresa() != null
                                ? documento
                                .getEmpresa()
                                .getRazaoSocial()
                                : null
                )

                .criadoEm(
                        documento.getCriadoEm()
                )

                .build();
    }
}