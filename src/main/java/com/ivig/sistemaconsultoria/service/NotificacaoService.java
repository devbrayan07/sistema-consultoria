package com.ivig.sistemaconsultoria.service;


import com.ivig.sistemaconsultoria.enums.TipoReferenciaNotificacao;
import com.ivig.sistemaconsultoria.model.Notificacao;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.NotificacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    @Transactional
    public void criarNotificacao(Usuario usuario, String mensagem, TipoReferenciaNotificacao refTipo, Integer refId) {
        Notificacao notificacao = Notificacao.builder()
                .usuario(usuario)
                .mensagem(mensagem)
                .referenciaTipo(refTipo)
                .idReferencia(refId)
                .lida(false)
                .build();

        notificacaoRepository.save(notificacao);
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarNaoLidasPorUsuario(Integer usuarioId) {
        return notificacaoRepository.findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc(usuarioId);
    }

    @Transactional
    public void marcarComoLida(Integer notificacaoId) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada"));
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }
}