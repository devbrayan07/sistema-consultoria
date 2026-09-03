package com.ivig.sistemaconsultoria.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ivig.sistemaconsultoria.dto.PagamentoRequestDTO;
import com.ivig.sistemaconsultoria.dto.PagamentoResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.service.PagamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos")
@RequiredArgsConstructor
public class PagamentoController {

    private final PagamentoService pagamentoService;


    /*
     * =========================================================
     * CRIAR PAGAMENTO
     * =========================================================
     */

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> criar(
            @RequestBody @Valid PagamentoRequestDTO dto,
            Authentication authentication
    ) throws JsonProcessingException {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);

        PagamentoResponseDTO pagamento =
                pagamentoService.criarPagamento(
                        dto,
                        usuario
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(pagamento);
    }


    /*
     * =========================================================
     * LISTAR PAGAMENTOS
     * =========================================================
     */

    @GetMapping
    public ResponseEntity<List<PagamentoResponseDTO>> listar(
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);


        /*
         * USUARIO vê somente os próprios pagamentos.
         */

        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            return ResponseEntity.ok(
                    pagamentoService.listarPorUsuario(
                            usuario.getId()
                    )
            );
        }


        /*
         * CONTADOR e ADMINISTRADOR
         * visualizam todos.
         */

        return ResponseEntity.ok(
                pagamentoService.listarTodos()
        );
    }


    /*
     * =========================================================
     * BUSCAR POR ID
     * =========================================================
     */

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscarPorId(
            @PathVariable Integer id,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);


        PagamentoResponseDTO pagamento =
                pagamentoService.buscarPorId(
                        id
                );


        /*
         * USUARIO só pode consultar pagamento próprio.
         */

        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            if (
                    pagamento.getIdUsuario() == null
                            ||
                            pagamento.getIdUsuario()
                                    != usuario.getId()
            ) {

                throw new AccessDeniedException(
                        "Você não possui acesso a este pagamento."
                );
            }
        }


        return ResponseEntity.ok(
                pagamento
        );
    }


    /*
     * =========================================================
     * PAGAMENTOS POR EMPRESA
     * =========================================================
     */

    @GetMapping("/empresa/{idEmpresa}")
    public ResponseEntity<List<PagamentoResponseDTO>> listarPorEmpresa(
            @PathVariable Integer idEmpresa,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);


        /*
         * USUARIO não deve conseguir consultar
         * arbitrariamente qualquer empresa.
         *
         * Por enquanto, para manter essa etapa simples,
         * restringimos esse endpoint aos perfis internos.
         */

        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            throw new AccessDeniedException(
                    "Você não possui permissão para consultar pagamentos por empresa."
            );
        }


        return ResponseEntity.ok(
                pagamentoService.listarPorEmpresa(
                        idEmpresa
                )
        );
    }


    /*
     * =========================================================
     * PAGAMENTOS POR OBRIGAÇÃO
     * =========================================================
     */

    @GetMapping("/obrigacao/{idObrigacao}")
    public ResponseEntity<List<PagamentoResponseDTO>> listarPorObrigacao(
            @PathVariable Integer idObrigacao,
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);


        List<PagamentoResponseDTO> pagamentos =
                pagamentoService.listarPorObrigacao(
                        idObrigacao
                );


        /*
         * Para USUARIO:
         *
         * se houver algum pagamento que não pertença
         * ao usuário autenticado, bloqueamos.
         */

        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            boolean possuiPagamentoDeOutroUsuario =
                    pagamentos
                            .stream()
                            .anyMatch(
                                    pagamento ->
                                            pagamento.getIdUsuario() == null
                                                    ||
                                                    pagamento.getIdUsuario()
                                                            != usuario.getId()
                            );


            if (possuiPagamentoDeOutroUsuario) {

                throw new AccessDeniedException(
                        "Você não possui acesso aos pagamentos desta obrigação."
                );
            }
        }


        return ResponseEntity.ok(
                pagamentos
        );
    }


    /*
     * =========================================================
     * USUÁRIO AUTENTICADO
     * =========================================================
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


        if (
                !(authentication.getPrincipal()
                        instanceof Usuario usuario)
        ) {

            throw new AccessDeniedException(
                    "Usuário autenticado inválido."
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