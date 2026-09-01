package com.ivig.sistemaconsultoria.model;

import com.ivig.sistemaconsultoria.enums.RegimeTributario;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "empresa")
@Data // <-- Importante: Gera os Getters e Setters automaticamente
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Integer idEmpresa; // Se estivesse como "id", o Lombok geraria getId() e não getIdEmpresa()

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(name = "regime_tributario")
    private RegimeTributario regimeTributario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_cliente")
    private Usuario cliente; // Se estivesse como "cliente" ou "usuario", o método seria setCliente(...) ou setUsuario(...)

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }
}