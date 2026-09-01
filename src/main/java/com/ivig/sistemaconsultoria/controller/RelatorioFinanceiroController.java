package com.ivig.sistemaconsultoria.controller;

import com.ivig.sistemaconsultoria.dto.RelatorioFinanceiroRequestDTO;
import com.ivig.sistemaconsultoria.dto.RelatorioFinanceiroResponseDTO;
import com.ivig.sistemaconsultoria.service.RelatorioFinanceiroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios-financeiros")
@RequiredArgsConstructor
public class RelatorioFinanceiroController {

    private final RelatorioFinanceiroService relatorioService;

    @PostMapping
    public ResponseEntity<RelatorioFinanceiroResponseDTO> criar(@RequestBody @Valid RelatorioFinanceiroRequestDTO dto) {
        RelatorioFinanceiroResponseDTO response = relatorioService.criarRelatorio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/empresa/{idEmpresa}")
    public ResponseEntity<List<RelatorioFinanceiroResponseDTO>> listarPorEmpresa(@PathVariable Integer idEmpresa) {
        List<RelatorioFinanceiroResponseDTO> lista = relatorioService.listarPorEmpresa(idEmpresa);
        return ResponseEntity.ok(lista);
    }
}