package com.ivig.sistemaconsultoria.config;

import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = obterTokenDaRequisicao(request);

        if (StringUtils.hasText(token) && tokenProvider.validarToken(token)) {
            String email = tokenProvider.getEmailDoToken(token);

            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            if (
                    usuario != null
                            &&
                            Boolean.TRUE.equals(
                                    usuario.getAtivo()
                            )
            ) {

                String role =
                        "ROLE_"
                                + usuario.getTipo()
                                .name()
                                .toUpperCase();


                System.out.println(
                        "========================================"
                );

                System.out.println(
                        "REQUISIÇÃO: "
                                + request.getMethod()
                                + " "
                                + request.getRequestURI()
                );

                System.out.println(
                        "USUÁRIO JWT: "
                                + usuario.getEmail()
                );

                System.out.println(
                        "TIPO BANCO: "
                                + usuario.getTipo()
                );

                System.out.println(
                        "AUTHORITY: "
                                + role
                );

                System.out.println(
                        "========================================"
                );


                var authority =
                        new SimpleGrantedAuthority(
                                role
                        );


                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                List.of(
                                        authority
                                )
                        );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );
            }
        }

        filterChain.doFilter(request, response);
    }

    private String obterTokenDaRequisicao(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}