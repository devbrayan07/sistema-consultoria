package com.ivig.sistemaconsultoria.controller;


import com.ivig.sistemaconsultoria.dto.PagamentoResponseDTO;
import com.ivig.sistemaconsultoria.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev/pagamentos")
@RequiredArgsConstructor
@Profile("dev")
public class PagamentoDevController {

    private final PagamentoService  pagamentoService;

    @PostMapping("/{idPagamento}/aprovar")
    public ResponseEntity<PagamentoResponseDTO> aprovar (
            @PathVariable Integer idPagamento
    ) {
        PagamentoResponseDTO pagamento = pagamentoService.simularAprovacao(idPagamento);

        return ResponseEntity.ok(pagamento);
    }
}
