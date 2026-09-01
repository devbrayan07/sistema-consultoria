package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.model.RelatorioFinanceiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioFinanceiroRepository extends JpaRepository<RelatorioFinanceiro, Integer> {

    // O sublinhado "Empresa_IdEmpresa" resolve a ambiguidade do Spring JPA
    List<RelatorioFinanceiro> findByEmpresa_IdEmpresa(Integer idEmpresa);

}