package com.ufcg.psoft.commerce.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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
    TecnicoRepository tecnicoRepository;

    @Autowired
    ObjectMapper objectMapper;

    Empresa empresaPadrao;
    Cliente clientePadrao;
    Servico servicoPadrao;
    Chamado chamado;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        empresaPadrao = empresaRepository.save(
            Empresa.builder()
                .nome("Service Corp")
                .codigoAcesso(CODIGO_ACESSO_PADRAO)
                .cnpj("12.345.678/0001-90")
                .build()
        );

        clientePadrao = clienteRepository.save(
            Cliente.builder()
                .nome("Teste Basico")
                .codigo("123456")
                .endereco("Rua Base, 100")
                .planoAtual(Plano.BASICO)
                .dataCobranca(LocalDate.now())
                .build()
        );

        servicoPadrao = servicoRepository.save(
            Servico.builder()
                .nome("Manutenção Simples")
                .descricao("Reparo básico")
                .urgencia(Urgencia.NORMAL)
                .duracao(30.0)
                .preco(100.0)
                .empresa(empresaPadrao)
                .plano(Plano.BASICO)
                .disponivel(true)
                .tipo(TipoServico.ELETRICA)
                .build()
        );

        chamado = Chamado.builder()
            .empresa(empresaPadrao)
            .cliente(clientePadrao)
            .servico(servicoPadrao)
            .enderecoAtendimento("Rua Base, 100")
            .dataCriacao(LocalDateTime.now())
            .status("CHAMADO_RECEBIDO")
            .build();
        chamado = chamadoRepository.save(chamado);
    }

    @AfterEach
    void tearDown() {
        chamadoRepository.deleteAll();
        servicoRepository.deleteAll();
        tecnicoRepository.deleteAll();
        clienteRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Testes de alteração de status (Avançar Status)")
    class AlteracaoStatusTests {

        @Test
        @DisplayName("Empresa avança status do chamado com sucesso")
        void avancarStatusComSucesso() throws Exception {
            String responseJson = driver
                .perform(
                    put(
                        URI_EMPRESAS +
                            "/" +
                            empresaPadrao.getId() +
                            "/chamados/" +
                            chamado.getId() +
                            "/avancar-status"
                    )
                        .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

            ChamadoResponseDTO resultado = objectMapper.readValue(
                responseJson,
                ChamadoResponseDTO.class
            );

            assertEquals("EM_ANALISE", resultado.getStatus());

            Chamado chamadoAtualizado = chamadoRepository
                .findById(chamado.getId())
                .orElseThrow();
            assertEquals("EM_ANALISE", chamadoAtualizado.getStatus());
        }

        @Test
        @DisplayName(
            "Endpoint atribui técnico e avança status para Em Atendimento"
        )
        void endpointAtribuirTecnico() throws Exception {
            chamado.setStatus("AGUARDANDO_TECNICO");
            chamadoRepository.save(chamado);

            Tecnico tecnico = Tecnico.builder()
                .nome("Carlos")
                .acesso("654321")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .placaVeiculo("ABC-9999")
                .corVeiculo("Preto")
                .especialidade("Geral")
                .statusDisponibilidade(
                    com.ufcg.psoft.commerce.model.StatusDisponibilidade.ATIVO
                )
                .build();
            tecnico.getEmpresasAprovadoras().add(empresaPadrao);
            tecnicoRepository.save(tecnico);

            String responseJson = driver
                .perform(
                    put(
                        URI_EMPRESAS +
                            "/" +
                            empresaPadrao.getId() +
                            "/chamados/" +
                            chamado.getId() +
                            "/tecnicos/" +
                            tecnico.getId()
                    )
                        .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

            ChamadoResponseDTO resultado = objectMapper.readValue(
                responseJson,
                ChamadoResponseDTO.class
            );

            assertEquals("EM_ATENDIMENTO", resultado.getStatus());
            assertEquals(
                "EM_ATENDIMENTO",
                chamadoRepository.findById(chamado.getId()).get().getStatus()
            );
        }

        @Test
        @DisplayName(
            "Falhar ao tentar avançar status com código de acesso inválido"
        )
        void avancarStatusCodigoAcessoInvalido() throws Exception {
            String responseJson = driver
                .perform(
                    put(
                        URI_EMPRESAS +
                            "/" +
                            empresaPadrao.getId() +
                            "/chamados/" +
                            chamado.getId() +
                            "/avancar-status"
                    )
                        .header("codigoAcesso", "000000")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                    assertTrue(
                        result.getResolvedException() instanceof
                            CodigoDeAcessoInvalidoException
                    )
                )
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(
                responseJson,
                CustomErrorType.class
            );
            assertEquals("Codigo de acesso invalido!", resultado.getMessage());
        }

        @Test
        @DisplayName(
            "Falhar ao tentar avançar status de um chamado inexistente"
        )
        void avancarStatusChamadoInexistente() throws Exception {
            driver
                .perform(
                    put(
                        URI_EMPRESAS +
                            "/" +
                            empresaPadrao.getId() +
                            "/chamados/99999/avancar-status"
                    )
                        .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(print());
        }
    }
}

    @Nested
    @DisplayName("Testes de notificação por falta de técnicos (US19)")
    class NotificacaoFaltaTecnicosTests {

        @BeforeEach
        void setUpEmAnalise() {
            chamado.setStatus("EM_ANALISE");
            chamadoRepository.save(chamado);
        }

        @ExtendWith(OutputCaptureExtension.class)
        @Test
        @DisplayName("Deve notificar cliente quando não há técnicos ativos ao entrar em AGUARDANDO_TECNICO")
        void deveNotificarClienteQuandoNaoHaTecnicosAtivos(CapturedOutput output) throws Exception {
            driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                            .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            Chamado atualizado = chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals("AGUARDANDO_TECNICO", atualizado.getStatus());
            assertTrue(output.getOut().contains(clientePadrao.getNome()));
        }

        @ExtendWith(OutputCaptureExtension.class)
        @Test
        @DisplayName("Não deve notificar cliente quando há técnicos ativos ao entrar em AGUARDANDO_TECNICO")
        void naoDeveNotificarClienteQuandoHaTecnicosAtivos(CapturedOutput output) throws Exception {
            tecnicoRepository.save(Tecnico.builder()
                    .nome("Técnico Ativo")
                    .especialidade("Geral")
                    .corVeiculo("Preto")
                    .tipoVeiculo(TipoVeiculo.CARRO)
                    .placaVeiculo("XYZ-9999")
                    .acesso("000000")
                    .statusTecnico(StatusTecnico.ATIVO)
                    .build());

            driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                            .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            Chamado atualizado = chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals("AGUARDANDO_TECNICO", atualizado.getStatus());
            assertFalse(output.getOut().contains("Não há técnicos ativos"));
        }
    }
}
