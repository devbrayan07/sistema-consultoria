package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.dto.ObrigacaoFiscalRequestDTO;
import com.ivig.sistemaconsultoria.dto.ObrigacaoFiscalResponseDTO;
import com.ivig.sistemaconsultoria.enums.StatusObrigacao;
import com.ivig.sistemaconsultoria.enums.TipoReferenciaNotificacao;
import com.ivig.sistemaconsultoria.exceptions.ResourceNotFoundException;
import com.ivig.sistemaconsultoria.model.Empresa;
import com.ivig.sistemaconsultoria.model.ObrigacaoFiscal;
import com.ivig.sistemaconsultoria.repository.ObrigacaoFiscalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObrigacaoFiscalService {

    private final ObrigacaoFiscalRepository obrigacaoRepository;
    private final EmpresaService empresaService;
    private final NotificacaoService notificacaoService;


    /*
     * ============================================================
     * LISTAR TODAS
     * CONTADOR / ADMINISTRADOR
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<ObrigacaoFiscalResponseDTO> listarTodas() {

        return obrigacaoRepository.findAll()
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * LISTAR OBRIGAÇÕES DE UM USUÁRIO
     * ============================================================
     *
     * Utilizado quando o usuário comum está autenticado.
     *
     * Retorna somente obrigações pertencentes às empresas
     * vinculadas ao usuário.
     */

    @Transactional(readOnly = true)
    public List<ObrigacaoFiscalResponseDTO> listarPorUsuario(
            Integer idUsuario
    ) {

        return obrigacaoRepository
                .findByEmpresa_Cliente_Id(idUsuario)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * CRIAR OBRIGAÇÃO
     * CONTADOR / ADMINISTRADOR
     * ============================================================
     */

    @Transactional
    public ObrigacaoFiscalResponseDTO criarObrigacao(
            ObrigacaoFiscalRequestDTO dto
    ) {

        Empresa empresa =
                empresaService.buscarEntidadePorId(dto.getIdEmpresa());

        ObrigacaoFiscal obrigacao =
                ObrigacaoFiscal.builder()
                        .tipo(dto.getTipo())
                        .competencia(dto.getCompetencia())
                        .dataVencimento(dto.getDataVencimento())
                        .empresa(empresa)
                        .valor(dto.getValor())
                        .honorario(dto.getHonorario())
                        .status(
                                dto.getStatus() != null
                                        ? dto.getStatus()
                                        : StatusObrigacao.PENDENTE
                        )
                        .build();

        ObrigacaoFiscal salva =
                obrigacaoRepository.save(obrigacao);


        /*
         * --------------------------------------------------------
         * NOTIFICAÇÃO AUTOMÁTICA
         * --------------------------------------------------------
         */

        if (empresa.getCliente() != null) {

            String msg = String.format(
                    "Nova obrigação %s lançada para %s. Vencimento: %s",
                    salva.getTipo(),
                    empresa.getRazaoSocial(),
                    salva.getDataVencimento()
            );

            notificacaoService.criarNotificacao(
                    empresa.getCliente(),
                    msg,
                    TipoReferenciaNotificacao.OBRIGACAO,
                    salva.getId()
            );
        }

        return converterParaDTO(salva);
    }


    /*
     * ============================================================
     * LISTAR POR EMPRESA
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<ObrigacaoFiscalResponseDTO> listarPorEmpresa(
            Integer idEmpresa
    ) {

        return obrigacaoRepository
                .findByEmpresa_IdEmpresa(idEmpresa)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }


    /*
     * ============================================================
     * VERIFICAR SE EMPRESA PERTENCE AO USUÁRIO
     * ============================================================
     *
     * Essa verificação impede que um usuário altere manualmente
     * o ID da empresa na URL e visualize dados de outro cliente.
     */

    @Transactional(readOnly = true)
    public boolean empresaPertenceAoUsuario(
            Integer idEmpresa,
            Integer idUsuario
    ) {

        Empresa empresa =
                empresaService.buscarEntidadePorId(idEmpresa);

        if (empresa.getCliente() == null) {
            return false;
        }

        return empresa.getCliente().getId() == idUsuario;
    }


    /*
     * ============================================================
     * BUSCAR OBRIGAÇÃO POR ID
     * ============================================================
     */

    @Transactional(readOnly = true)
    public ObrigacaoFiscalResponseDTO buscarPorId(
            Integer id
    ) {

        ObrigacaoFiscal obrigacao =
                obrigacaoRepository.findById(id)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "Obrigação fiscal não encontrada com ID: "
                                                + id
                                )
                        );

        return converterParaDTO(obrigacao);
    }


    /*
     * ============================================================
     * CONVERTER ENTIDADE PARA DTO
     * ============================================================
     */

    private ObrigacaoFiscalResponseDTO converterParaDTO(
            ObrigacaoFiscal obrigacao
    ) {

        return ObrigacaoFiscalResponseDTO.builder()
                .id(obrigacao.getId())
                .tipo(obrigacao.getTipo())
                .competencia(obrigacao.getCompetencia())
                .dataVencimento(obrigacao.getDataVencimento())
                .valor(obrigacao.getValor())
                .honorario(obrigacao.getHonorario())
                .status(obrigacao.getStatus())

                .idEmpresa(
                        obrigacao.getEmpresa() != null
                                ? obrigacao.getEmpresa().getIdEmpresa()
                                : null
                )

                .razaoSocialEmpresa(
                        obrigacao.getEmpresa() != null
                                ? obrigacao.getEmpresa().getRazaoSocial()
                                : null
                )

                .criadoEm(obrigacao.getCriadoEm())
                .build();
    }


    @Transactional
    public ObrigacaoFiscalResponseDTO atualizarObrigacao(
            Integer id,
            ObrigacaoFiscalRequestDTO dto
    ) {

        ObrigacaoFiscal obrigacao =
                obrigacaoRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Obrigação fiscal não encontrada com ID: " + id
                                )
                        );


        Empresa empresa =
                empresaService.buscarEntidadePorId(
                        dto.getIdEmpresa()
                );


        obrigacao.setTipo(
                dto.getTipo()
        );

        obrigacao.setCompetencia(
                dto.getCompetencia()
        );

        obrigacao.setDataVencimento(
                dto.getDataVencimento()
        );

        obrigacao.setValor(
                dto.getValor()
        );

        obrigacao.setHonorario(
                dto.getHonorario()
        );

        obrigacao.setEmpresa(
                empresa
        );


        if (dto.getStatus() != null) {
            obrigacao.setStatus(
                    dto.getStatus()
            );
        }


        ObrigacaoFiscal atualizada =
                obrigacaoRepository.save(
                        obrigacao
                );


        return converterParaDTO(
                atualizada
        );
    }
}