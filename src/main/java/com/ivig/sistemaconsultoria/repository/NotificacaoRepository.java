package com.ivig.sistemaconsultoria.repository;

import com.ivig.sistemaconsultoria.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {

    List<Notificacao> findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(Integer usuarioId);

    List<Notificacao> findByUsuarioIdOrderByCriadoEmDesc(Integer usuarioId);



}
