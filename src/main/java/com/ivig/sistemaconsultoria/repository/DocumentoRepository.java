package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.model.Documento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, Integer> {

    List<Documento> findByEmpresaIdEmpresa(Integer idEmpresa);

    List<Documento> findByEmpresaIdEmpresaAndCompetencia(Integer idEmpresa, String competencia);

    List<Documento> findByEmpresa_Cliente_Id(Integer idUsuario);
}