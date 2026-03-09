package com.ufcg.psoft.commerce.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.Chamado;
import com.ufcg.psoft.commerce.model.ChamadoStatus;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.repository.ChamadoRepository;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;

import jakarta.persistence.EntityManager;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do Controlador de Gerenciamento de Status de Chamados ")
public class GerenciamentoStatusControllerTests {

    final String URI_EMPRESAS = "/empresas";
    final String CODIGO_ACESSO_PADRAO = "123456";

    @Autowired
    MockMvc driver;
    @Autowired
    EntityManager entityManager;
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
    Tecnico tecnicoPadrao;
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

        tecnicoPadrao = Tecnico.builder()
                .nome("Carlos")
                .acesso("654321")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .placaVeiculo("ABC-9999")
                .corVeiculo("Preto")
                .especialidade("Geral")
                .build();

        chamado = Chamado.builder()
                .empresa(empresaPadrao)
                .cliente(clientePadrao)
                .servico(servicoPadrao)
                .enderecoAtendimento("Rua Base, 100")
                .dataCriacao(LocalDateTime.now())
                .status("CHAMADO_RECEBIDO")
                .build();

        tecnicoPadrao = tecnicoRepository.save(tecnicoPadrao);
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
            String responseJson = driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                            .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            ChamadoResponseDTO resultado = objectMapper.readValue(responseJson, ChamadoResponseDTO.class);

            assertEquals("EM_ANALISE", resultado.getStatus());
            
            Chamado chamadoAtualizado = chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals("EM_ANALISE", chamadoAtualizado.getStatus());
        }

        @Test
        @DisplayName("Endpoint atribui técnico e avança status para Em Atendimento")
        void endpointAtribuirTecnico() throws Exception {
            chamado.setStatus("AGUARDANDO_TECNICO");
            chamadoRepository.save(chamado);

            tecnicoPadrao.getEmpresasAprovadoras().add(empresaPadrao);
            tecnicoRepository.save(tecnicoPadrao);

            String responseJson = driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/tecnicos/" + tecnicoPadrao.getId())
                            .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

            ChamadoResponseDTO resultado = objectMapper.readValue(responseJson, ChamadoResponseDTO.class);

            assertEquals("EM_ATENDIMENTO", resultado.getStatus());
            assertEquals("EM_ATENDIMENTO", chamadoRepository.findById(chamado.getId()).get().getStatus());
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

    @Nested
    @DisplayName("Testes de atribuição automática de técnicos a chamadas")
    class AtribuicaoAutomaticaTecnicosChamadas {

        private Tecnico tecnicoAntigoDisponivel;
        private Tecnico tecnicoNovoDisponivel;

        @BeforeEach
        void setupTecnicos() {
            chamado.setStatus(ChamadoStatus.EM_ANALISE.getNome());
            chamadoRepository.save(chamado);

            tecnicoAntigoDisponivel = tecnicoRepository.save(Tecnico.builder()
                .nome("Tecnico Um da Silva")
                .acesso("654321")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .placaVeiculo("ABC-9999")
                .corVeiculo("Preto")
                .empresasAprovadoras(List.of(empresaPadrao))
                .disponivel(true)
                .dataUltimaMudancaDisponibilidade(LocalDateTime.now().minusHours(5))
                .especialidade("Geral")
                .build());

            tecnicoNovoDisponivel = tecnicoRepository.save(Tecnico.builder()
                .nome("Tecnico Dois da Silva")
                .acesso("123456")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .placaVeiculo("ABC-1111")
                .corVeiculo("Preto")
                .empresasAprovadoras(List.of(empresaPadrao))
                .disponivel(true)
                .dataUltimaMudancaDisponibilidade(LocalDateTime.now().minusHours(1))
                .especialidade("Geral")
                .build());
        }


        @Test
        @DisplayName("Atribui técnico à chamada imediatamente e prioriza o técnico disponível a mais tempo") 
        void priorizaTecnicoDisponivelAMaisTempo() throws Exception {

            // Act
            driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                            .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                            .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andDo(print());

            entityManager.flush();

            // Assert
            Chamado chamadaAtualizada = chamadoRepository.findById(chamado.getId()).get();
            Tecnico tecnicoAtualizado = tecnicoRepository.findById(tecnicoAntigoDisponivel.getId()).get();

            assertAll(
                    () -> assertEquals(ChamadoStatus.EM_ATENDIMENTO.getNome(), chamadaAtualizada.getStatus()),
                    () -> assertEquals(tecnicoAntigoDisponivel.getId(), chamadaAtualizada.getTecnico().getId()),
                    () -> assertFalse(tecnicoAtualizado.isDisponivel())
            );

        }                    

        @Test
        @DisplayName("Deixa a chamada no status aguardando_tecnico se não houver técnico disponviel")
        void deixaChamadaAguardandoTecnico() throws Exception {

            // Arrange
            tecnicoAntigoDisponivel.setDisponivel(false);
            tecnicoNovoDisponivel.setDisponivel(false);
            tecnicoRepository.saveAll(List.of(tecnicoAntigoDisponivel, tecnicoNovoDisponivel));

            // Act
            driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                    .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

            //Assert
            Chamado chamadoAtualizado = chamadoRepository.findById(chamado.getId()).get();

            assertAll(
                () -> assertEquals(ChamadoStatus.AGUARDANDO_TECNICO.getNome(), chamadoAtualizado.getStatus()),
                () -> assertNull(chamadoAtualizado.getTecnico())
            );
        }

        @Test
        @DisplayName("Quando técnico fica disponivel, deve assumir o chamado que está mais tempo aguardando técnico")
        void chamadoDeveAvancarQuandoTecnicoFicaDisponivel() throws Exception {

            // Arrange
            tecnicoAntigoDisponivel.setDisponivel(false);
            tecnicoNovoDisponivel.setDisponivel(false);
            tecnicoRepository.saveAll(List.of(tecnicoAntigoDisponivel, tecnicoNovoDisponivel));

            chamado.setStatus(ChamadoStatus.EM_ATENDIMENTO.getNome());
            chamado.setTecnico(tecnicoNovoDisponivel);
            chamadoRepository.save(chamado);

            Chamado chamadoAguardandoTecnico = chamadoRepository.save(Chamado.builder()
                    .empresa(empresaPadrao)
                    .cliente(clientePadrao)
                    .servico(servicoPadrao)
                    .enderecoAtendimento("Rua Base, 100")
                    .dataCriacao(LocalDateTime.now().minusMinutes(30))
                    .status(ChamadoStatus.AGUARDANDO_TECNICO.getNome())
                    .build());

            // Act
            driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/" + chamado.getId() + "/avancar-status")
                    .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

            entityManager.flush();

            // Assert

            Chamado chamadoAtualizado = chamadoRepository.findById(chamado.getId()).get();
            Chamado chamadoAguardandoTecnicoAtualizado = chamadoRepository.findById(chamadoAguardandoTecnico.getId()).get();
            Tecnico tecnicoAtualizado = tecnicoRepository.findById(tecnicoNovoDisponivel.getId()).get();

            assertAll(
                () -> assertEquals(ChamadoStatus.CONCLUIDO.getNome(), chamadoAtualizado.getStatus()),
                () -> assertEquals(tecnicoNovoDisponivel.getId(), chamadoAguardandoTecnicoAtualizado.getTecnico().getId()),
                () -> assertFalse(tecnicoAtualizado.isDisponivel())
            );

        }

    }
    
}