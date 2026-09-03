package com.ivig.sistemaconsultoria.config;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    /*
     * ============================================================
     * SECURITY FILTER CHAIN
     * ============================================================
     */

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http
    ) throws Exception {

        http

                /*
                 * =================================================
                 * CORS
                 * =================================================
                 */

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource()
                        )
                )


                /*
                 * =================================================
                 * CSRF
                 *
                 * API REST utilizando JWT.
                 * Não utilizamos sessão para autenticação.
                 * =================================================
                 */

                .csrf(
                        AbstractHttpConfigurer::disable
                )


                /*
                 * =================================================
                 * SESSÃO
                 *
                 * JWT -> aplicação stateless.
                 * =================================================
                 */

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                /*
                 * =================================================
                 * AUTORIZAÇÃO
                 * =================================================
                 */

                .authorizeHttpRequests(auth -> auth


                        /*
                         * =================================================
                         * DISPATCH DE ERRO
                         *
                         * Impede que erros internos legítimos
                         * sejam mascarados pelo Spring Security
                         * como 403 Forbidden.
                         * =================================================
                         */

                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        )
                        .permitAll()


                        /*
                         * =================================================
                         * PREFLIGHT / CORS
                         * =================================================
                         */

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()


                        /*
                         * =================================================
                         * ROTA PADRÃO DE ERRO DO SPRING
                         * =================================================
                         */

                        .requestMatchers(
                                "/error"
                        )
                        .permitAll()


                        /*
                         * =================================================
                         * FRONTEND PÚBLICO
                         *
                         * As páginas são públicas no nível HTTP.
                         * A proteção funcional das páginas internas
                         * continua sendo realizada pelo auth.js/JWT.
                         * =================================================
                         */

                        .requestMatchers(
                                "/",

                                "/dashboard",
                                "/empresas",
                                "/obrigacoes",
                                "/documentos",
                                "/usuarios",
                                "/pagamentos",
                                "/esqueci-senha",
                                "/redefinir-senha",
                                "/cadastro",

                                "/index.html",
                                "/dashboard.html",
                                "/empresas.html",
                                "/obrigacoes.html",
                                "/documentos.html",
                                "/usuarios.html",
                                "/pagamentos.html",
                                "/esqueci-senha.html",
                                "/redefinir-senha.html",
                                "/cadastro.html",

                                "/style.css",
                                "/*.js",
                                "/favicon.ico"
                        )
                        .permitAll()

                        /*
                         * =================================================
                         * SWAGGER / OPENAPI
                         * =================================================
                         */

                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        )
                        .permitAll()


                        /*
                         * =================================================
                         * AUTENTICAÇÃO
                         *
                         * Login e cadastro público.
                         * =================================================
                         */

                        .requestMatchers(
                                "/api/auth/**"
                        )
                        .permitAll()


                        /*
                         * =================================================
                         * ADMINISTRAÇÃO
                         *
                         * Exclusivo do ADMINISTRADOR.
                         * =================================================
                         */

                        .requestMatchers(
                                "/api/admin/**"
                        )
                        .hasRole(
                                "ADMINISTRADOR"
                        )


                        /*
                         * =================================================
                         * USUÁRIOS
                         *
                         * CONTADOR:
                         * pode criar usuários comuns conforme
                         * as validações do Controller/Service.
                         *
                         * ADMINISTRADOR:
                         * gerenciamento administrativo.
                         * =================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/usuarios",
                                "/api/usuarios/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/usuarios",
                                "/api/usuarios/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/usuarios",
                                "/api/usuarios/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/usuarios",
                                "/api/usuarios/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        /*
                         * =================================================
                         * EMPRESAS
                         *
                         * USUARIO:
                         * pode cadastrar/administrar a própria empresa
                         * conforme as regras de negócio.
                         *
                         * CONTADOR / ADMINISTRADOR:
                         * podem administrar empresas dos clientes.
                         * =================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/empresas",
                                "/api/empresas/**"
                        )
                        .hasAnyRole(
                                "USUARIO",
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/empresas",
                                "/api/empresas/**"
                        )
                        .hasAnyRole(
                                "USUARIO",
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/empresas",
                                "/api/empresas/**"
                        )
                        .hasAnyRole(
                                "USUARIO",
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/empresas",
                                "/api/empresas/**"
                        )
                        .hasAnyRole(
                                "USUARIO",
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        /*
                         * =================================================
                         * OBRIGAÇÕES FISCAIS
                         *
                         * CONTADOR e ADMINISTRADOR:
                         * criação, alteração e exclusão.
                         *
                         * USUARIO:
                         * somente consulta das próprias obrigações.
                         * =================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/obrigacoes",
                                "/api/obrigacoes/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/obrigacoes",
                                "/api/obrigacoes/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/obrigacoes",
                                "/api/obrigacoes/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/obrigacoes",
                                "/api/obrigacoes/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        /*
                         * =================================================
                         * DOCUMENTOS
                         *
                         * CONTADOR / ADMINISTRADOR:
                         * upload e alterações.
                         *
                         * USUARIO:
                         * somente leitura dos próprios documentos.
                         * =================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/documentos",
                                "/api/documentos/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/documentos",
                                "/api/documentos/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/documentos",
                                "/api/documentos/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/documentos",
                                "/api/documentos/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        /*
                         * =================================================
                         * RELATÓRIOS FINANCEIROS
                         * =================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/relatorios-financeiros",
                                "/api/relatorios-financeiros/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/relatorios-financeiros",
                                "/api/relatorios-financeiros/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/relatorios-financeiros",
                                "/api/relatorios-financeiros/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/relatorios-financeiros",
                                "/api/relatorios-financeiros/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        /*
                         * =================================================
                         * CONSULTAS GET
                         *
                         * Todos os perfis autenticados podem entrar
                         * nas rotas GET.
                         *
                         * Restrições de propriedade continuam sendo
                         * responsabilidade dos Controllers/Services.
                         * =================================================
                         */

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/dev/pagamentos/**"
                        )
                        .hasAnyRole(
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/webhooks/mercadopago"
                        )
                        .permitAll()

                        .requestMatchers(
                                "/api/**"
                        )
                        .authenticated()

                        .requestMatchers(
                                "/api/auth/**"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pagamentos",
                                "/api/pagamentos/**"
                        )
                        .hasAnyRole(
                                "USUARIO",
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/pagamentos",
                                "/api/pagamentos/**"
                        )
                        .hasAnyRole(
                                "USUARIO",
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/**"
                        )
                        .hasAnyRole(
                                "USUARIO",
                                "CONTADOR",
                                "ADMINISTRADOR"
                        )


                        /*
                         * =================================================
                         * RESTANTE DA API
                         * =================================================
                         */

                        .requestMatchers(
                                "/api/**"
                        )
                        .authenticated()


                        /*
                         * =================================================
                         * RESTANTE DAS ROTAS
                         * =================================================
                         */

                        .anyRequest()
                        .authenticated()
                )


                /*
                 * =================================================
                 * FILTRO JWT
                 * =================================================
                 */

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    /*
     * ============================================================
     * CORS
     * ============================================================
     */

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        /*
         * Desenvolvimento.
         *
         * Em produção devemos restringir as origens
         * aos domínios oficiais do sistema.
         */

        configuration.setAllowedOriginPatterns(
                List.of("*")
        );


        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "PATCH",
                        "OPTIONS"
                )
        );


        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With"
                )
        );


        configuration.setExposedHeaders(
                List.of(
                        "Authorization"
                )
        );


        configuration.setAllowCredentials(
                true
        );


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
                "/**",
                configuration
        );


        return source;
    }


    /*
     * ============================================================
     * PASSWORD ENCODER
     * ============================================================
     */

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }


    /*
     * ============================================================
     * AUTHENTICATION MANAGER
     * ============================================================
     */

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {

        return configuration
                .getAuthenticationManager();
    }
}