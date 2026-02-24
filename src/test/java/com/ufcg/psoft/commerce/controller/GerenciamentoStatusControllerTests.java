package com.ufcg.psoft.commerce.controller;

import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do Controlador de Gerenciamento de Status de Chamados ")
public class GerenciamentoStatusControllerTests {

    final String URI_EMPRESAS = "/empresas";
    final String CODIGO_ACESSO_PADRAO = "123456";

    @Autowired
    MockMvc driver;
    @Autowired
    EmpresaRepository empresaRepository;
    @Autowired
    ChamadoRepository chamadoRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    ServicoRepository servicoRepository;
    @Autowired
    ObjectMapper objectMapper;

    Empresa empresaPadrao;
    Cliente clientePadrao;
    Servico servicoPadrao;
    Chamado chamado;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        empresaPadrao = empresaRepository.save(Empresa.builder()
                .nome("Service Corp")
                .codigoAcesso(CODIGO_ACESSO_PADRAO)
                .cnpj("12.345.678/0001-90")
                .build());

        clientePadrao = clienteRepository.save(Cliente.builder()
                .nome("Teste Basico")
                .codigo("123456")
                .endereco("Rua Base, 100")
                .planoAtual(Plano.BASICO)
                .dataCobranca(LocalDate.now())
                .build());

        servicoPadrao = servicoRepository.save(Servico.builder()
                .nome("Manutenção Simples")
                .descricao("Reparo básico")
                .urgencia(Urgencia.NORMAL)
                .duracao(30.0)
                .preco(100.0)
                .empresa(empresaPadrao)
                .plano(Plano.BASICO)
                .disponivel(true)
                .tipo(TipoServico.ELETRICA)
                .build());

        chamado = Chamado.builder()
                .empresa(empresaPadrao)
                .cliente(clientePadrao)
                .servico(servicoPadrao)
                .enderecoAtendimento("Rua Base, 100")
                .dataCriacao(LocalDateTime.now())
                .status("Chamado recebido") 
                .build();
        chamado = chamadoRepository.save(chamado);
    }

    @AfterEach
    void tearDown() {
        chamadoRepository.deleteAll();
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Testes de alteração de status (Avançar Status)")
    class AlteracaoStatusTests {

        @Test
        @DisplayName("Empresa avança status do chamado com sucesso")
        void avancarStatusComSucesso() throws Exception {
            String responseJson = driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                            .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            ChamadoResponseDTO resultado = objectMapper.readValue(responseJson, ChamadoResponseDTO.class);

            assertEquals("Chamado recebido", resultado.getStatus());
            
            Chamado chamadoAtualizado = chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals("Chamado recebido", chamadoAtualizado.getStatus());
        }

        @Test
        @DisplayName("Falhar ao tentar avançar status com código de acesso inválido")
        void avancarStatusCodigoAcessoInvalido() throws Exception {
            String responseJson = driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                            .header("codigoAcesso", "000000")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof CodigoDeAcessoInvalidoException))
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJson, CustomErrorType.class);
            assertEquals("Codigo de acesso invalido!", resultado.getMessage());
        }

        @Test
        @DisplayName("Falhar ao tentar avançar status de um chamado inexistente")
        void avancarStatusChamadoInexistente() throws Exception {
            driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/99999/avancar-status")
                            .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }
    }
}