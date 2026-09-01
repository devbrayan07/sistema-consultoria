package com.ivig.sistemaconsultoria.config;

import com.ivig.sistemaconsultoria.enums.TipoUsuario; // Verifique o pacote correto da sua Enum
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        String emailAdmin = "admin@consultoria.com";

        Usuario usuario = usuarioRepository.findByEmail(emailAdmin)
                .orElseGet(Usuario::new);

        usuario.setNome("Administrador");
        usuario.setEmail(emailAdmin);
        usuario.setSenha(passwordEncoder.encode("123456"));

        // Define o tipo do usuário (ADMIN, CONTADOR, CLIENTE, etc.)
        usuario.setTipo(TipoUsuario.CONTADOR);

        usuarioRepository.save(usuario);

        System.out.println("==================================================");
        System.out.println(">>> USUÁRIO ADMIN E TIPO ATUALIZADOS COM SUCESSO!");
        System.out.println("==================================================");
    }
}