package com.ivig.sistemaconsultoria.model;


import com.ivig.sistemaconsultoria.enums.TipoReferenciaNotificacao;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacao")
    private int id;

    @Column(length = 300)
    private String mensagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    private Boolean lida = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "referencia_tipo")
    private TipoReferenciaNotificacao referenciaTipo;

    @Column(name = "id_referencia")
    private int idReferencia;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;
}