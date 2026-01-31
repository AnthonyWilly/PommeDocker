package com.ufcg.psoft.commerce.service.cliente;

import com.ufcg.psoft.commerce.dto.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.HistoricoPlano;
import com.ufcg.psoft.commerce.model.PlanoBasico;
import com.ufcg.psoft.commerce.model.PlanoPremium;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.HistoricoPlanoRepository;
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
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


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
    ClientePostPutRequestDTO clienteDTO;

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
      
        clienteDTO = ClientePostPutRequestDTO.builder()
                .nome("João da Silva")
                .endereco("Rua A, 123")
                .codigo("123456")
                .build();

    }
  
    @Nested
    @DisplayName("Conjunto de casos de verificação de fluxos básicos")
    class ClienteVerificacaoFluxoBasico {
      
        @Test
        @DisplayName("Deve criar cliente com plano padrao Basico")
        void deveCriarClientePlanoPadrao() {
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

            ClienteResponseDTO resultado = clienteService.criar(clienteDTO);

            assertNotNull(resultado);
            assertEquals("Basico", resultado.getPlanoAtual());
            verify(clienteRepository, times(1)).save(any(Cliente.class));
        }

        @Test
        @DisplayName("Deve alterar todos os dados do cliente (exceto plano)")
        void deveAlterarClienteCompleto() {
            ClientePostPutRequestDTO dtoAtualizacao = ClientePostPutRequestDTO.builder()
                    .nome("João Atualizado")
                    .endereco("Rua Nova, 999")
                    .codigo("123456")
                    .build();

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(cliente)).thenReturn(cliente);

            ClienteResponseDTO resultado = clienteService.alterar(1L, "123456", dtoAtualizacao);

            assertEquals("João Atualizado", resultado.getNome());
            assertEquals("Rua Nova, 999", resultado.getEndereco());
            verify(clienteRepository, times(1)).save(cliente);
        }

        @Test
        @DisplayName("Deve alterar apenas o Nome do cliente")
        void deveAlterarApenasNome() {
            ClientePostPutRequestDTO dtoSoNome = ClientePostPutRequestDTO.builder()
                    .nome("Nome Novo Só")
                    .codigo("123456")
                    .build();

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(cliente)).thenReturn(cliente);

            clienteService.alterar(1L, "123456", dtoSoNome);

            assertEquals("Nome Novo Só", cliente.getNome());
            verify(clienteRepository, times(1)).save(cliente);
        }

        @Test
        @DisplayName("Deve alterar apenas o Endereço do cliente")
        void deveAlterarApenasEndereco() {
            ClientePostPutRequestDTO dtoSoEndereco = ClientePostPutRequestDTO.builder()
                    .endereco("Endereço Novo Só")
                    .codigo("123456")
                    .build();

            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(cliente)).thenReturn(cliente);

            clienteService.alterar(1L, "123456", dtoSoEndereco);

            assertEquals("Endereço Novo Só", cliente.getEndereco());
            verify(clienteRepository, times(1)).save(cliente);
        }

        @Test
        @DisplayName("Deve setar proxPlano como Premium e salvar histórico")
        void deveSetarProxPlanoPremium() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

            clienteService.setPlanoPremium(1L, "123456");

            assertEquals("Premium", cliente.getProxPlano());
            assertEquals("Basico", cliente.getPlanoAtual());
            verify(clienteRepository, times(1)).save(cliente);
            verify(historicoRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Deve setar proxPlano como Basico e salvar histórico")
        void deveSetarProxPlanoBasico() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            when(clienteRepository.save(any(Cliente.class))).thenReturn(cliente);

            cliente.setPlanoAtual("Premium");

            clienteService.setPlanoBasico(1L, "123456");

            assertEquals("Basico", cliente.getProxPlano());
            assertEquals("Premium", cliente.getPlanoAtual());
            verify(clienteRepository, times(1)).save(cliente);
            verify(historicoRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Deve falhar ao alterar cliente com código incorreto")
        void deveFalharAlterarCodigoIncorreto() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            assertThrows(CodigoDeAcessoInvalidoException.class, () ->
                    clienteService.alterar(1L, "000000", clienteDTO)
            );
            verify(clienteRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar ao alterar cliente inexistente")
        void deveFalharAlterarClienteInexistente() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ClienteNaoExisteException.class, () ->
                    clienteService.alterar(1L, "123456", clienteDTO)
            );
        }

        @Test
        @DisplayName("Deve remover cliente com código
            ClienteResponseDTO response =
                    clienteService.setPlanoBasico(1L, "123456");

            // Assert
            assertAll(
                    () -> assertNotNull(respon de acesso correto")
        void deveRemoverCliente() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            clienteService.remover(1L, "123456");
            verify(clienteRepository, times(1)).delete(cliente);
        }

        @Test
        @DisplayName("Deve falhar ao remover cliente com código incorreto")
        void deveFalharRemoverCodigoIncorreto() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
            assertThrows(CodigoDeAcessoInvalidoException.class, () ->
                    clienteService.remover(1L, "999999")
            );
            verify(clienteRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Deve recuperar cliente pelo ID")
        void deveRecuperarCliente() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

            ClienteResponseDTO resultado = clienteService.recuperar(1L);
            assertNotNull(resultado);
            assertEquals(cliente.getNome(), resultado.getNome());
        }

        @Test
        @DisplayName("Deve falhar ao recuperar cliente inexistente")
        void deveFalharRecuperarInexistente() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.empty());
            assertThrows(ClienteNaoExisteException.class, () ->
                    clienteService.recuperar(1L)
            );
        }

        @Test
        @DisplayName("Deve listar todos os clientes")
        void deveListarClientes() {
            when(clienteRepository.findAll()).thenReturn(List.of(cliente));
            List<ClienteResponseDTO> resultado = clienteService.listar();
            assertFalse(resultado.isEmpty());
            assertEquals(1, resultado.size());
        }

        @Test
        @DisplayName("Deve listar clientes por nome")
        void deveListarClientesPorNome() {
            when(clienteRepository.findByNomeContaining("João")).thenReturn(List.of(cliente));
            List<ClienteResponseDTO> resultado = clienteService.listarPorNome("João");
            assertFalse(resultado.isEmpty());
            assertEquals("João da Silva", resultado.get(0).getNome());
        }
      
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
