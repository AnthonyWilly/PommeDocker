package com.ufcg.psoft.commerce.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Interesses de clientes em serviços indisponíveis")
public class InteresseControllerTests {

    final String URI_SERVICO = "/servico";
    final String URI_INTERESSE = "/interesses";

    @Autowired
    MockMvc driver;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    ServicoRepository servicoRepository;

    @Autowired
    InteresseRepository interesseRepository;

    Cliente clienteBasico;
    Cliente clientePremium;
    Empresa empresa;
    Servico servicoDisponivelBasico;
    Servico servicoDisponivelPremium;
    Servico servicoIndiponivelBasico;
    Servico servicoIndiponivelPremium;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        empresa = empresaRepository.save(Empresa.builder()
                    .nome("Empresa Exemplo")
                    .cnpj("12.345.678/0001-90")
                    .codigoAcesso("123456")
                    .build()
        );

        clienteBasico = clienteRepository.save(Cliente.builder()
                .nome("Cliente Um da Silva")
                .endereco("Rua dos Testes, 123")
                .codigo("123456")
                .planoAtual(Plano.BASICO)
                .proxPlano(null)
                .dataCobranca(LocalDate.now().plusDays(30))
                .build()
        );

        clientePremium = clienteRepository.save(Cliente.builder()
                .nome("Cliente Dois da Silva")
                .endereco("Rua dos Testes ao quadrado, 321")
                .codigo("654321")
                .planoAtual(Plano.PREMIUM)
                .proxPlano(null)
                .dataCobranca(LocalDate.now().plusDays(30))
                .build()
        );

        servicoDisponivelBasico = servicoRepository.save(Servico.builder()
                .nome("Pintura")
                .tipo(TipoServico.PINTURA)
                .urgencia(Urgencia.NORMAL)
                .descricao("Pintar determinada area")
                .preco(100.0)
                .duracao(3.0)
                .disponivel(true)
                .empresa(empresa)
                .plano(Plano.BASICO)
                .build()
        );

        servicoDisponivelPremium = servicoRepository.save(Servico.builder()
                .nome("Pintura premium")
                .tipo(TipoServico.PINTURA)
                .urgencia(Urgencia.URGENTE)
                .descricao("Pintar determinada area no alto padrão")
                .preco(500.0)
                .duracao(12.0)
                .disponivel(true)
                .empresa(empresa)
                .plano(Plano.PREMIUM)
                .build()
        );

        servicoIndiponivelBasico = servicoRepository.save(Servico.builder()
                .nome("Reparo Hidraulico")
                .tipo(TipoServico.HIDRAULICA)
                .urgencia(Urgencia.BAIXA)
                .descricao("Reparo Hidraulico completo")
                .preco(100.0)
                .duracao(1.0)
                .disponivel(false)
                .plano(Plano.BASICO) 
                .empresa(empresa)
                .build());

