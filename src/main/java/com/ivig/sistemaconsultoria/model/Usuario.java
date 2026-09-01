package com.ivig.sistemaconsultoria.model;

import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false, length = 255)
    private String senha;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            columnDefinition = "ENUM('USUARIO', 'CONTADOR', 'ADMINISTRADOR')"
    )
    private TipoUsuario tipo;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(
            name = "criado_em",
            insertable = false,
            updatable = false
    )
    private LocalDateTime criadoEm;
}