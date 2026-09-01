package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.dto.EmpresaRequestDTO;
import com.ivig.sistemaconsultoria.dto.EmpresaResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Tag(
        name = "Empresas",
        description = "Endpoints para gerenciamento das empresas clientes"
)
public class EmpresaController {

    private final EmpresaService empresaService;


    /*
     * ============================================================
     * CRIAR EMPRESA
     * USUARIO / CONTADOR / ADMINISTRADOR
     * ============================================================
     */

    @PostMapping
    @Operation(
            summary = "Cadastra uma nova empresa"
    )
    public ResponseEntity<EmpresaResponseDTO> criar(
            @RequestBody @Valid EmpresaRequestDTO dto,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(
                        authentication
                );


        EmpresaResponseDTO empresa =
                empresaService.criar(
                        dto,
                        usuario
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(empresa);
    }


    /*
     * ============================================================
     * LISTAR EMPRESAS
     * ============================================================
     */

    @GetMapping
    @Operation(
            summary = "Lista empresas de acordo com o usuário autenticado"
    )
    public ResponseEntity<List<EmpresaResponseDTO>> listarTodas(
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(
                        authentication
                );


        /*
         * USUARIO:
         * visualiza somente as próprias empresas.
         */
        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            return ResponseEntity.ok(
                    empresaService.listarPorUsuario(
                            usuario.getId()
                    )
            );
        }


        /*
         * CONTADOR / ADMINISTRADOR:
         * visualizam todas as empresas.
         */
        return ResponseEntity.ok(
                empresaService.listarTodas()
        );
    }


    /*
     * ============================================================
     * BUSCAR EMPRESA POR ID
     * ============================================================
     */

    @GetMapping("/{id}")
    @Operation(
            summary = "Busca uma empresa pelo seu ID"
    )
    public ResponseEntity<EmpresaResponseDTO> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(
                        authentication
                );


        /*
         * USUARIO:
         * só pode acessar empresa vinculada à própria conta.
         */
        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            boolean pertenceAoUsuario =
                    empresaService
                            .empresaPertenceAoUsuario(
                                    id,
                                    usuario.getId()
                            );


            if (!pertenceAoUsuario) {

                throw new AccessDeniedException(
                        "Você não possui acesso a esta empresa."
                );
            }
        }


        return ResponseEntity.ok(
                empresaService.buscarPorId(
                        id
                )
        );
    }


    /*
     * ============================================================
     * RECUPERAR USUÁRIO AUTENTICADO
     * ============================================================
     */

    private Usuario obterUsuarioAutenticado(
            Authentication authentication
    ) {

        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
        ) {

            throw new AccessDeniedException(
                    "Usuário não autenticado."
            );
        }


        Object principal =
                authentication.getPrincipal();


        if (
                !(principal instanceof Usuario usuario)
        ) {

            throw new AccessDeniedException(
                    "Não foi possível identificar o usuário autenticado."
            );
        }


        if (
                !Boolean.TRUE.equals(
                        usuario.getAtivo()
                )
        ) {

            throw new AccessDeniedException(
                    "Usuário inativo."
            );
        }


        return usuario;
    }
}