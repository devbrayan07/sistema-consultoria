package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.dto.EmpresaRequestDTO;
import com.ivig.sistemaconsultoria.dto.EmpresaResponseDTO;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.exceptions.BusinessException;
import com.ivig.sistemaconsultoria.exceptions.ResourceNotFoundException;
import com.ivig.sistemaconsultoria.model.Empresa;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.EmpresaRepository;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final UsuarioRepository usuarioRepository;


    /*
     * ============================================================
     * CRIAR EMPRESA
     * ============================================================
     */

    @Transactional
    public EmpresaResponseDTO criar(
            EmpresaRequestDTO dto,
            Usuario usuarioAutenticado
    ) {

        if (usuarioAutenticado == null) {

            throw new BusinessException(
                    "Usuário autenticado não identificado."
            );
        }


        if (
                !Boolean.TRUE.equals(
                        usuarioAutenticado.getAtivo()
                )
        ) {

            throw new BusinessException(
                    "Usuário autenticado está inativo."
            );
        }


        String cnpj =
                dto.getCnpj() != null
                        ? dto.getCnpj().replaceAll("\\D", "")
                        : null;


        if (
                cnpj == null
                        ||
                        cnpj.length() != 14
        ) {

            throw new BusinessException(
                    "O CNPJ deve possuir 14 dígitos."
            );
        }


        if (
                empresaRepository.existsByCnpj(
                        cnpj
                )
        ) {

            throw new BusinessException(
                    "Já existe uma empresa cadastrada com este CNPJ."
            );
        }


        Usuario cliente;


        /*
         * ========================================================
         * USUÁRIO COMUM
         *
         * Pode possuir apenas uma empresa.
         * A empresa é vinculada automaticamente ao próprio usuário.
         * ========================================================
         */

        if (
                usuarioAutenticado.getTipo()
                        == TipoUsuario.USUARIO
        ) {

            boolean jaPossuiEmpresa =
                    empresaRepository
                            .existsByCliente_Id(
                                    usuarioAutenticado.getId()
                            );


            if (jaPossuiEmpresa) {

                throw new BusinessException(
                        "Você já possui uma empresa cadastrada."
                );
            }


            cliente =
                    usuarioAutenticado;

        } else {


            /*
             * ====================================================
             * CONTADOR / ADMINISTRADOR
             *
             * Precisam escolher explicitamente um cliente.
             * ====================================================
             */

            if (
                    dto.getIdUsuarioCliente()
                            == null
            ) {

                throw new BusinessException(
                        "Selecione o cliente responsável pela empresa."
                );
            }


            cliente =
                    usuarioRepository
                            .findById(
                                    dto.getIdUsuarioCliente()
                            )
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Cliente não encontrado."
                                            )
                            );


            if (
                    cliente.getTipo()
                            != TipoUsuario.USUARIO
            ) {

                throw new BusinessException(
                        "O responsável pela empresa deve ser um usuário comum."
                );
            }


            if (
                    !Boolean.TRUE.equals(
                            cliente.getAtivo()
                    )
            ) {

                throw new BusinessException(
                        "O cliente responsável está inativo."
                );
            }
        }


        /*
         * ========================================================
         * MONTAR EMPRESA
         * ========================================================
         */

        Empresa empresa =
                new Empresa();


        empresa.setRazaoSocial(
                dto.getRazaoSocial().trim()
        );


        empresa.setNomeFantasia(
                dto.getNomeFantasia() != null
                        &&
                        !dto.getNomeFantasia().isBlank()

                        ? dto.getNomeFantasia().trim()
                        : null
        );


        empresa.setCnpj(
                cnpj
        );


        empresa.setRegimeTributario(
                dto.getRegimeTributario()
        );


        empresa.setCliente(
                cliente
        );


        Empresa salva =
                empresaRepository.save(
                        empresa
                );


        return converterParaDTO(
                salva
        );
    }


    /*
     * ============================================================
     * LISTAR TODAS
     * CONTADOR / ADMINISTRADOR
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarTodas() {

        return empresaRepository
                .findAll()
                .stream()
                .map(
                        this::converterParaDTO
                )
                .toList();
    }


    /*
     * ============================================================
     * LISTAR EMPRESAS DO USUÁRIO
     * ============================================================
     */

    @Transactional(readOnly = true)
    public List<EmpresaResponseDTO> listarPorUsuario(
            Integer idUsuario
    ) {

        return empresaRepository
                .findByCliente_Id(
                        idUsuario
                )
                .stream()
                .map(
                        this::converterParaDTO
                )
                .toList();
    }


    /*
     * ============================================================
     * BUSCAR EMPRESA POR ID
     * ============================================================
     */

    @Transactional(readOnly = true)
    public EmpresaResponseDTO buscarPorId(
            Integer id
    ) {

        Empresa empresa =
                buscarEntidadePorId(
                        id
                );


        return converterParaDTO(
                empresa
        );
    }


    /*
     * ============================================================
     * BUSCAR ENTIDADE POR ID
     * ============================================================
     */

    @Transactional(readOnly = true)
    public Empresa buscarEntidadePorId(
            Integer id
    ) {

        return empresaRepository
                .findById(
                        id
                )
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Empresa não encontrada com ID: "
                                                + id
                                )
                );
    }


    /*
     * ============================================================
     * VERIFICAR PROPRIEDADE DA EMPRESA
     * ============================================================
     */

    @Transactional(readOnly = true)
    public boolean empresaPertenceAoUsuario(
            Integer idEmpresa,
            Integer idUsuario
    ) {

        Empresa empresa =
                buscarEntidadePorId(
                        idEmpresa
                );


        if (
                empresa.getCliente() == null
                        ||
                        idUsuario == null
        ) {

            return false;
        }


        return empresa
                .getCliente()
                .getId()
                == idUsuario.intValue();
    }


    /*
     * ============================================================
     * CONVERTER EMPRESA PARA DTO
     * ============================================================
     */

    private EmpresaResponseDTO converterParaDTO(
            Empresa empresa
    ) {

        return EmpresaResponseDTO
                .builder()

                .idEmpresa(
                        empresa.getIdEmpresa()
                )

                .razaoSocial(
                        empresa.getRazaoSocial()
                )

                .nomeFantasia(
                        empresa.getNomeFantasia()
                )

                .cnpj(
                        empresa.getCnpj()
                )

                .regimeTributario(
                        empresa.getRegimeTributario()
                )

                .idUsuarioCliente(
                        empresa.getCliente() != null
                                ? empresa.getCliente().getId()
                                : null
                )

                .nomeCliente(
                        empresa.getCliente() != null
                                ? empresa.getCliente().getNome()
                                : null
                )

                .criadoEm(
                        empresa.getCriadoEm()
                )

                .build();
    }
}