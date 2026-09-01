package com.ivig.sistemaconsultoria.model;

import com.ivig.sistemaconsultoria.enums.TipoDocumento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "documento")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Documento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_documento")
    private int id;

    @Enumerated(EnumType.STRING)
    private TipoDocumento tipo;

    @Column(name = "nome_arquivo")
    private String nomeArquivo;

    @Column(name = "url_arquivo")
    private String urlArquivo;

    private LocalDate competencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enviado_por")
    private Usuario enviadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private LocalDateTime criadoEm;
}