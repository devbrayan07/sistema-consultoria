package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.dto.ObrigacaoFiscalRequestDTO;
import com.ivig.sistemaconsultoria.dto.ObrigacaoFiscalResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.service.ObrigacaoFiscalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/obrigacoes")
@RequiredArgsConstructor
public class ObrigacaoFiscalController {

    private final ObrigacaoFiscalService obrigacaoFiscalService;


    /*
     * ============================================================
     * LISTAR OBRIGAÇÕES
     * ============================================================
     */

    @GetMapping
    public ResponseEntity<List<ObrigacaoFiscalResponseDTO>> listarTodas(
            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(authentication);


        /*
         * USUARIO comum:
         * visualiza apenas as próprias obrigações.
         */
        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            return ResponseEntity.ok(
                    obrigacaoFiscalService
                            .listarPorUsuario(
                                    usuario.getId()
                            )
            );
        }


        /*
         * CONTADOR e ADMINISTRADOR:
         * visualizam todas as obrigações.
         */
        return ResponseEntity.ok(
                obrigacaoFiscalService
                        .listarTodas()
        );
    }


    /*
     * ============================================================
     * CADASTRAR OBRIGAÇÃO
     *
     * Permitido somente para:
     *
     * CONTADOR
     * ADMINISTRADOR
     * ============================================================
     */

    @PostMapping
    public ResponseEntity<ObrigacaoFiscalResponseDTO> criar(
            @RequestBody
            @Valid
            ObrigacaoFiscalRequestDTO dto,

            Authentication authentication
    ) {

        System.out.println(
                ">>> ENTROU EM ObrigacaoFiscalController.criar() <<<"
        );


        Usuario usuario =
                obterUsuarioAutenticado(
                        authentication
                );


        System.out.println(
                ">>> USUÁRIO CONTROLLER: "
                        + usuario.getEmail()
        );


        System.out.println(
                ">>> TIPO CONTROLLER: "
                        + usuario.getTipo()
        );


        boolean podeCadastrar =
                usuario.getTipo()
                        == TipoUsuario.CONTADOR
                        ||
                        usuario.getTipo()
                                == TipoUsuario.ADMINISTRADOR;


        if (!podeCadastrar) {

            throw new AccessDeniedException(
                    "Você não possui permissão para cadastrar obrigações."
            );
        }


        System.out.println(
                ">>> PERMISSÃO PARA CADASTRAR: OK <<<"
        );


        System.out.println(
                ">>> DTO RECEBIDO: "
                        + dto
        );


        System.out.println(
                ">>> CHAMANDO ObrigacaoFiscalService.criarObrigacao() <<<"
        );


        ObrigacaoFiscalResponseDTO criada =
                obrigacaoFiscalService
                        .criarObrigacao(
                                dto
                        );


        System.out.println(
                ">>> SERVICE FINALIZADO COM SUCESSO <<<"
        );


        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(
                        criada
                );
    }


    /*
     * ============================================================
     * LISTAR OBRIGAÇÕES POR EMPRESA
     * ============================================================
     */

    @GetMapping("/empresa/{empresaId}")
    public ResponseEntity<List<ObrigacaoFiscalResponseDTO>>
    listarPorEmpresa(
            @PathVariable
            Integer empresaId,

            Authentication authentication
    ) {

        Usuario usuario =
                obterUsuarioAutenticado(
                        authentication
                );


        /*
         * USUARIO comum:
         *
         * só pode consultar obrigações da
         * própria empresa.
         */
        if (
                usuario.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            boolean pertenceAoUsuario =
                    obrigacaoFiscalService
                            .empresaPertenceAoUsuario(
                                    empresaId,
                                    usuario.getId()
                            );


            if (!pertenceAoUsuario) {

                throw new AccessDeniedException(
                        "Você não possui acesso às obrigações desta empresa."
                );
            }
        }


        return ResponseEntity.ok(
                obrigacaoFiscalService
                        .listarPorEmpresa(
                                empresaId
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


    @PutMapping("/{id}")
    public ResponseEntity<ObrigacaoFiscalResponseDTO> atualizar(@PathVariable Integer id, @RequestBody @Valid ObrigacaoFiscalRequestDTO dto, Authentication authentication) {
        Usuario usuario = obterUsuarioAutenticado(authentication);

        boolean podeEditar = usuario.getTipo() == TipoUsuario.CONTADOR || usuario.getTipo() == TipoUsuario.ADMINISTRADOR;


        if (!podeEditar) {
            throw new AccessDeniedException("Você não possui permissão para editar obrigações.");
        }

        ObrigacaoFiscalResponseDTO atualizada = obrigacaoFiscalService.atualizarObrigacao(id, dto);

        return ResponseEntity.ok(atualizada);

    }
}