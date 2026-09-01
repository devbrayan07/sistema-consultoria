package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.dto.CadastroRequestDTO;
import com.ivig.sistemaconsultoria.dto.UsuarioRequestDTO;
import com.ivig.sistemaconsultoria.dto.UsuarioResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.exceptions.BusinessException;
import com.ivig.sistemaconsultoria.exceptions.ResourceNotFoundException;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;


    /*
     * ============================================================
     * CRIAR USUÁRIO
     * ============================================================
     */

    @Transactional
    public UsuarioResponseDTO criarUsuario(
            UsuarioRequestDTO dto,
            Usuario usuarioAutenticado
    ) {

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(
                    "E-mail já cadastrado no sistema."
            );
        }


        /*
         * CONTADOR:
         * pode cadastrar somente clientes USUARIO.
         */
        if (usuarioAutenticado.getTipo() == TipoUsuario.CONTADOR) {

            if (dto.getTipo() != TipoUsuario.USUARIO) {
                throw new BusinessException(
                        "O contador pode cadastrar somente usuários do tipo USUARIO."
                );
            }
        }


        /*
         * USUARIO comum não pode cadastrar contas.
         */
        if (usuarioAutenticado.getTipo() == TipoUsuario.USUARIO) {

            throw new BusinessException(
                    "Você não possui permissão para cadastrar usuários."
            );
        }


        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(
                        passwordEncoder.encode(
                                dto.getSenha()
                        )
                )
                .tipo(dto.getTipo())
                .ativo(true)
                .build();


        Usuario salvo =
                usuarioRepository.save(usuario);


        return converterParaDTO(salvo);
    }


    /*
     * ============================================================
     * BUSCAR POR ID
     * ============================================================
     */

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(
            Integer id
    ) {

        Usuario usuario =
                buscarEntidadePorId(id);

        return converterParaDTO(usuario);
    }


    /*
     * ============================================================
     * BUSCAR ENTIDADE
     * ============================================================
     */

    @Transactional(readOnly = true)
    public Usuario buscarEntidadePorId(
            Integer id
    ) {

        return usuarioRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Usuário não encontrado com ID: " + id
                        )
                );
    }


    /*
     * ============================================================
     * LISTAR TODOS
     * SOMENTE ADMINISTRADOR
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodos() {

        return usuarioRepository
                .findAll()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * LISTAR CLIENTES
     * CONTADOR / ADMINISTRADOR
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarClientes() {

        return usuarioRepository
                .findByTipo(
                        TipoUsuario.USUARIO
                )
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * DTO
     * ============================================================
     */

    private UsuarioResponseDTO converterParaDTO(
            Usuario usuario
    ) {

        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .tipo(usuario.getTipo())
                .ativo(usuario.getAtivo())
                .criadoEm(usuario.getCriadoEm())
                .build();
    }


    @Transactional
    public UsuarioResponseDTO cadastrarPublico(CadastroRequestDTO dto) {

        String emailNormalizado =
                dto.getEmail()
                        .trim()
                        .toLowerCase();

        if (usuarioRepository.existsByEmail(emailNormalizado)) {
            throw new BusinessException(
                    "Já existe uma conta cadastrada com este e-mail."
            );
        }

        Usuario usuario = Usuario.builder()
                .nome(dto.getNome().trim())
                .email(emailNormalizado)
                .senha(
                        passwordEncoder.encode(
                                dto.getSenha()
                        )
                )
                .tipo(TipoUsuario.USUARIO)
                .ativo(true)
                .build();

        Usuario salvo =
                usuarioRepository.save(usuario);

        return converterParaDTO(salvo);
    }
}