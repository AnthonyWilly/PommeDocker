package com.ufcg.psoft.commerce.service.servico;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do service dos serviços")
public class ServicoServiceTests {

    @Mock
    ServicoRepository servicoRepository;

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    EmpresaRepository empresaRepository;

    @InjectMocks
    ServicoServiceImpl servicoService;

    Cliente clienteBasico;
    Cliente clientePremium;
    Servico servicoBasico;
    Servico servicoPremium;
    Empresa empresa;
    
    @BeforeEach
    void setup() {

        clienteBasico = Cliente.builder()
                .id(1L)
                .nome("João da Silva")
                .endereco("Rua A, 123")
                .codigo("123456")
                .planoAtual("Basico")
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build();

        clientePremium = Cliente.builder()
                .id(2L)
                .nome("João da Silva 2")
                .endereco("Rua B, 123")
                .codigo("654321")
                .planoAtual("Premium")
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build();

        empresa = Empresa.builder()
                .id(1L)
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .build();

        servicoBasico = Servico.builder()
                .id(1L)
                .nome("Reparo Hidraulico")
                .tipo("Hidraulica")
                .precoBase(100.0)
                .disponibilidadePlano("Basico") 
                .empresa(empresa)
                .build();

        servicoPremium = Servico.builder()
                .id(2L)
                .nome("Guinchar carro")
                .tipo("Emergencia")
                .precoBase(500.0)
                .disponibilidadePlano("Premium")
                .empresa(empresa)
                .build();

    }

    @Nested
    @DisplayName("Conlunto de testes de listagem do catálogo de serviços")
    class ListagemDeServico {

        @Test
        @DisplayName("Listar serviços básicos para clientes básicos")
        void listarServicosBasicos() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.filtrarCatalogo(eq("Basico"), any(), any(), any()))
                .thenReturn(Arrays.asList(servicoBasico));
            
            // Act
            List<Servico> resultado = servicoService.listarCatalogoParaCliente(1L, null, null, null);

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> verify(servicoRepository, times(1)).filtrarCatalogo(anyString(), any(), any(), any()),
                () -> verify(servicoRepository).filtrarCatalogo(eq("Basico"), any(), any(), any())
            );
        }

        @Test
        @DisplayName("Listar serviços premium para clientes premium")
        void listarServicosPremium() {

            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.filtrarCatalogo(any(), any(), any(), any()))
                .thenReturn(Arrays.asList(servicoBasico, servicoPremium));
            
            // Act
            List<Servico> resultado = servicoService.listarCatalogoParaCliente(2L, null, null, null);

            // Assert
            assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> assertEquals("Guinchar carro", resultado.get(1).getNome()),
                () -> assertTrue(resultado.contains(servicoBasico)),
                () -> assertTrue(resultado.contains(servicoPremium)),
                () -> verify(servicoRepository, times(1)).filtrarCatalogo(anyString(), any(), any(), any())
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando o cliente não existir")
        void quandoClienteNaoExistir() {
            // Arrange
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                servicoService.listarCatalogoParaCliente(99L, null, null, null);
            });
        } 

        @Test
        @DisplayName("Listar serviços por tipo existente")
        void listarServicosPorTipoExistente() {

            // arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.filtrarCatalogo(any(), "Hidraulica", any(), any()))
                .thenReturn(Arrays.asList(servicoBasico));
            
            // act
            List<Servico> resultado = servicoService.listarCatalogoParaCliente(2L, "Hidraulica", null, null);

            // assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> assertTrue(resultado.contains(servicoBasico)),
                () -> verify(servicoRepository, times(1)).filtrarCatalogo(any(), anyString(), any(), any())
            );
        }

        @Test
        @DisplayName("Listar serviços por tipo inexistente")
        void listarServicosPorTipoInexistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.filtrarCatalogo("Basico", "Emergencia", any(), any()))
                .thenReturn(Arrays.asList());
            
            // Act
            List<Servico> resultado = servicoService.listarCatalogoParaCliente(1L, "Emergencia", null, null);

            // Assert
            assertAll(
                () -> assertEquals(0, resultado.size()),
                () -> verify(servicoRepository, times(1)).filtrarCatalogo(anyString(), anyString(), any(), any())
            );
        }

        @Test
        @DisplayName("Listar serviços por empresa existente")
        void listarServicosPorEmpresaExistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.filtrarCatalogo("Basico", any() , 1L, any()))
                .thenReturn(Arrays.asList(servicoBasico));
            
            // Act
            List<Servico> resultado = servicoService.listarCatalogoParaCliente(1L, null, 1L, null);

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).getEmpresa().getId()),
                () -> verify(servicoRepository, times(1)).filtrarCatalogo(anyString(), any(), any(), any())
            );
        }

        @Test
        @DisplayName("Listar serviços por empresa inexistente")
        void listarServicosPorEmpresaInexistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.filtrarCatalogo("Basico", any() , 2L, any()))
                .thenReturn(Arrays.asList());
            
            // Act
            List<Servico> resultado = servicoService.listarCatalogoParaCliente(1L, null, 2L, null);

            // Assert
            assertAll(
                () -> assertEquals(0, resultado.size()),
                () -> verify(servicoRepository, times(1)).filtrarCatalogo(anyString(), any(), any(), any())
            );
        }

        @Test
        @DisplayName("Listar serviços por faixa de preço")
        void listarServicosPorFaixaDePreco() {

            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.filtrarCatalogo(any(), any(), any(), 200.00))
                .thenReturn(Arrays.asList(servicoBasico));
            
            // Act
            List<Servico> resultado = servicoService.listarCatalogoParaCliente(2L, null, null, 200.00);

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertTrue(resultado.get(0).getPrecoBase() <= 200.00),
                () -> assertTrue(resultado.contains(servicoBasico)),
                () -> verify(servicoRepository, times(1)).filtrarCatalogo(anyString(), any(), any(), any())
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de testes de contratação de serviços")
    class ContratacaoDeServicos {

        @Test
        @DisplayName("Contratação quando o plano é compatível")
        void contratarServicoValido () {

            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoBasico));

            // Act
            servicoService.contratarServico(2L, 1L);

            // Assert
            verify(servicoRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente básico tenta contratar serviço premium")
        void contratarServicoInvalido () {

            // Arrange
            Long clienteId = 1L;
            Long servicoId = 2L; 

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(servicoId)).thenReturn(Optional.of(servicoPremium));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                servicoService.contratarServico(clienteId, servicoId);
            });

            assertEquals("Seu plano não permite a contratação deste serviço premium!", exception.getMessage());
        }
    }
}