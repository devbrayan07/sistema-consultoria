package com.ivig.sistemaconsultoria.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "empresa_responsavel")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EmpresaResponsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa_resp")
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;
}