package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.StatusDisponibilidade;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import com.ufcg.psoft.commerce.repository.HistoricoDisponibilidadeRepository;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
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

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do Controller - Disponibilidade do Técnico")
public class DisponibilidadeTecnicoControllerTests {

    final String URI_TECNICOS = "/tecnicos";

    @Autowired
    MockMvc driver;

    @Autowired
    TecnicoRepository tecnicoRepository;

    @Autowired
    HistoricoDisponibilidadeRepository historicoDisponibilidadeRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    Tecnico tecnico;

    private static final String ACESSO_TECNICO = "123456";
    private static final String ACESSO_INVALIDO = "000000";

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        historicoDisponibilidadeRepository.deleteAll();
        tecnicoRepository.deleteAll();

        tecnico = tecnicoRepository.save(Tecnico.builder()
                .nome("Tecnico Disponibilidade")
                .especialidade("hidraulica")
                .placaVeiculo("XYZ9A12")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .corVeiculo("branco")
                .acesso(ACESSO_TECNICO)
                .build());
    }

    @AfterEach
    void tearDown() {
        historicoDisponibilidadeRepository.deleteAll();
        tecnicoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Alteração de disponibilidade via endpoint PATCH")
    class AlteracaoDisponibilidadeEndpointTests {

        @Test
        @DisplayName("Técnico altera disponibilidade para ATIVO com acesso correto e recebe 200")
        void quandoTecnicoAlteraDisponibilidadeParaAtivoComAcessoCorretoRetorna200() throws Exception {
            String responseJson = driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.ATIVO, atualizado.getStatusDisponibilidade());
            assertFalse(responseJson.contains("acesso"));
        }

        @Test
        @DisplayName("Técnico altera disponibilidade para DESCANSO com acesso correto e recebe 200")
        void quandoTecnicoAlteraDisponibilidadeParaDescansoComAcessoCorretoRetorna200() throws Exception {
            // primeiro ativa
            driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // depois coloca em descanso
            driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.DESCANSO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());

            Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.DESCANSO, atualizado.getStatusDisponibilidade());
        }

        @Test
        @DisplayName("Resposta do endpoint não expõe o código de acesso do técnico")
        void quandoDisponibilidadeEhAlteradaRespostaNaoExpoeCodioDeAcesso() throws Exception {
            String responseJson = driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            assertFalse(responseJson.contains("acesso"));
        }

        @Test
        @DisplayName("Resposta contém o novo status atualizado")
        void quandoDisponibilidadeEhAlteradaRespostaContemNovoStatus() throws Exception {
            String responseJson = driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            assertTrue(responseJson.contains("ATIVO"));
        }
    }

    @Nested
    @DisplayName("Rejeição por acesso inválido")
    class RejeicaoAcessoInvalidoTests {

        @Test
        @DisplayName("Código de acesso errado retorna 400 e não altera o status")
        void quandoAcessoEhInvalidoRetorna400EStatusNaoEhAlterado() throws Exception {
            String responseJson = driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_INVALIDO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            CustomErrorType erro = objectMapper.readValue(responseJson, CustomErrorType.class);
            assertNotNull(erro.getMessage());

            Tecnico semMudanca = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.DESCANSO, semMudanca.getStatusDisponibilidade());
        }

        @Test
        @DisplayName("Técnico inexistente retorna 404")
        void quandoTecnicoNaoExisteRetorna404() throws Exception {
            driver.perform(patch(URI_TECNICOS + "/99999/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("Histórico de disponibilidade via endpoint")
    class HistoricoDisponibilidadeEndpointTests {

        @Test
        @DisplayName("Após alteração via endpoint uma entrada é persistida no histórico")
        void quandoDisponibilidadeEhAlteradaViaEndpointUmaEntradaEhPersistidaNoHistorico() throws Exception {
            driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            var historico = historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId());
            assertEquals(1, historico.size());
            assertEquals(StatusDisponibilidade.ATIVO, historico.get(0).getNovoStatus());
        }

        @Test
        @DisplayName("Três alterações via endpoint resultam em três entradas no histórico")
        void quandoTresAlteracoesViaEndpointTresEntradasSaoPersistidasNoHistorico() throws Exception {
            driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.DESCANSO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_TECNICO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            var historico = historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId());
            assertEquals(3, historico.size());
        }

        @Test
        @DisplayName("Falha na alteração com acesso inválido não gera entrada no histórico")
        void quandoAcessoEhInvalidoNenhumaEntradaEhSalvaNoHistorico() throws Exception {
            driver.perform(patch(URI_TECNICOS + "/" + tecnico.getId() + "/disponibilidade")
                            .param("acesso", ACESSO_INVALIDO)
                            .param("status", StatusDisponibilidade.ATIVO.name())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            var historico = historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId());
            assertTrue(historico.isEmpty());
        }
    }
}
