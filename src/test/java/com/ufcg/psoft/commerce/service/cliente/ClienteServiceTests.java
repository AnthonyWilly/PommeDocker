package com.ufcg.psoft.commerce.service.cliente;
import com.ufcg.psoft.commerce.dto.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;


import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Service de Clientes")
class ClienteServiceTests {


   @Mock
   ClienteRepository clienteRepository;


   @Mock
   ModelMapper modelMapper;


   @InjectMocks
   ClienteServiceImpl clienteService;


   Cliente cliente;
   ClientePostPutRequestDTO clienteDTO;
   ClienteResponseDTO clienteResponseDTO;

   @BeforeEach
   void setup() {
       cliente = Cliente.builder()
               .id(1L)
               .nome("João da Silva")
               .endereco("Rua A, 123")
               .codigo("123456")
               .planoAtual("Basico")
               .build();


       clienteDTO = ClientePostPutRequestDTO.builder()
               .nome("João da Silva")
               .endereco("Rua A, 123")
               .codigo("123456")
               .build();


       clienteResponseDTO = ClienteResponseDTO.builder()
               .id(1L)
               .nome("João da Silva")
               .planoAtual("Basico")
               .build();
   }


   @Test
   @DisplayName("Deve criar cliente com plano padrao Basico")
   void deveCriarClientePlanoPadrao() {
       when(modelMapper.map(clienteDTO, Cliente.class)).thenReturn(cliente);
       when(clienteRepository.save(cliente)).thenReturn(cliente);
       when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);


       ClienteResponseDTO resultado = clienteService.criar(clienteDTO);


       assertNotNull(resultado);
       assertEquals("Basico", resultado.getPlanoAtual());
       verify(clienteRepository, times(1)).save(cliente);
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
      
       doAnswer(invocation -> {
           Cliente entity = invocation.getArgument(1);
           entity.setNome("João Atualizado");
           entity.setEndereco("Rua Nova, 999");
           return null;
       }).when(modelMapper).map(dtoAtualizacao, cliente);


       when(clienteRepository.save(cliente)).thenReturn(cliente);
      
       ClienteResponseDTO responseAtualizado = ClienteResponseDTO.builder()
               .nome("João Atualizado")
               .endereco("Rua Nova, 999")
               .planoAtual("Basico")
               .build();
       when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(responseAtualizado);


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


       doAnswer(invocation -> {
           Cliente entity = invocation.getArgument(1);
           entity.setNome("Nome Novo Só");
           return null;
       }).when(modelMapper).map(dtoSoNome, cliente);


       when(clienteRepository.save(cliente)).thenReturn(cliente);
       when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);


       clienteService.alterar(1L, "123456", dtoSoNome);


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


       doAnswer(invocation -> {
           Cliente entity = invocation.getArgument(1);
           entity.setEndereco("Endereço Novo Só");
           return null;
       }).when(modelMapper).map(dtoSoEndereco, cliente);


       when(clienteRepository.save(cliente)).thenReturn(cliente);
       when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);


       clienteService.alterar(1L, "123456", dtoSoEndereco);


       verify(clienteRepository, times(1)).save(cliente);
   }


    @Test
    @DisplayName("Deve setar plano Premium e salvar histórico")
    void deveSetarPlanoPremium() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setPlanoAtual("Premium");
            return c;
        });
        ClienteResponseDTO responsePremium = ClienteResponseDTO.builder().planoAtual("Premium").build();
        when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class))).thenReturn(responsePremium);
        ClienteResponseDTO resultado = clienteService.setPlanoPremium(1L, "123456");
        assertEquals("Premium", resultado.getPlanoAtual());
        verify(clienteRepository, times(1)).save(cliente);
        verify(historicoRepository, times(1)).save(any());
    }


    @Test
    @DisplayName("Deve setar plano basico e salvar histórico")
    void deveSetarPlanoBasico() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente c = invocation.getArgument(0);
            c.setPlanoAtual("Basico");
            return c;
        });
        ClienteResponseDTO responseBasico = ClienteResponseDTO.builder().planoAtual("Basico").build();
        when(modelMapper.map(any(Cliente.class), eq(ClienteResponseDTO.class))).thenReturn(responseBasico);
        ClienteResponseDTO resultado = clienteService.setPlanoBasico(1L, "123456");
        assertEquals("Basico", resultado.getPlanoAtual());
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
   @DisplayName("Deve remover cliente com código de acesso correto")
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

