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
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("João da Silva");
        cliente.setEndereco("Rua A, 123");
        cliente.setCodigo("123456");
        cliente.setPlano("BASICO");

        clienteDTO = new ClientePostPutRequestDTO();
        clienteDTO.setNome("João da Silva");
        clienteDTO.setEndereco("Rua A, 123");
        clienteDTO.setCodigoAcesso("123456");
        clienteDTO.setPlano("BASICO");

        clienteResponseDTO = new ClienteResponseDTO();
        clienteResponseDTO.setId(1L);
        clienteResponseDTO.setNome("João da Silva");
        clienteResponseDTO.setPlano("BASICO");
    }

    @Test
    @DisplayName("Deve criar cliente com plano Básico (Padrão)")
    void deveCriarClientePlanoBasico() {
        // Arrange
        clienteDTO.setPlano("BASICO");
        
        when(modelMapper.map(clienteDTO, Cliente.class)).thenReturn(cliente);
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // Act
        ClienteResponseDTO resultado = clienteService.criar(clienteDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("BASICO", resultado.getPlano());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Deve criar cliente com plano Premium")
    void deveCriarClientePlanoPremium() {
        // Arrange
        ClientePostPutRequestDTO dtoPremium = new ClientePostPutRequestDTO();
        dtoPremium.setNome("Maria Premium");
        dtoPremium.setCodigoAcesso("654321");
        dtoPremium.setPlano("PREMIUM");

        Cliente clientePremium = new Cliente();
        clientePremium.setId(2L);
        clientePremium.setNome("Maria Premium");
        clientePremium.setPlano("PREMIUM");

        ClienteResponseDTO responsePremium = new ClienteResponseDTO();
        responsePremium.setId(2L);
        responsePremium.setPlano("PREMIUM");

        when(modelMapper.map(dtoPremium, Cliente.class)).thenReturn(clientePremium);
        when(clienteRepository.save(clientePremium)).thenReturn(clientePremium);
        when(modelMapper.map(clientePremium, ClienteResponseDTO.class)).thenReturn(responsePremium);

        // Act
        ClienteResponseDTO resultado = clienteService.criar(dtoPremium);

        // Assert
        assertNotNull(resultado);
        assertEquals("PREMIUM", resultado.getPlano());
        verify(clienteRepository, times(1)).save(clientePremium);
    }

    @Test
    @DisplayName("Deve alterar cliente com código de acesso correto")
    void deveAlterarCliente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        
        doNothing().when(modelMapper).map(clienteDTO, cliente);
        
        when(clienteRepository.save(cliente)).thenReturn(cliente);
        when(modelMapper.map(cliente, ClienteResponseDTO.class)).thenReturn(clienteResponseDTO);

        // Act
        ClienteResponseDTO resultado = clienteService.alterar(1L, "123456", clienteDTO);

        // Assert
        assertNotNull(resultado);
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Deve falhar ao alterar cliente com código incorreto")
    void deveFalharAlterarCodigoIncorreto() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act & Assert
        assertThrows(CodigoDeAcessoInvalidoException.class, () -> 
            clienteService.alterar(1L, "000000", clienteDTO)
        );
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao alterar cliente inexistente")
    void deveFalharAlterarClienteInexistente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoExisteException.class, () -> 
            clienteService.alterar(1L, "123456", clienteDTO)
        );
    }

    @Test
    @DisplayName("Deve remover cliente com código de acesso correto")
    void deveRemoverCliente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act
        clienteService.remover(1L, "123456");

        // Assert
        verify(clienteRepository, times(1)).delete(cliente);
    }

    @Test
    @DisplayName("Deve falhar ao remover cliente com código incorreto")
    void deveFalharRemoverCodigoIncorreto() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act & Assert
        assertThrows(CodigoDeAcessoInvalidoException.class, () -> 
            clienteService.remover(1L, "999999")
        );
        verify(clienteRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve recuperar cliente pelo ID")
    void deveRecuperarCliente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // Act
        ClienteResponseDTO resultado = clienteService.recuperar(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(cliente.getNome(), resultado.getNome());
    }

    @Test
    @DisplayName("Deve falhar ao recuperar cliente inexistente")
    void deveFalharRecuperarInexistente() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoExisteException.class, () -> 
            clienteService.recuperar(1L)
        );
    }

    @Test
    @DisplayName("Deve listar todos os clientes")
    void deveListarClientes() {
        // Arrange
        when(clienteRepository.findAll()).thenReturn(List.of(cliente));

        // Act
        List<ClienteResponseDTO> resultado = clienteService.listar();

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Deve listar clientes por nome")
    void deveListarClientesPorNome() {
        // Arrange
        when(clienteRepository.findByNomeContaining("João")).thenReturn(List.of(cliente));

        // Act
        List<ClienteResponseDTO> resultado = clienteService.listarPorNome("João");

        // Assert
        assertFalse(resultado.isEmpty());
        assertEquals("João da Silva", resultado.get(0).getNome());
    }
}