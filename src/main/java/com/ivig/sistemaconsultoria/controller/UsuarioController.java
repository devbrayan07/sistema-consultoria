package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.dto.UsuarioRequestDTO;
import com.ivig.sistemaconsultoria.dto.UsuarioResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;


    /*
     * ============================================================
     * CRIAR USUÁRIO
     *
     * CONTADOR:
     * somente USUARIO
     *
     * ADMINISTRADOR:
     * qualquer perfil
     * ============================================================
     */

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criar(
            @RequestBody @Valid UsuarioRequestDTO dto,
            Authentication authentication
    ) {

        Usuario usuarioAutenticado =
                obterUsuarioAutenticado(authentication);


        if (usuarioAutenticado.getTipo() == TipoUsuario.USUARIO) {
            throw new AccessDeniedException(
                    "Você não possui permissão para cadastrar usuários."
            );
        }


        UsuarioResponseDTO usuarioCriado =
                usuarioService.criarUsuario(
                        dto,
                        usuarioAutenticado
                );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioCriado);
    }


    /*
     * ============================================================
     * BUSCAR USUÁRIO POR ID
     * ============================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Usuario autenticado =
                obterUsuarioAutenticado(authentication);


        /*
         * USUARIO comum:
         * só pode visualizar a própria conta.
         */
        if (autenticado.getTipo() == TipoUsuario.USUARIO) {

            if (autenticado.getId() != id) {

                throw new AccessDeniedException(
                        "Você não possui acesso a este usuário."
                );
            }
        }


        /*
         * CONTADOR:
         * só pode consultar clientes USUARIO.
         */
        if (autenticado.getTipo() == TipoUsuario.CONTADOR) {

            Usuario alvo =
                    usuarioService.buscarEntidadePorId(id);

            if (alvo.getTipo() != TipoUsuario.USUARIO) {

                throw new AccessDeniedException(
                        "Você não possui acesso a este usuário."
                );
            }
        }


        return ResponseEntity.ok(
                usuarioService.buscarPorId(id)
        );
    }


    /*
     * ============================================================
     * LISTAGEM
     * ============================================================
     */

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos(
            Authentication authentication
    ) {

        Usuario autenticado =
                obterUsuarioAutenticado(authentication);


        /*
         * ADMINISTRADOR:
         * vê absolutamente todas as contas.
         */
        if (
                autenticado.getTipo()
                        == TipoUsuario.ADMINISTRADOR
        ) {

            return ResponseEntity.ok(
                    usuarioService.listarTodos()
            );
        }


        /*
         * CONTADOR:
         * vê somente clientes.
         */
        if (
                autenticado.getTipo()
                        == TipoUsuario.CONTADOR
        ) {

            return ResponseEntity.ok(
                    usuarioService.listarClientes()
            );
        }


        /*
         * USUARIO:
         * não pode listar usuários.
         */
        throw new AccessDeniedException(
                "Você não possui permissão para listar usuários."
        );
    }


    /*
     * ============================================================
     * PERFIL DO USUÁRIO LOGADO
     * ============================================================
     */

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> meuPerfil(
            Authentication authentication
    ) {

        Usuario autenticado =
                obterUsuarioAutenticado(authentication);

        return ResponseEntity.ok(
                usuarioService.buscarPorId(
                        autenticado.getId()
                )
        );
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
}