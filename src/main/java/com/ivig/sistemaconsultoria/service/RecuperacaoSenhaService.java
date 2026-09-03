package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.model.TokenRedefinicaoSenha;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.TokenRedefinicaoSenhaRepository;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private final UsuarioRepository usuarioRepository;

    private final TokenRedefinicaoSenhaRepository
            tokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;


    @Value("${app.frontend.url}")
    private String frontendUrl;


    @Transactional
    public void solicitarRecuperacao(
            String email
    ) {

        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElse(null);


        /*
         * Não informamos se o e-mail existe ou não.
         * Isso evita enumeração de usuários.
         */
        if (usuario == null) {
            return;
        }


        String token =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        +
                        UUID.randomUUID()
                                .toString()
                                .replace("-", "");


        String tokenHash =
                gerarHash(token);


        TokenRedefinicaoSenha registro =
                TokenRedefinicaoSenha
                        .builder()
                        .usuario(usuario)
                        .tokenHash(tokenHash)
                        .dataExpiracao(
                                LocalDateTime
                                        .now()
                                        .plusMinutes(30)
                        )
                        .utilizado(false)
                        .build();


        tokenRepository.save(
                registro
        );


        String link =
                frontendUrl
                        + "/redefinir-senha.html?token="
                        + token;


        emailService.enviarRecuperacaoSenha(
                usuario.getEmail(),
                usuario.getNome(),
                link
        );
    }


    @Transactional
    public void redefinirSenha(
            String token,
            String novaSenha
    ) {

        String tokenHash =
                gerarHash(token);


        TokenRedefinicaoSenha registro =
                tokenRepository
                        .findByTokenHashAndUtilizadoFalse(
                                tokenHash
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Token inválido ou já utilizado."
                                        )
                        );


        if (
                registro
                        .getDataExpiracao()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {

            throw new IllegalArgumentException(
                    "O link de recuperação expirou."
            );
        }


        Usuario usuario =
                registro.getUsuario();


        usuario.setSenha(
                passwordEncoder.encode(
                        novaSenha
                )
        );


        usuarioRepository.save(
                usuario
        );


        registro.setUtilizado(
                true
        );


        tokenRepository.save(
                registro
        );
    }


    private String gerarHash(
            String valor
    ) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );


            byte[] hash =
                    digest.digest(
                            valor.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );


            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (Exception erro) {

            throw new IllegalStateException(
                    "Não foi possível gerar o token de segurança.",
                    erro
            );
        }
    }
}