        servicoIndiponivelPremium = servicoRepository.save(Servico.builder()
                .nome("Reparo Hidraulico de toda a casa")
                .tipo(TipoServico.HIDRAULICA)
                .urgencia(Urgencia.ALTA)
                .descricao("Reparo Hidraulico completo de toda a sua casa")
                .preco(1000.0)
                .duracao(10.0)
                .disponivel(false)
                .plano(Plano.PREMIUM) 
                .empresa(empresa)
                .build());

    }

    @AfterEach
    void tearDown() {
        interesseRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Testes de demonstrar interesse em serviço")
    class DemonstrarIntesseTestes {

        @Nested
        @DisplayName("Testes de demonstração de interesse com sucesso")
        class demonstrarInteresseSucesso {

                @Test
                @DisplayName("Deve demonstrar interesse com sucesso quando o serviço estiver indisponível de servico e cliente basico")
                void demonstrarIntesseComSucessoComServicoEClienteBasico() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoIndiponivelBasico.getId() + "/interesses")
                                        .param("clienteId", clienteBasico.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        // Assert
                        assertAll(
                                () -> assertEquals(1, interesseRepository.findAll().size()),
                                () -> assertTrue(response.contains("Reparo Hidraulico"))
                        );
                        
                }

                @Test
                @DisplayName("Deve demonstrar interesse com sucesso quando o serviço estiver indisponível de servico e cliente premium")
                void demonstrarIntesseComSucessoComServicoEClientePremium() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoIndiponivelPremium.getId() + "/interesses")
                                        .param("clienteId", clientePremium.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        // Assert
                        assertAll(
                                () -> assertEquals(1, interesseRepository.findAll().size()),
                                () -> assertTrue(response.contains("Reparo Hidraulico de toda a casa"))
                        );
                        
                }
                
                @Test
                @DisplayName("Deve demonstrar interesse com sucesso quando o serviço estiver indisponível de serviço básico e cliente premium")
                void demonstrarIntesseComSucessoComPlanoEClientePremium() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoIndiponivelBasico.getId() + "/interesses")
                                        .param("clienteId", clientePremium.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        // Assert
                        assertAll(
                                () -> assertEquals(1, interesseRepository.findAll().size()),
                                () -> assertTrue(response.contains("Reparo Hidraulico"))
                        );
                        
                }
        }
        
        @Nested
        @DisplayName("Testes de demonstração de interesse com sucesso")
        class demonstrarInteresseFalha{

                @Test
                @DisplayName("Deve retornar erro quando cliente Basico tenta demonstrar interesse em serviço premium indisponível")
                void demonstrarIntesseClienteBasicoParaServicoPremium() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoIndiponivelPremium.getId() + "/interesses")
                                        .param("clienteId", clienteBasico.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        CustomErrorType resultado = objectMapper.readValue(response, CustomErrorType.class);

                        // Assert
                        assertAll(
                                () -> assertEquals(0, interesseRepository.findAll().size()),
                                () -> assertEquals("Não é possível demonstrar interesse à um serviço premium com seu plano atual.", resultado.getMessage())
                        );
                        
                }

                @Test
                @DisplayName("Deve retornar erro se tentar demonstrar interesse em um serviço disponível onde cliente e servico são basicos")
                void demonstrarIntesseEmServicoDisponivelBasicoEClienteBasico() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoDisponivelBasico.getId() + "/interesses")
                                        .param("clienteId", clienteBasico.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        CustomErrorType resultado = objectMapper.readValue(response, CustomErrorType.class);

                        // Assert
                        assertAll(
                                () -> assertEquals(0, interesseRepository.findAll().size()),
                                () -> assertEquals("Não é possível demonstrar interesse em serviço disponível.", resultado.getMessage())
                        );
                        
                }

                @Test
                @DisplayName("Deve retornar erro se tentar demonstrar interesse em um serviço disponível onde cliente e serviço são premium")
                void demonstrarIntesseEmServicoDisponivelPremiumEClientePremium() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoDisponivelPremium.getId() + "/interesses")
                                        .param("clienteId", clientePremium.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        CustomErrorType resultado = objectMapper.readValue(response, CustomErrorType.class);

                        // Assert
                        assertAll(
                                () -> assertEquals(0, interesseRepository.findAll().size()),
                                () -> assertEquals("Não é possível demonstrar interesse em serviço disponível.", resultado.getMessage())
                        );
                        
                }

                @Test
                @DisplayName("Deve retornar erro se tentar demonstrar interesse em um serviço disponível onde cliente é premium e servico é basico")
                void demonstrarIntesseEmServicoDisponivelBasicoEClientePremium() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoDisponivelBasico.getId() + "/interesses")
                                        .param("clienteId", clientePremium.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        CustomErrorType resultado = objectMapper.readValue(response, CustomErrorType.class);

                        // Assert
                        assertAll(
                                () -> assertEquals(0, interesseRepository.findAll().size()),
                                () -> assertEquals("Não é possível demonstrar interesse em serviço disponível.", resultado.getMessage())
                        );
                        
                }

                @Test
                @DisplayName("Deve retornar erro se tentar demonstrar interesse em um serviço disponível onde cliente é basico e servico é premium")
                void demonstrarIntesseEmServicoDisponivelPremiumEClienteBasico() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoDisponivelPremium.getId() + "/interesses")
                                        .param("clienteId", clienteBasico.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest()) 
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        CustomErrorType resultado = objectMapper.readValue(response, CustomErrorType.class);

                        // Assert
                        assertAll(
                                () -> assertEquals(0, interesseRepository.findAll().size()),
                                () -> assertEquals("Não é possível demonstrar interesse em serviço disponível.", resultado.getMessage())
                        );
                        
                }

                @Test
                @DisplayName("Deve retornar erro se o cliente não existir")
                void demonstrarInteresseClienteInexistente() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/" + servicoIndiponivelBasico.getId() + URI_INTERESSE)
                                        .param("clienteId", "9999")
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        CustomErrorType resultado = objectMapper.readValue(response, CustomErrorType.class);
                        
                        // Assert
                        assertAll(
                                () -> assertEquals("O cliente não existe!", resultado.getMessage())
                        );
                }

                @Test
                @DisplayName("Deve retornar erro se o serviço não existir")
                void demonstrarInteresseServicoInexistente() throws Exception {

                        // Act
                        String response = driver.perform(
                                post(URI_SERVICO + "/9999" + URI_INTERESSE)
                                        .param("clienteId", clienteBasico.getId().toString())
                                        .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andDo(print())
                                .andReturn().getResponse().getContentAsString();

                        CustomErrorType resultado = objectMapper.readValue(response, CustomErrorType.class);
                        
                        // Assert
                        assertAll(
                                () -> assertEquals("O serviço não existe!", resultado.getMessage())
                        );
                }
        }
    }
}