package com.ivig.sistemaconsultoria.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "token_redefinicao_senha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRedefinicaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_usuario",
            nullable = false
    )
    private Usuario usuario;

    @Column(
            name = "token_hash",
            nullable = false,
            unique = true,
            length = 64
    )
    private String tokenHash;

    @Column(
            name = "data_expiracao",
            nullable = false
    )
    private LocalDateTime dataExpiracao;

    @Column(
            name = "utilizado",
            nullable = false
    )
    @Builder.Default
    private Boolean utilizado = false;

    @Column(
            name = "data_criacao",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime dataCriacao =
            LocalDateTime.now();
}