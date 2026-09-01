package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.model.Notificacao;
import com.ivig.sistemaconsultoria.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService  notificacaoService;

    @GetMapping("/usuario/{usuarioId}/nao-lidas")
    public ResponseEntity<List<Notificacao>> listarPorUsuario(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(notificacaoService.listarNaoLidasPorUsuario(usuarioId));
    }


    @PatchMapping("/{id}/ler")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Integer id) {
        notificacaoService.marcarComoLida(id);
        return ResponseEntity.noContent().build();
    }
}
