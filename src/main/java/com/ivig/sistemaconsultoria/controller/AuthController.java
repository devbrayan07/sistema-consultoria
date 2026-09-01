package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.config.JwtTokenProvider;
import com.ivig.sistemaconsultoria.dto.CadastroRequestDTO;
import com.ivig.sistemaconsultoria.dto.LoginRequestDTO;
import com.ivig.sistemaconsultoria.dto.LoginResponseDTO;
import com.ivig.sistemaconsultoria.dto.UsuarioResponseDTO;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;
import com.ivig.sistemaconsultoria.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid LoginRequestDTO dto
    ) {

        Usuario usuario = usuarioRepository
                .findByEmail(dto.getEmail())
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "mensagem",
                                    "E-mail ou senha inválidos."
                            )
                    );
        }

        if (!passwordEncoder.matches(
                dto.getSenha(),
                usuario.getSenha()
        )) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "mensagem",
                                    "E-mail ou senha inválidos."
                            )
                    );
        }

        String token =
                jwtTokenProvider.gerarToken(usuario);

        LoginResponseDTO resposta =
                LoginResponseDTO.builder()
                        .id(usuario.getId())
                        .nome(usuario.getNome())
                        .token(token)
                        .email(usuario.getEmail())
                        .tipo(usuario.getTipo().name())
                        .build();

        return ResponseEntity.ok(resposta);
    }


    @PostMapping("/cadastro")
    public ResponseEntity<UsuarioResponseDTO> cadastrar(
            @RequestBody @Valid CadastroRequestDTO dto
    ) {

        UsuarioResponseDTO usuario =
                usuarioService.cadastrarPublico(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }
}