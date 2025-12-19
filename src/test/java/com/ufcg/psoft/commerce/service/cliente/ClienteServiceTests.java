package com.ufcg.psoft.commerce.service.cliente;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.HistoricoPlano;
import com.ufcg.psoft.commerce.model.PlanoBasico;
import com.ufcg.psoft.commerce.model.PlanoPremium;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.HistoricoPlanoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;


@ExtendWith(MockitoExtension.class) 
@DisplayName("Testes do service de clientes")
public class ClienteServiceTests {
     
    @Mock
    ClienteRepository clienteRepository;

    @Mock
    HistoricoPlanoRepository historicoRepository;
    @Mock
    PlanoBasico planoBasico;
    @Mock
    PlanoPremium planoPremium;
    @InjectMocks
    ClienteServiceImpl clienteService;

    @Spy
    ModelMapper modelMapper = new ModelMapper();
    Cliente cliente;

    @BeforeEach
    void setup() {
        modelMapper.getConfiguration().setSkipNullEnabled(true);
        clienteService.inicializarMapaDePlanos();
        cliente = Cliente.builder()
                .id(1L)
                .nome("João da Silva")
                .endereco("Rua A, 123")
                .codigo("123456")
                .planoAtual("Basico")
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build();

    }

    @Nested 
    @DisplayName("Conjunto de casos de verificação de mudança de plano do cliente")
    class ClienteVerificacaoMudancaPlano {
        @Test
        @DisplayName("Altera plano do cliente para premium com sucesso")
        void alteraPlanoDoClienteParaPremiumComSucesso() throws Exception {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);
            // Act
            ClienteResponseDTO response =
                    clienteService.setPlanoPremium(1L, "123456");
            // Assert
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals("Premium", response.getProxPlano(),
                            "O próximo plano do cliente deve ser Premium")
            );

            verify(clienteRepository).save(any(Cliente.class));
        }
        @Test
        @DisplayName("Altera plano do cliente para basico com sucesso")
        void alteraPlanoDoClienteParaBasicoValido() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

            // Act
            ClienteResponseDTO response =
                    clienteService.setPlanoBasico(1L, "123456");

            // Assert
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals("Basico", response.getProxPlano(),
                            "O próximo plano do cliente deve ser Basico")
            );
            verify(clienteRepository).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Altera o plano do cliente com o código de validação inválido.")
        void alteramosPlanoDoClienteCodigoAcessoInvalido() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente)); 

            // Act && Assert
            Throwable exception = assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
                clienteService.setPlanoPremium(1L, "123457");
            });
            assertEquals("Codigo de acesso invalido!", exception.getMessage()); 
        }

        @Test
        @DisplayName("Altera o plano de um cliente inexistente")
        void alteraPlanoClienteInexistente() {
            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.empty()); 

            // Act && Assert 
            Throwable exception = assertThrows(ClienteNaoExisteException.class, () -> {
                clienteService.setPlanoPremium(2L, "123456");
            });
            assertEquals("O cliente consultado nao existe!", exception.getMessage());
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente foi modificado ao trocar o plano para premium")
        void alteraPlanoClienteParaPremiumDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente)); 

            // Act
            clienteService.setPlanoPremium(1L, "123456");

            // Assert
            ArgumentCaptor<HistoricoPlano> historicoCaptor = ArgumentCaptor.forClass(HistoricoPlano.class);
            verify(historicoRepository).save(historicoCaptor.capture());
            HistoricoPlano historicoSalvo = historicoCaptor.getValue();

            assertEquals("Basico", historicoSalvo.getIdPlanoAntigo(), "O histórico deve registrar a string 'Basico'");
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente foi modificado ao trocar o plano para basico")
        void alteraPlanoClienteParaBasicoDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            // Act
            clienteService.setPlanoBasico(1L, "123456");
            // Assert
            ArgumentCaptor<HistoricoPlano> historicoCaptor = ArgumentCaptor.forClass(HistoricoPlano.class);
            verify(historicoRepository).save(historicoCaptor.capture());
            HistoricoPlano historicoSalvo = historicoCaptor.getValue();

            assertEquals("Basico", historicoSalvo.getIdPlanoAntigo(), "O histórico deve registrar a string 'Basico'");
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente não foi modificado ao trocar o plano para Premium com código de cliente inválido")
        void alteraPlanoClienteParaPremiumComCodigoInvalidoNaoDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente)); 

            // Act && Assert
            assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
                clienteService.setPlanoPremium(1L, "123457");
            });
            verify(historicoRepository, never()).save(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente não foi modificado ao trocar o plano para Premium com cliente inexistente")
        void alteraPlanoClienteParaPremiumClienteInexistenteNaoDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.empty());

            // Act && Assert
            assertThrows(ClienteNaoExisteException.class, () -> {
                clienteService.setPlanoPremium(2L, "123456");
            });
            verify(historicoRepository, never()).save(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente não foi modificado ao trocar o plano para Basico com código de cliente inválido")
        void alteraPlanoClienteParaBasicoComCodigoInvalidoNaoDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente)); 

            // Act && Assert
            assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
                clienteService.setPlanoBasico(1L, "123457");
            });
            verify(historicoRepository, never()).save(any());
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente não foi modificado ao trocar o plano para Premium com cliente inexistente")
        void alteraPlanoClienteParaBasicoComClienteInexistenteNaoDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.empty());

            // Act && Assert
            assertThrows(ClienteNaoExisteException.class, () -> {
                clienteService.setPlanoBasico(2L, "123456");
            });
            verify(historicoRepository, never()).save(any());
            verify(clienteRepository, never()).save(any());
        }
    }
}