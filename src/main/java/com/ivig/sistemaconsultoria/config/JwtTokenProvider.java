package com.ivig.sistemaconsultoria.config;

import com.ivig.sistemaconsultoria.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Chave secreta configurada no application.properties ou valor padrão com +256 bits
    @Value("${app.jwt.secret:umaChaveSuperSecretaECompridaComMaisDe32Caracteres123456}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:86400000}") // 24 horas padrão
    private long jwtExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date validade = new Date(agora.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("tipo", usuario.getTipo().name())
                .issuedAt(agora)
                .expiration(validade)
                .signWith(getSigningKey())
                .compact();
    }

    public String getEmailDoToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}