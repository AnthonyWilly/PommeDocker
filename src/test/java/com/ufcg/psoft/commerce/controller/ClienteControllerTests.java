package com.ufcg.psoft.commerce.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import com.ufcg.psoft.commerce.model.Plano;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ClientePostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.HistoricoPlanoRepository;
import com.ufcg.psoft.commerce.service.cliente.ClienteService;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Clientes")
public class ClienteControllerTests {

    final String URI_CLIENTES = "/clientes";
    final String URI_PLANO = "/plano";
    @Autowired
    MockMvc driver;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    ClienteService clienteService;

    @Autowired
    HistoricoPlanoRepository historicoRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    Cliente cliente;

    ClientePostPutRequestDTO clientePostPutRequestDTO;

    @BeforeEach
    void setup() {

        objectMapper.registerModule(new JavaTimeModule());
        cliente = clienteRepository.save(Cliente.builder()
                .nome("Cliente Um da Silva")
                .endereco("Rua dos Testes, 123")
                .codigo("123456")
                .planoAtual(Plano.BASICO)
                .proxPlano(null)
                .dataCobranca(LocalDate.now().plusDays(30))
                .build()
        );
        clientePostPutRequestDTO = ClientePostPutRequestDTO.builder()
                .nome(cliente.getNome())
                .endereco(cliente.getEndereco())
                .codigo(cliente.getCodigo())
                .build();

    }

