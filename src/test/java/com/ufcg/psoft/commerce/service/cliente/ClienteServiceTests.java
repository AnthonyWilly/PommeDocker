package com.ufcg.psoft.commerce.service.cliente;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.HistoricoPlano;
import com.ufcg.psoft.commerce.model.PlanoBasico;
import com.ufcg.psoft.commerce.model.PlanoPremium;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.HistoricoPlanoRepository;

@ExtendWith(MockitoExtension.class) 
@DisplayName("Testes do service de clientes")
public class ClienteServiceTests {
     
    @Mock
    ClienteRepository clienteRepository;

    @Mock
    HistoricoPlanoRepository historicoRepository;

    @InjectMocks
    ClienteServiceImpl clienteService;

    Cliente cliente;

    @BeforeEach
    void setup() {
        cliente = Cliente.builder() 
            .nome("Cliente")
            .endereco("Rua dos Testes, 123")
            .codigo("123456")
            .build();
    }

    @Nested 
    @DisplayName("Conjunto de casos de verificação de mudança de plano do cliente")
    class ClienteVerificacaoMudancaPlano { 

        @Test
        @DisplayName("Quando alteramos o plano do cliente para premium com dados válidos")
        void quandoAlteramosPlanoDoClienteParaPremiumValido() throws Exception {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente); 

            // Act
            boolean resultado = clienteService.setPlanoPremium(1L, "123456");
            

            // Assert
            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());
            Cliente clienteSalvo = clienteCaptor.getValue();
            
            assertAll(
                () -> assertTrue(resultado, "O plano não foi mudado com sucesso."),
                () -> assertInstanceOf(PlanoPremium.class, clienteSalvo.getPlano(), "O plano do cliente deveria ser uma instância do PlanoPremium")
            );
        }

        @Test
        @DisplayName("Quando alteramos o plano do cliente para basico com dados válidos")
        void quandoAlteramosPlanoDoClienteParaBasicoValido() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente); 

            // Act
            boolean resultado = clienteService.setPlanoBasico(1L, "123456");

            // Assert
            ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(clienteCaptor.capture());
            Cliente clienteSalvo = clienteCaptor.getValue();
            
            assertAll(
                () -> assertTrue(resultado, "O plano não foi mudado com sucesso."),
                () -> assertInstanceOf(PlanoBasico.class, clienteSalvo.getPlano(), "O plano do cliente deveria ser uma instância do PlanoBasico")
            );
        }

        @Test
        @DisplayName("Quando alteramos o plano do cliente com o código de validação inválido.")
        void quandoAlteramosPlanoCodigoAcessoInvalido() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente)); 

            // Act && Assert
            Throwable exception = assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
                clienteService.setPlanoPremium(1L, "123457");
            });
            assertEquals("Codigo de acesso invalido!", exception.getMessage()); 
        }

        @Test
        @DisplayName("Quando alteramos o plano de um cliente inexistente")
        void quandoAlteramosPlanoClienteInexistente() {
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
        void quandoAlteramosPlanoClienteParaPremiumDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente)); 

            // Act
            clienteService.setPlanoPremium(1L, "123456");

            // Assert
            ArgumentCaptor<HistoricoPlano> historicoCaptor = ArgumentCaptor.forClass(HistoricoPlano.class);
            verify(historicoRepository).save(historicoCaptor.capture());
            HistoricoPlano historicoSalvo = historicoCaptor.getValue();

            assertEquals("Premium", historicoSalvo.getTipoPlano(), "O histórico deve registrar a string 'Premium'");
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente foi modificado ao trocar o plano para basico")
        void quandoAlteramosPlanoClienteParaBasicoDeveSalvarHistorico() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente)); 

            // Act
            clienteService.setPlanoBasico(1L, "123456");

            // Assert
            ArgumentCaptor<HistoricoPlano> historicoCaptor = ArgumentCaptor.forClass(HistoricoPlano.class);
            verify(historicoRepository).save(historicoCaptor.capture());
            HistoricoPlano historicoSalvo = historicoCaptor.getValue();

            assertEquals("Basico", historicoSalvo.getTipoPlano(), "O histórico deve registrar a string 'Basico'");
        }

        @Test
        @DisplayName("Verificar se histórico de plano de um cliente não foi modificado ao trocar o plano para Premium com código de cliente inválido")
        void quandoAlteramosPlanoClienteComCodigoInvalidoParaPremiumNaoDeveSalvarHistorico() {
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
        void quandoAlteramosPlanoClienteComClienteInexistenteParaPremiumNaoDeveSalvarHistorico() {
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
        void quandoAlteramosPlanoClienteComCodigoInvalidoParaBasicoNaoDeveSalvarHistorico() {
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
        void quandoAlteramosPlanoClienteComClienteInexistenteParaBasicoNaoDeveSalvarHistorico() {
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