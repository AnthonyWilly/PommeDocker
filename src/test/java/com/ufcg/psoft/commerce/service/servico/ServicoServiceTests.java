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

import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
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
                .planoAtual("BASICO")
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build();

        clientePremium = Cliente.builder()
                .id(2L)
                .nome("João da Silva 2")
                .endereco("Rua B, 123")
                .codigo("654321")
                .planoAtual("PREMIUM")
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
                .tipo(TipoServico.HIDRAULICA)
                .preco(100.0)
                .idPlano("BASICO") 
                .empresa(empresa)
                .build();

        servicoPremium = Servico.builder()
                .id(2L)
                .nome("Guinchar carro")
                .tipo(TipoServico.LIMPEZA)
                .preco(500.0)
                .idPlano("PREMIUM")
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
            when(servicoRepository.findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList("BASICO"))))
                .thenReturn(Arrays.asList(servicoBasico));

            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), new ServicoFiltroDTO());

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList("BASICO")))
            );
        }

        @Test
        @DisplayName("Listar serviços premium para clientes premium")
        void listarServicosPremium() {

            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList("BASICO", "PREMIUM"))))
                .thenReturn(Arrays.asList(servicoBasico, servicoPremium));
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clientePremium.getId(), new ServicoFiltroDTO());

            // Assert
            assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> assertEquals("Guinchar carro", resultado.get(1).getNome()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList("BASICO", "PREMIUM")))
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando o cliente não existir")
        void quandoClienteNaoExistir() {
            // Arrange
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                servicoService.listarCatalogoServicoCliente(99L, new ServicoFiltroDTO());
            });
        } 

        @Test
        @DisplayName("Listar serviços por tipo existente")
        void listarServicosPorTipoExistente() {

            // arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findAllComFiltros(eq(TipoServico.HIDRAULICA), any(), any(), any(), any(), eq(Arrays.asList("BASICO", "PREMIUM"))))
                .thenReturn(Arrays.asList(servicoBasico));
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .tipo(TipoServico.HIDRAULICA)
                                .build(); 
            // act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clientePremium.getId(), filtro); 

            // assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(eq(TipoServico.HIDRAULICA), any(), any(), any(), any(), eq(Arrays.asList("BASICO", "PREMIUM")))
            );
        }

        @Test
        @DisplayName("Listar serviços por tipo inexistente")
        void listarServicosPorTipoInexistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findAllComFiltros(eq(TipoServico.LIMPEZA), any(), any(), any(), any(), eq(Arrays.asList("BASICO"))))
                .thenReturn(Arrays.asList());
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .tipo(TipoServico.LIMPEZA)
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), filtro);

            // Assert
            assertAll(
                () -> assertEquals(0, resultado.size()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(eq(TipoServico.LIMPEZA), any(), any(), any(), any(), eq(Arrays.asList("BASICO")))
            );
        }

        @Test
        @DisplayName("Listar serviços por empresa existente")
        void listarServicosPorEmpresaExistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findAllComFiltros(any(), eq(empresa.getId()), any(), any(), any(), eq(Arrays.asList("BASICO"))))
                .thenReturn(Arrays.asList(servicoBasico));
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .empresaId(empresa.getId())
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), filtro);

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).getEmpresaId()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), eq(empresa.getId()), any(), any(), any(), eq(Arrays.asList("BASICO")))
            );
        }

        @Test
        @DisplayName("Listar serviços por empresa inexistente")
        void listarServicosPorEmpresaInexistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findAllComFiltros(any(), eq(2L), any(), any(), any(), eq(Arrays.asList("BASICO"))))
                .thenReturn(Arrays.asList());
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .empresaId(2L)
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), filtro); 

            // Assert
            assertAll(
                () -> assertEquals(0, resultado.size()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), eq(2L), any(), any(), any(), eq(Arrays.asList("BASICO")))
            );
        }

        @Test
        @DisplayName("Listar serviços por faixa de preço")
        void listarServicosPorFaixaDePreco() {

            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findAllComFiltros(any(), any(), any(), any(), eq(200.00), eq(Arrays.asList("BASICO", "PREMIUM"))))
                .thenReturn(Arrays.asList(servicoBasico));
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .precoMax(200.00)
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clientePremium.getId(), filtro);

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertTrue(resultado.get(0).getPreco() <= 200.00),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), any(), any(), any(), eq(200.00), eq(Arrays.asList("BASICO", "PREMIUM")))
            );
        }
    }
}