    @AfterEach
    void tearDown() {
        clienteRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação de nome")
    class ClienteVerificacaoNome {

        @Test
        @DisplayName("Quando recuperamos um cliente com dados válidos")
        void quandoRecuperamosNomeDoClienteValido() throws Exception {
            String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + cliente.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Cliente resultado = objectMapper.readValue(responseJsonString, Cliente.ClienteBuilder.class).build();

            assertEquals("Cliente Um da Silva", resultado.getNome());
        }

        @Test
        @DisplayName("Quando alteramos o nome do cliente com dados válidos")
        void quandoAlteramosNomeDoClienteValido() throws Exception {
            clientePostPutRequestDTO.setNome("Cliente Um Alterado");

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Cliente resultado = objectMapper.readValue(responseJsonString, Cliente.ClienteBuilder.class).build();

            assertEquals("Cliente Um Alterado", resultado.getNome());
        }

        @Test
        @DisplayName("Quando alteramos o nome do cliente nulo")
        void quandoAlteramosNomeDoClienteNulo() throws Exception {
            clientePostPutRequestDTO.setNome(null);

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Nome obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o nome do cliente vazio")
        void quandoAlteramosNomeDoClienteVazio() throws Exception {
            clientePostPutRequestDTO.setNome("");

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Nome obrigatorio", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação do endereço")
    class ClienteVerificacaoEndereco {

        @Test
        @DisplayName("Quando alteramos o endereço do cliente com dados válidos")
        void quandoAlteramosEnderecoDoClienteValido() throws Exception {
            clientePostPutRequestDTO.setEndereco("Endereco Alterado");

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ClienteResponseDTO resultado = objectMapper.readValue(responseJsonString, ClienteResponseDTO.ClienteResponseDTOBuilder.class).build();

            assertEquals("Endereco Alterado", resultado.getEndereco());
        }

        @Test
        @DisplayName("Quando alteramos o endereço do cliente nulo")
        void quandoAlteramosEnderecoDoClienteNulo() throws Exception {
            clientePostPutRequestDTO.setEndereco(null);

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Endereco obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o endereço do cliente vazio")
        void quandoAlteramosEnderecoDoClienteVazio() throws Exception {
            clientePostPutRequestDTO.setEndereco("");

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Endereco obrigatorio", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação do código de acesso")
    class ClienteVerificacaoCodigoAcesso {

        @Test
        @DisplayName("Quando alteramos o código de acesso do cliente nulo")
        void quandoAlteramosCodigoAcessoDoClienteNulo() throws Exception {
            clientePostPutRequestDTO.setCodigo(null);

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do cliente mais de 6 digitos")
        void quandoAlteramosCodigoAcessoDoClienteMaisDe6Digitos() throws Exception {
            clientePostPutRequestDTO.setCodigo("1234567");

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do cliente menos de 6 digitos")
        void quandoAlteramosCodigoAcessoDoClienteMenosDe6Digitos() throws Exception {
            clientePostPutRequestDTO.setCodigo("12345");

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do cliente caracteres não numéricos")
        void quandoAlteramosCodigoAcessoDoClienteCaracteresNaoNumericos() throws Exception {
            clientePostPutRequestDTO.setCodigo("a*c4e@");

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação dos fluxos básicos API Rest")
    class ClienteVerificacaoFluxosBasicosApiRest {

        @Test
        @DisplayName("Quando buscamos por todos clientes salvos")
        void quandoBuscamosPorTodosClienteSalvos() throws Exception {
            Cliente cliente1 = Cliente.builder()
                    .nome("Cliente Dois Almeida")
                    .endereco("Av. da Pits A, 100")
                    .codigo("246810")
                    .planoAtual(Plano.BASICO)
                    .proxPlano(null)
                    .dataCobranca(LocalDate.now().plusDays(30))
                    .build();
            Cliente cliente2 = Cliente.builder()
                    .nome("Cliente Três Lima")
                    .endereco("Distrito dos Testadores, 200")
                    .codigo("135790")
                    .planoAtual(Plano.PREMIUM)
                    .proxPlano(null)
                    .dataCobranca(LocalDate.now().plusDays(30))
                    .build();
            clienteRepository.saveAll(Arrays.asList(cliente1, cliente2));

            String responseJsonString = driver.perform(get(URI_CLIENTES)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<Cliente> resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {
            });

            assertAll(
                    () -> assertEquals(3, resultado.size())
            );
        }

        @Test
        @DisplayName("Quando buscamos um cliente salvo pelo id")
        void quandoBuscamosPorUmClienteSalvo() throws Exception {
            String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ClienteResponseDTO resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {});

            assertAll(
                    () -> assertEquals(cliente.getId().longValue(), resultado.getId().longValue()),
                    () -> assertEquals(cliente.getNome(), resultado.getNome())
            );
        }

        @Test
        @DisplayName("Quando buscamos um cliente inexistente")
        void quandoBuscamosPorUmClienteInexistente() throws Exception {
            String responseJsonString = driver.perform(get(URI_CLIENTES + "/" + 999999999)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O cliente consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando criamos um novo cliente com dados válidos")
        void quandoCriarClienteValido() throws Exception {
            String responseJsonString = driver.perform(post(URI_CLIENTES)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Cliente resultado = objectMapper.readValue(responseJsonString, Cliente.ClienteBuilder.class).build();

            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(clientePostPutRequestDTO.getNome(), resultado.getNome()),
                    () -> assertEquals(Plano.BASICO, resultado.getPlanoAtual())
            );

        }

        @Test
        @DisplayName("Quando alteramos o cliente com dados válidos")
        void quandoAlteramosClienteValido() throws Exception {
            Long clienteId = cliente.getId();

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Cliente resultado = objectMapper.readValue(responseJsonString, Cliente.ClienteBuilder.class).build();

            assertAll(
                    () -> assertEquals(resultado.getId().longValue(), clienteId),
                    () -> assertEquals(clientePostPutRequestDTO.getNome(), resultado.getNome())
            );
        }

        @Test
        @DisplayName("Quando alteramos o cliente inexistente")
        void quandoAlteramosClienteInexistente() throws Exception {
            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O cliente consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando alteramos o cliente passando código de acesso inválido")
        void quandoAlteramosClienteCodigoAcessoInvalido() throws Exception {
            Long clienteId = cliente.getId();

            String responseJsonString = driver.perform(put(URI_CLIENTES + "/" + clienteId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", "invalido")
                            .content(objectMapper.writeValueAsString(clientePostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando excluímos um cliente salvo")
        void quandoExcluimosClienteValido() throws Exception {
            String responseJsonString = driver.perform(delete(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo()))
                    .andExpect(status().isNoContent())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            assertTrue(responseJsonString.isBlank());
        }

        @Test
        @DisplayName("Quando excluímos um cliente inexistente")
        void quandoExcluimosClienteInexistente() throws Exception {
            String responseJsonString = driver.perform(delete(URI_CLIENTES + "/" + 999999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo()))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("O cliente consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando excluímos um cliente salvo passando código de acesso inválido")
        void quandoExcluimosClienteCodigoAcessoInvalido() throws Exception {
            String responseJsonString = driver.perform(delete(URI_CLIENTES + "/" + cliente.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", "invalido"))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            assertAll(
                    () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage())
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação de Planos")
    class ClienteVerificacaoPlanos {

        @Test
        @DisplayName("Deve alterar o plano para Premium com dados Validos")
        void quandoAlteramosPlanoParaPremium() throws Exception {
            String uri = URI_CLIENTES + "/" + cliente.getId() + URI_PLANO;
            String responseJsonString = driver.perform(patch(uri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .param("tipoPlano", "Premium"))
                    .andExpect(status().isOk()) //
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
            ClienteResponseDTO resultado = objectMapper.readValue(responseJsonString, ClienteResponseDTO.class);
            assertEquals(Plano.BASICO, resultado.getPlanoAtual());
            assertEquals(Plano.PREMIUM, resultado.getProxPlano());
        }

        @Test
        @DisplayName("Deve alterar o plano para Basico com dados Validos")
        void quandoAlteramosPlanoParaBasico() throws Exception {
            cliente.setPlanoAtual(Plano.PREMIUM);
            cliente.setProxPlano(null);
            cliente = clienteRepository.save(cliente);
            String uri = URI_CLIENTES + "/" + cliente.getId() + URI_PLANO;
            String responseJsonString = driver.perform(patch(uri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", cliente.getCodigo())
                            .param("tipoPlano", "Basico"))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ClienteResponseDTO resultado = objectMapper.readValue(responseJsonString, ClienteResponseDTO.class);
            assertEquals(Plano.BASICO, resultado.getProxPlano());
            assertEquals(Plano.PREMIUM, resultado.getPlanoAtual());
        }
        
        @Test
        @DisplayName("Deve falhar ao alterar plano de cliente inexistente")
        void quandoAlteramosPlanoClienteInexistente() throws Exception {
            Long idInexistente = 999999L;
            String uri = URI_CLIENTES + "/" + idInexistente + URI_PLANO;
            String responseJsonString = driver.perform(patch(uri)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("codigo", "123456")
                            .param("tipoPlano", "Premium"))
                    .andExpect(status().isBadRequest()) 
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();
            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);
            assertEquals("O cliente consultado nao existe!", resultado.getMessage());
        }
    }
  
    @Test
    @DisplayName("Deve falhar ao alterar para Premium com codigo de acesso invalido")
    void quandoAlteramosParaPremiumCodigoInvalido() throws Exception {
        // Arrange
        String codigoInvalido = "000000";
        String uri = URI_CLIENTES + "/" + cliente.getId() + URI_PLANO;

        // Act
        String responseJsonString = driver.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("codigo", codigoInvalido)
                        .param("tipoPlano", "Premium"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        // Assert
        assertEquals("Codigo de acesso invalido!", resultado.getMessage());
    }

    @Test
    @DisplayName("Deve falhar ao alterar para Basico com codigo de acesso invalido")
    void quandoAlteramosParaBasicoCodigoInvalido() throws Exception {
        // Arrange
        String codigoInvalido = "000000";
        String uri = URI_CLIENTES + "/" + cliente.getId() + URI_PLANO;

        // Act
        String responseJsonString = driver.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("codigo", codigoInvalido)
                        .param("tipoPlano", "Basico"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        // Assert
        assertEquals("Codigo de acesso invalido!", resultado.getMessage());
    }

    @Test
    @DisplayName("Deve falhar ao alterar para Basico de cliente inexistente")
    void quandoAlteramosPlanoBasicoClienteInexistente() throws Exception {
        // Arrange
        Long idInexistente = 999999L;
        String uri = URI_CLIENTES + "/" + idInexistente + URI_PLANO;

        // Act
        String responseJsonString = driver.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("codigo", "123456")
                        .param("tipoPlano", "Basico"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        // Assert
        assertEquals("O cliente consultado nao existe!", resultado.getMessage());
    }

    @Test
    @DisplayName("Deve falhar ao passar um tipo de plano que nao existe")
    void quandoPassamosTipoPlanoInvalido() throws Exception {
        // Arrange
        String planoInexistente = "PlanoNaoExiste";
        String uri = URI_CLIENTES + "/" + cliente.getId() + URI_PLANO;

        // Act
        String responseJsonString = driver.perform(patch(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("codigo", cliente.getCodigo())
                        .param("tipoPlano", planoInexistente))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertEquals("Plano invalido", responseJsonString);
    }
}
