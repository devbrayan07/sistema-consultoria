package com.ivig.sistemaconsultoria.service;

import com.ivig.sistemaconsultoria.dto.EmpresaRequestDTO;
import com.ivig.sistemaconsultoria.dto.EmpresaResponseDTO;
import com.ivig.sistemaconsultoria.enums.RegimeTributario;
import com.ivig.sistemaconsultoria.enums.TipoUsuario;
import com.ivig.sistemaconsultoria.exceptions.BusinessException;
import com.ivig.sistemaconsultoria.exceptions.ResourceNotFoundException;
import com.ivig.sistemaconsultoria.model.Empresa;
import com.ivig.sistemaconsultoria.model.Usuario;
import com.ivig.sistemaconsultoria.repository.EmpresaRepository;
import com.ivig.sistemaconsultoria.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private EmpresaService empresaService;

    private Usuario usuario;


    @BeforeEach
    void setUp() {

        usuario = new Usuario();

        usuario.setId(1);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@email.com");
        usuario.setTipo(TipoUsuario.USUARIO);
        usuario.setAtivo(true);
    }


    @Test
    void deveCriarEmpresaComSucesso() {

        EmpresaRequestDTO dto = new EmpresaRequestDTO();

        dto.setRazaoSocial("Empresa Teste LTDA");
        dto.setNomeFantasia("Empresa Teste");
        dto.setCnpj("12345678000199");
        dto.setRegimeTributario(RegimeTributario.SIMPLES_NACIONAL);
        dto.setIdUsuarioCliente(1);


        Empresa empresaSalva = new Empresa();

        empresaSalva.setIdEmpresa(1);
        empresaSalva.setRazaoSocial(dto.getRazaoSocial());
        empresaSalva.setNomeFantasia(dto.getNomeFantasia());
        empresaSalva.setCnpj(dto.getCnpj());
        empresaSalva.setRegimeTributario(dto.getRegimeTributario());
        empresaSalva.setCliente(usuario);


        when(
                empresaRepository.existsByCnpj(
                        dto.getCnpj()
                )
        ).thenReturn(false);


        when(
                usuarioRepository.findById(1)
        ).thenReturn(
                Optional.of(usuario)
        );


        when(
                empresaRepository.save(
                        any(Empresa.class)
                )
        ).thenReturn(
                empresaSalva
        );


        EmpresaResponseDTO resultado =
                empresaService.criar(
                        dto,
                        usuario
                );


        assertNotNull(resultado);

        assertEquals(
                "Empresa Teste LTDA",
                resultado.getRazaoSocial()
        );

        assertEquals(
                "12345678000199",
                resultado.getCnpj()
        );


        verify(
                empresaRepository
        ).save(
                any(Empresa.class)
        );
    }


    @Test
    void deveLancarErroQuandoCnpjJaExiste() {

        EmpresaRequestDTO dto =
                new EmpresaRequestDTO();

        dto.setCnpj(
                "12345678000199"
        );


        when(
                empresaRepository.existsByCnpj(
                        dto.getCnpj()
                )
        ).thenReturn(true);


        assertThrows(
                BusinessException.class,
                () -> empresaService.criar(
                        dto,
                        usuario
                )
        );


        verify(
                usuarioRepository,
                never()
        ).findById(
                any()
        );
    }


    @Test
    void deveLancarErroQuandoUsuarioNaoExiste() {

        EmpresaRequestDTO dto =
                new EmpresaRequestDTO();

        dto.setCnpj(
                "12345678000199"
        );

        dto.setIdUsuarioCliente(1);


        when(
                empresaRepository.existsByCnpj(
                        dto.getCnpj()
                )
        ).thenReturn(false);


        when(
                usuarioRepository.findById(1)
        ).thenReturn(
                Optional.empty()
        );


        assertThrows(
                ResourceNotFoundException.class,
                () -> empresaService.criar(
                        dto,
                        usuario
                )
        );
    }


    @Test
    void deveBuscarEmpresaPorId() {

        Empresa empresa =
                new Empresa();

        empresa.setIdEmpresa(1);
        empresa.setRazaoSocial(
                "Empresa Teste"
        );


        when(
                empresaRepository.findById(1)
        ).thenReturn(
                Optional.of(empresa)
        );


        EmpresaResponseDTO resultado =
                empresaService.buscarPorId(1);


        assertEquals(
                "Empresa Teste",
                resultado.getRazaoSocial()
        );
    }


    @Test
    void deveLancarErroQuandoEmpresaNaoExiste() {

        when(
                empresaRepository.findById(99)
        ).thenReturn(
                Optional.empty()
        );


        assertThrows(
                ResourceNotFoundException.class,
                () -> empresaService.buscarPorId(99)
        );
    }


    @Test
    void deveListarEmpresas() {

        Empresa empresa =
                new Empresa();

        empresa.setIdEmpresa(1);
        empresa.setRazaoSocial(
                "Empresa Teste"
        );


        when(
                empresaRepository.findAll()
        ).thenReturn(
                List.of(empresa)
        );


        List<EmpresaResponseDTO> resultado =
                empresaService.listarTodas();


        assertEquals(
                1,
                resultado.size()
        );


        assertEquals(
                "Empresa Teste",
                resultado
                        .get(0)
                        .getRazaoSocial()
        );
    }


    @Test
    void deveRetornarListaVaziaQuandoNaoExistirEmpresa() {

        when(
                empresaRepository.findAll()
        ).thenReturn(
                List.of()
        );


        List<EmpresaResponseDTO> resultado =
                empresaService.listarTodas();


        assertTrue(
                resultado.isEmpty()
        );
    }
}