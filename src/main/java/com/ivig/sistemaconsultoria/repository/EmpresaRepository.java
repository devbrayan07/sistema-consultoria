package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    boolean existsByCnpj(String cnpj);

    Optional<Empresa> findByCnpj(String cnpj);

    List<Empresa> findByCliente_Id(Integer idUsuario);

    boolean existsByCliente_Id(Integer idUsuario);
}