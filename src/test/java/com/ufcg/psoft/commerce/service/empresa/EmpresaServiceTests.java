package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.service.empresa.EmpresaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Empresa")
public class EmpresaServiceTests {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    private EmpresaPostPutRequestDTO empresaDTO;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresaDTO = EmpresaPostPutRequestDTO.builder()
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .senhaAdmin("admin123")
                .build();
        
        empresa = Empresa.builder()
                .id(1L)
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .build();
    }

    @Test
    @DisplayName("Cadastrar empresa com sucesso")
    void testCadastrarEmpresaSucesso() {
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO resultado = empresaService.cadastrar(empresaDTO);

        assertNotNull(resultado);
        assertEquals(empresa.getNome(), resultado.getNome());
        assertEquals(empresa.getCnpj(), resultado.getCnpj());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Recuperar empresa com sucesso")
    void testRecuperarEmpresaSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        EmpresaResponseDTO resultado = empresaService.recuperar(1L);

        assertNotNull(resultado);
        assertEquals(empresa.getId(), resultado.getId());
        verify(empresaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Alterar empresa com sucesso")
    void testAlterarEmpresaSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);

        EmpresaResponseDTO resultado = empresaService.alterar(1L, "123456", empresaDTO);

        assertNotNull(resultado);
        assertEquals(empresa.getNome(), resultado.getNome());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Remover empresa com sucesso")
    void testRemoverEmpresaSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        doNothing().when(empresaRepository).delete(any(Empresa.class));

        empresaService.remover(1L, "123456", "admin123");

        verify(empresaRepository, times(1)).delete(any(Empresa.class));
    }

    @Test
    @DisplayName("Alterar empresa com código de acesso inválido")
    void testAlterarEmpresaCodigoAcessoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
            empresaService.alterar(1L, "000000", empresaDTO);
        });

        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Remover empresa com código de acesso inválido")
    void testRemoverEmpresaCodigoAcessoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
            empresaService.remover(1L, "000000", "admin123");
        });

        verify(empresaRepository, never()).delete(any(Empresa.class));
    }
}