package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.enums.StatusObrigacao;
import com.ivig.sistemaconsultoria.model.ObrigacaoFiscal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ObrigacaoFiscalRepository extends JpaRepository<ObrigacaoFiscal, Integer> {

    // O sublinhado "Empresa_IdEmpresa" avisa ao Spring: procure no campo "empresa", o atributo "idEmpresa"
    List<ObrigacaoFiscal> findByEmpresa_IdEmpresa(Integer idEmpresa);

    List<ObrigacaoFiscal> findByStatus(StatusObrigacao status);

    List<ObrigacaoFiscal> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);

    List<ObrigacaoFiscal> findByEmpresa_Cliente_Id(Integer idUsuario);
}