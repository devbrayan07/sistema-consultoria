package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.dto.DocumentoResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.service.DocumentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
@RequiredArgsConstructor
@Tag(
        name = "Documentos",
        description = "Endpoints para gerenciamento de documentos"
)
public class DocumentoController {

    private final DocumentoService documentoService;


    /*
     * ============================================================
     * UPLOAD
     * CONTADOR / ADMINISTRADOR
     * ============================================================
     */

    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Cadastra um novo documento com upload de arquivo"
    )
    public ResponseEntity<DocumentoResponseDTO> criar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("idEmpresa") Integer idEmpresa,
            @RequestParam("tipo") String tipo,
            @RequestParam("competencia") String competencia,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);

        if (
                usuario.getTipo() != TipoUsuario.CONTADOR
                        &&
                        usuario.getTipo() != TipoUsuario.ADMINISTRADOR
        ) {

            throw new AccessDeniedException(
                    "Você não possui permissão para enviar documentos."
            );
        }

        DocumentoResponseDTO documentoCriado =
                documentoService.criarComArquivo(
                        file,
                        idEmpresa,
                        usuario.getId(),
                        tipo,
                        competencia
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(documentoCriado);
    }


    /*
     * ============================================================
     * LISTAGEM
     * ============================================================
     */

    @GetMapping
    @Operation(
            summary = "Lista documentos disponíveis para o usuário autenticado"
    )
    public ResponseEntity<List<DocumentoResponseDTO>> listarTodos(
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);

        if (usuario.getTipo() == TipoUsuario.USUARIO) {

            return ResponseEntity.ok(
                    documentoService.listarPorUsuario(
                            usuario.getId()
                    )
            );
        }

        return ResponseEntity.ok(
                documentoService.listarTodos()
        );
    }


    /*
     * ============================================================
     * LISTAGEM POR EMPRESA
     * ============================================================
     */

    @GetMapping("/empresa/{idEmpresa}")
    @Operation(
            summary = "Lista documentos de uma empresa"
    )
    public ResponseEntity<List<DocumentoResponseDTO>> listarPorEmpresa(
            @PathVariable Integer idEmpresa,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);

        if (usuario.getTipo() == TipoUsuario.USUARIO) {

            boolean possuiAcesso =
                    documentoService.empresaPertenceAoUsuario(
                            idEmpresa,
                            usuario.getId()
                    );

            if (!possuiAcesso) {

                throw new AccessDeniedException(
                        "Você não possui acesso aos documentos desta empresa."
                );
            }
        }

        return ResponseEntity.ok(
                documentoService.listarPorEmpresa(
                        idEmpresa
                )
        );
    }


    /*
     * ============================================================
     * BUSCAR DOCUMENTO
     * ============================================================
     */

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca um documento pelo seu ID"
    )
    public ResponseEntity<DocumentoResponseDTO> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);

        if (usuario.getTipo() == TipoUsuario.USUARIO) {

            boolean possuiAcesso =
                    documentoService.documentoPertenceAoUsuario(
                            id,
                            usuario.getId()
                    );

            if (!possuiAcesso) {

                throw new AccessDeniedException(
                        "Você não possui acesso a este documento."
                );
            }
        }

        return ResponseEntity.ok(
                documentoService.buscarPorId(id)
        );
    }


    /*
     * ============================================================
     * DOWNLOAD / VISUALIZAÇÃO
     * ============================================================
     */

    @GetMapping("/{id}/download")
    @Operation(
            summary = "Visualiza ou baixa o arquivo do documento por ID"
    )
    public ResponseEntity<Resource> visualizar(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);


        /*
         * USUARIO comum só pode acessar arquivos
         * pertencentes às próprias empresas.
         */
        if (usuario.getTipo() == TipoUsuario.USUARIO) {

            boolean possuiAcesso =
                    documentoService.documentoPertenceAoUsuario(
                            id,
                            usuario.getId()
                    );

            if (!possuiAcesso) {

                throw new AccessDeniedException(
                        "Você não possui acesso a este documento."
                );
            }
        }


        DocumentoResponseDTO documento =
                documentoService.buscarPorId(id);

        Resource resource =
                documentoService.carregarArquivoComoRecurso(id);

        String nomeArquivo =
                documento.getNomeArquivo();

        if (
                nomeArquivo == null
                        ||
                        nomeArquivo.isBlank()
        ) {

            nomeArquivo =
                    resource.getFilename();
        }

        if (
                nomeArquivo == null
                        ||
                        nomeArquivo.isBlank()
        ) {

            nomeArquivo =
                    "documento";
        }


        MediaType mediaType =
                obterMediaType(nomeArquivo);

        boolean podeAbrirNoNavegador =
                podeVisualizarInline(nomeArquivo);


        ContentDisposition contentDisposition;

        if (podeAbrirNoNavegador) {

            contentDisposition =
                    ContentDisposition
                            .inline()
                            .filename(
                                    nomeArquivo,
                                    StandardCharsets.UTF_8
                            )
                            .build();

        } else {

            contentDisposition =
                    ContentDisposition
                            .attachment()
                            .filename(
                                    nomeArquivo,
                                    StandardCharsets.UTF_8
                            )
                            .build();
        }


        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        contentDisposition.toString()
                )
                .body(resource);
    }


    /*
     * ============================================================
     * USUÁRIO AUTENTICADO
     * ============================================================
     */

    private Usuario obterUsuarioAutenticado(
            Authentication authentication
    ) {

        if (
                authentication == null
                        ||
                        !(authentication.getPrincipal()
                                instanceof Usuario usuario)
        ) {

            throw new AccessDeniedException(
                    "Usuário não autenticado."
            );
        }

        return usuario;
    }


    /*
     * ============================================================
     * VISUALIZAÇÃO INLINE
     * ============================================================
     */

    private boolean podeVisualizarInline(
            String nomeArquivo
    ) {

        String nome =
                nomeArquivo.toLowerCase();

        return nome.endsWith(".pdf")
                || nome.endsWith(".png")
                || nome.endsWith(".jpg")
                || nome.endsWith(".jpeg")
                || nome.endsWith(".gif")
                || nome.endsWith(".webp")
                || nome.endsWith(".txt");
    }


    /*
     * ============================================================
     * MIME TYPE
     * ============================================================
     */

    private MediaType obterMediaType(
            String nomeArquivo
    ) {

        String nome =
                nomeArquivo.toLowerCase();


        if (nome.endsWith(".pdf")) {

            return MediaType.APPLICATION_PDF;
        }


        if (nome.endsWith(".png")) {

            return MediaType.IMAGE_PNG;
        }


        if (
                nome.endsWith(".jpg")
                        ||
                        nome.endsWith(".jpeg")
        ) {

            return MediaType.IMAGE_JPEG;
        }


        if (nome.endsWith(".gif")) {

            return MediaType.parseMediaType(
                    "image/gif"
            );
        }


        if (nome.endsWith(".webp")) {

            return MediaType.parseMediaType(
                    "image/webp"
            );
        }


        if (nome.endsWith(".txt")) {

            return MediaType.TEXT_PLAIN;
        }


        if (nome.endsWith(".doc")) {

            return MediaType.parseMediaType(
                    "application/msword"
            );
        }


        if (nome.endsWith(".docx")) {

            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            );
        }


        if (nome.endsWith(".xls")) {

            return MediaType.parseMediaType(
                    "application/vnd.ms-excel"
            );
        }


        if (nome.endsWith(".xlsx")) {

            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
        }


        if (nome.endsWith(".ppt")) {

            return MediaType.parseMediaType(
                    "application/vnd.ms-powerpoint"
            );
        }


        if (nome.endsWith(".pptx")) {

            return MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            );
        }


        return MediaType.APPLICATION_OCTET_STREAM;
    }
}