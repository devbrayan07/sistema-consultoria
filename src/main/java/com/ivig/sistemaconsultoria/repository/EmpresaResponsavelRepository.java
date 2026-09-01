package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.model.EmpresaResponsavel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpresaResponsavelRepository extends JpaRepository<EmpresaResponsavel, Integer> {

    // O sublinhado "Empresa_IdEmpresa" força a busca pelo atributo 'idEmpresa' dentro de 'Empresa'
    List<EmpresaResponsavel> findByEmpresa_IdEmpresa(Integer idEmpresa);

}