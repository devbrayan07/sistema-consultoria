package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.dto.RelatorioFinanceiroRequestDTO;
import com.ivig.sistemaconsultoria.dto.RelatorioFinanceiroResponseDTO;
import com.ivig.sistemaconsultoria.model.Empresa;
import com.ivig.sistemaconsultoria.model.RelatorioFinanceiro;
import com.ivig.sistemaconsultoria.repository.RelatorioFinanceiroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioFinanceiroService {

    private final RelatorioFinanceiroRepository relatorioRepository;
    private final EmpresaService empresaService;

    @Transactional
    public RelatorioFinanceiroResponseDTO criarRelatorio(RelatorioFinanceiroRequestDTO dto) {
        Empresa empresa = empresaService.buscarEntidadePorId(dto.getIdEmpresa());

        RelatorioFinanceiro relatorio = RelatorioFinanceiro.builder()
                .empresa(empresa)
                .periodo(dto.getPeriodo())
                .receita(dto.getReceita())
                .despesa(dto.getDespesa())
                .impostosPagos(dto.getImpostosPagos())
                .build();

        RelatorioFinanceiro salvo = relatorioRepository.save(relatorio);
        return converterParaDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<RelatorioFinanceiroResponseDTO> listarPorEmpresa(Integer idEmpresa) {
        return relatorioRepository.findByEmpresa_IdEmpresa(idEmpresa).stream()
                .map(this::converterParaDTO)
                .toList();
    }

    private RelatorioFinanceiroResponseDTO converterParaDTO(RelatorioFinanceiro relatorio) {
        return RelatorioFinanceiroResponseDTO.builder()
                .id(relatorio.getId())
                .idEmpresa(relatorio.getEmpresa() != null ? relatorio.getEmpresa().getIdEmpresa() : null)
                .razaoSocialEmpresa(relatorio.getEmpresa() != null ? relatorio.getEmpresa().getRazaoSocial() : null)
                .periodo(relatorio.getPeriodo())
                .receita(relatorio.getReceita())
                .despesa(relatorio.getDespesa())
                .impostosPagos(relatorio.getImpostosPagos())
                .geradoEm(relatorio.getGeradoEm())
                .nomeGeradoPor(relatorio.getGeradoPor() != null ? relatorio.getGeradoPor().getNome() : null)
                .build();
    }
}