package com.ufcg.psoft.commerce.service.interesseServico;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Service de Interesse dos clientes em Serviço indisponíveis")
public class InteresseServiceTests {

    @Mock
    ClienteRepository clienteRepository;

    @Mock
    ServicoRepository servicoRepository;

    @Mock
    InteresseRepository interesseRepository;

    @InjectMocks
    InteresseServiceImpl interesseService;

    Empresa empresa;
    Cliente clienteBasico;
    Cliente clientePremium;
    Servico servicoDisponivelBasico;
    Servico servicoDisponivelPremium;
    Servico servicoIndisponivelBasico;
    Servico servicoIndisponivelPremium;

    @BeforeEach
    void setup() {

        empresa = Empresa.builder()
                .id(1L)
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .build();

        clienteBasico = Cliente.builder()
                .id(1L)
                .nome("João da Silva")
                .endereco("Rua A, 123")
                .codigo("123456")
                .planoAtual(Plano.BASICO)
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build();

        clientePremium = Cliente.builder()
                .id(2L)
                .nome("Maria da Silva")
                .endereco("Rua B, 123")
                .codigo("654321")
                .planoAtual(Plano.PREMIUM)
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build();

        servicoDisponivelBasico = Servico.builder()
                .id(1L)
                .nome("Instalacao de Chuveiro")
                .tipo(TipoServico.ELETRICA)
                .descricao("instala um chuveiro")
                .urgencia(Urgencia.NORMAL)
                .preco(150.0)
                .disponivel(true)
                .plano(Plano.BASICO)
                .duracao(3.0)
                .empresa(empresa)
                .build();
                
        servicoDisponivelPremium = Servico.builder()
                .id(2L)
                .nome("Instalacao de Chuveiro de última geração")
                .tipo(TipoServico.ELETRICA)
                .descricao("instala um chuveiro que cobrimos o custo caso a fiação da sua casa queime")
                .urgencia(Urgencia.ALTA)
                .preco(350.0)
                .disponivel(true)
                .plano(Plano.PREMIUM)
                .duracao(4.0)
                .empresa(empresa)
                .build();

        servicoIndisponivelBasico = Servico.builder()
                .id(3L)
                .nome("Pintura de parede")
                .tipo(TipoServico.PINTURA)
                .descricao("Pintura de uma parede")
                .urgencia(Urgencia.BAIXA)
                .preco(150.0)
                .disponivel(false)
                .plano(Plano.BASICO)
                .duracao(4.0)
                .empresa(empresa)
                .build();

        servicoIndisponivelPremium = Servico.builder()
                .id(4L)
                .nome("Pintura de parede alto padrão")
                .tipo(TipoServico.PINTURA)
                .descricao("Pintura de uma parede no estilo alto padrão")
                .urgencia(Urgencia.ALTA)
                .preco(550.0)
                .disponivel(false)
                .plano(Plano.PREMIUM)
                .duracao(8.0)
                .empresa(empresa)
                .build();
    }

    @Nested
    @DisplayName("Testes de demonstração de interesse com sucesso")
    class demonstrarInteresseSucesso {

        @Test
        @DisplayName("Deve demonstrar interesse com sucesso quando serviço indisponível e cliente compatível")
        void demonstrarInteresseComSucesso() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(3L)).thenReturn(Optional.of(servicoIndisponivelBasico));
            
            when(interesseRepository.save(any(Interesse.class))).thenAnswer(r -> r.getArguments()[0]);

            // Act
            Interesse resultado = interesseService.demonstrarInteresse(1L, 3L);

            // Assert
            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(clienteBasico, resultado.getCliente()),
                    () -> assertEquals(servicoIndisponivelBasico, resultado.getServico()),
                    () -> verify(interesseRepository, times(1)).save(any(Interesse.class))
            );
        }

        @Test
        @DisplayName("Deve demonstrar interesse com sucesso quando cliente premium se interessa por serviço básico")
        void demonstrarInteressePremiumParaBasico() {
            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findById(4L)).thenReturn(Optional.of(servicoIndisponivelBasico));
            when(interesseRepository.save(any(Interesse.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            Interesse resultado = interesseService.demonstrarInteresse(2L, 4L);

            // Assert
            assertAll(
                    () -> assertNotNull(resultado),
                    () -> verify(interesseRepository, times(1)).save(any(Interesse.class))
            );
        }
    }

    @Nested
    @DisplayName("Testes de demonstração de interesse que devem lançar erro")
    class demonstrarInteresseFalha {

        @Test
        @DisplayName("Deve lançar erro ao tentar demonstrar interesse em serviço disponível")
        void demonstrarInteresseEmServicoDisponivel() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(1L)).thenReturn(Optional.of(servicoDisponivelBasico));

            // Act & Assert
            RuntimeException exception = assertThrows(DemonstrarInteresseInvalidoException.class, 
                () -> interesseService.demonstrarInteresse(1L, 1L)
            );

            assertAll(
                    () -> assertEquals("Não é possível demonstrar interesse em serviço disponível.", exception.getMessage()),
                    () -> verify(interesseRepository, never()).save(any())
            );
        }

        @Test
        @DisplayName("Deve lançar erro quando cliente básico tenta serviço premium")
        void demonstrarInteresseClienteBasicoServicoPremium() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(4L)).thenReturn(Optional.of(servicoIndisponivelPremium));

            // Act & Assert
            RuntimeException exception = assertThrows(DemonstrarInteresseInvalidoException.class, 
                () -> interesseService.demonstrarInteresse(1L, 4L));

            assertAll(
                    () -> assertEquals("Não é possível demonstrar interesse à um serviço premium com seu plano atual.", exception.getMessage()),
                    () -> verify(interesseRepository, never()).save(any())
            );
        }

        @Test
        @DisplayName("Deve lançar erro se o cliente não existir")
        void demonstrarInteresseClienteInexistente() {
            // Arrange
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(ClienteNaoExisteException.class,
                () -> interesseService.demonstrarInteresse(99L, 20L));
            
            assertAll(
                () -> assertEquals("O cliente consultado não existe!", exception.getMessage()),
                () -> verify(interesseRepository, never()).save(any())
            );
        }

        @Test
        @DisplayName("Deve lançar exceção se o serviço não existir")
        void demonstrarInteresseServicoInexistente() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(ServicoInexistenteException.class, () -> {
                interesseService.demonstrarInteresse(1L, 99L);
            });

            assertEquals("O serviço consultado não existe!", exception.getMessage());
            verify(interesseRepository, never()).save(any());
        }
    }

}