package com.ufcg.psoft.commerce.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

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
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de serviços")
public class ServicoControllerTests {

    final String URI_SERVICOS = "/servicos";
    final String URI_CATALOGO = "/catalogo";

    @Autowired
    MockMvc driver;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ServicoRepository servicoRepository;

    @Autowired
    ClienteRepository clienteRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    Cliente clienteBasico;
    Cliente clientePremium;
    Empresa empresa;
    Servico servicoBasico;
    Servico servicoPremium;

    @BeforeEach
    void setup() {
        empresa = empresaRepository.save(Empresa.builder()
                .nome("Empresa de Teste")
                .codigoAcesso("123456")
                .build());

        clienteBasico = clienteRepository.save(Cliente.builder()
                .nome("Matheus Basico")
                .planoAtual("Basico")
                .codigo("111111")
                .build());

        clientePremium = clienteRepository.save(Cliente.builder()
                .nome("Matheus Premium")
                .planoAtual("Premium")
                .codigo("222222")
                .build());

        servicoBasico = servicoRepository.save(Servico.builder()
                .nome("Reparo Hidraulico")
                .tipo("Hidraulica")
                .precoBase(100.0)
                .disponibilidadePlano("Basico") 
                .empresa(empresa)
                .build());

        servicoPremium = servicoRepository.save(Servico.builder()
                .nome("Guinchar carro")
                .tipo("Emergencia")
                .precoBase(500.0)
                .disponibilidadePlano("Premium")
                .empresa(empresa)
                .build());
    }

    @AfterEach
    void tearDown() {
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de testes de verificação de catálogo de serviços por plano")
    class catalogoDeServicosPorPlano {
    
        @Test
        @DisplayName("Deve listar apenas serviços do plano básico para cliente com plano básico")
        void quandoClienteBasicoAcessaCatalogo() throws Exception {

            String responseJsoString = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                            .param("clienteId", clienteBasico.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<Servico> resultados = objectMapper.readValue(responseJsoString, new TypeReference<List<Servico>>() {});

            assertAll(
                () -> assertEquals(1, resultados.size()),
                () -> assertEquals("Reparo Hidraulico", resultados.get(0).getNome())
            );

        }                    

        @Test
        @DisplayName("Deve listar apenas serviços do plano premium para cliente com plano premium")
        void quandoClientePremiumAcessaCatalogo() throws Exception {

            String responseJsoString = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                            .param("clienteId", clientePremium.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<Servico> resultados = objectMapper.readValue(responseJsoString, new TypeReference<List<Servico>>() {});

            assertAll(
                () -> assertEquals(2, resultados.size()),
                () -> assertEquals("Reparo Hidraulico", resultados.get(0).getNome()),
                () -> assertEquals("Guinchar carro", resultados.get(1).getNome())
            );

        }                    
        
    }

    @Nested
    @DisplayName("Conjunto de casos de teste de verificação de filtros")
    class catalogoDeServicosPorFiltro {

        @Test
        @DisplayName("Quando filtramos por tipo de serviço")
        void quandoFiltramosPorTipo() throws Exception {
            String responJsoString = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                        .param("clienteId", clientePremium.getId().toString())
                        .param("tipo", "Hidraulica")
                        .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Servico> resultado = objectMapper.readValue(responJsoString, new TypeReference<List<Servico>>() {});

            assertTrue(resultado.stream().allMatch(s -> s.getTipo().equals("Hidraulica")));
        }

        @Test
        @DisplayName("Quando filtramos por faixa de preço")
        void quandoFiltramosPorPreco() throws Exception {
            
            String responseJsonString = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                            .param("clienteId", clientePremium.getId().toString())
                            .param("precoMax", "200.0")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Servico> resultado = objectMapper.readValue(responseJsonString, new TypeReference<List<Servico>>() {});

            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertTrue(resultado.get(0).getPrecoBase() <= 200.0)
            );
            
        }

        @Test
        @DisplayName("Quando filtramos por empresa")
        void quandoFiltramosPorEmpresa() throws Exception {

            String responseJsonString = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                            .param("clienteId", clientePremium.getId().toString())
                            .param("empresaId", empresa.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Servico> resultado = objectMapper.readValue(responseJsonString, new TypeReference<List<Servico>>() {}); 

            assertAll(
                () -> assertEquals(2, resultado.size())
            );

        }

    }
}
