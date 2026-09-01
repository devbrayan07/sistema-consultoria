package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.enums.StatusPagamento;
import com.ivig.sistemaconsultoria.model.Pagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoRepository
        extends JpaRepository<Pagamento, Integer> {

    List<Pagamento> findByUsuario_IdOrderByDataCriacaoDesc(
            Integer idUsuario
    );

    List<Pagamento> findByEmpresa_IdEmpresaOrderByDataCriacaoDesc(
            Integer idEmpresa
    );

    List<Pagamento> findByObrigacao_IdOrderByDataCriacaoDesc(
            Integer idObrigacao
    );

    Optional<Pagamento> findFirstByObrigacao_IdOrderByDataCriacaoDesc(
            Integer idObrigacao
    );

    boolean existsByObrigacao_IdAndStatusIn(
            Integer idObrigacao,
            Collection<StatusPagamento> status
    );

    List<Pagamento> findByStatusOrderByDataCriacaoDesc(
            StatusPagamento status
    );
}