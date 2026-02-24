package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.exception.PlanoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.service.empresa.EmpresaServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.modelmapper.internal.bytebuddy.matcher.ElementMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do Controlador de Chamados")
public class ChamadoControllerTests {

    final String URI_CHAMADOS = "/chamados";
    @MockBean
    private ListenerChamado listenerChamado;
    @Autowired
    MockMvc driver;
    @Autowired
    ChamadoRepository chamadoRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    EmpresaRepository empresaRepository;
    @Autowired
    TecnicoRepository tecnicoRepository;
    @Autowired
    ServicoRepository servicoRepository;
    @Autowired
    EmpresaServiceImpl empresaService;
    @Autowired
    ObjectMapper objectMapper;

    @SpyBean
    Cliente cliente;
    Empresa empresa;
    Cliente clienteBasico;
    Cliente clientePremium;
    Servico servicoComum;
    Servico servicoExclusivo;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        empresa = empresaRepository.save(Empresa.builder()
                .nome("Service Corp")
                .codigoAcesso("101010")
                .cnpj("12.345.678/0001-90")
                .build());

        clienteBasico = clienteRepository.save(Cliente.builder()
                .nome("Teste Basico")
                .codigo("123456")
                .endereco("Rua Base, 100")
                .planoAtual(Plano.BASICO)
                .dataCobranca(LocalDate.now())
                .build());

        clientePremium = clienteRepository.save(Cliente.builder()
                .nome("Teste Premium")
                .codigo("654321")
                .endereco("Rua VIP, 200")
                .planoAtual(Plano.PREMIUM)
                .dataCobranca(LocalDate.now())
                .build());

        servicoComum = servicoRepository.save(Servico.builder()
                .nome("Pintura Simples")
                .descricao("Pintura de um quarto")
                .urgencia(Urgencia.NORMAL)
                .duracao(30.0)
                .preco(100.0)
                .empresa(empresa)
                .plano(Plano.BASICO)
                .disponivel(true)
                .tipo(TipoServico.PINTURA)
                .build());

        servicoExclusivo = servicoRepository.save(Servico.builder()
                .nome("Instalação 24h")
                .descricao("Instalação elétrica completa de urgência")
                .urgencia(Urgencia.ALTA)
                .duracao(120.0)
                .preco(300.0)
                .empresa(empresa)
                .plano(Plano.PREMIUM)
                .disponivel(true)
                .tipo(TipoServico.ELETRICA)
                .build());
    }

    @AfterEach
    void tearDown() {
        chamadoRepository.deleteAll();
        tecnicoRepository.deleteAll();
        servicoRepository.deleteAll();
        empresaRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Nested
    @DisplayName("Testes de Operações Basicas Em Chamado")
    class CRUDChamado {
        @Test
        @DisplayName("Criar chamado Básico com sucesso")
        void criarChamadoBasicoSucesso() throws Exception {
            ChamadoPostPutRequestDTO dto = ChamadoPostPutRequestDTO.builder()
                    .servicoId(servicoComum.getId())
                    .empresaId(empresa.getId())
                    .build();

            String response = driver.perform(post("/clientes/" + clienteBasico.getId() + "/chamados")
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ChamadoResponseDTO result = objectMapper.readValue(response, ChamadoResponseDTO.class);

            assertNotNull(result.getId());
            assertEquals(clienteBasico.getEndereco(), result.getEnderecoAtendimento());
        }

        @Test
        @DisplayName("Criar chamado Premium com sucesso")
        void criarChamadoPremiumSucesso() throws Exception {
            ChamadoPostPutRequestDTO dto = ChamadoPostPutRequestDTO.builder()
                    .servicoId(servicoExclusivo.getId())
                    .empresaId(empresa.getId())
                    .enderecoAtendimento("Casa de Praia")
                    .build();

            String response = driver.perform(post("/clientes/" + clientePremium.getId() + "/chamados")
                            .header("codigoAcesso", clientePremium.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            ChamadoResponseDTO result = objectMapper.readValue(response, ChamadoResponseDTO.class);

            assertNotNull(result.getId());
            assertEquals("Casa de Praia", result.getEnderecoAtendimento());
        }

        @Test
        @DisplayName("Confirmar pagamento com sucesso")
        void confirmarPagamentoSucesso() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .status("AGUARDANDO_PAGAMENTO")
                    .build();
            chamado.setEstado(new ChamadoEstadoAguardandoPagamento());
            chamado = chamadoRepository.save(chamado);

            driver.perform(put(URI_CHAMADOS + "/" + chamado.getId() + "/pagamento")
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .param("metodoPagamento", "PIX")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print());

            Chamado chamadoAtualizado = chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals("CHAMADO_RECEBIDO", chamadoAtualizado.getStatus());
        }

        @Test
        @DisplayName("Cliente tenta remover chamado com código errado")
        void removerChamadoCodigoErrado() throws Exception {
            Chamado chamado = chamadoRepository.save(Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .status("AGUARDANDO_PAGAMENTO")
                    .build());

            driver.perform(delete(URI_CHAMADOS + "/" + chamado.getId())
                            .header("codigoAcesso", "000000")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());

            assertTrue(chamadoRepository.existsById(chamado.getId()));
        }

        @Test
        @DisplayName("Remover chamado com sucesso pelo cliente")
        void removerChamadoSucesso() throws Exception {
            Chamado chamado = chamadoRepository.save(Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .status("AGUARDANDO_PAGAMENTO")
                    .build());

            driver.perform(delete(URI_CHAMADOS + "/" + chamado.getId())
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            assertFalse(chamadoRepository.existsById(chamado.getId()));
        }

        @Test
        @DisplayName("Remover chamado com sucesso pela empresa")
        void removerChamadoPorEmpresaSucesso() throws Exception {
            Chamado chamado = chamadoRepository.save(Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .status("AGUARDANDO_PAGAMENTO")
                    .build());

            driver.perform(delete(URI_CHAMADOS + "/" + chamado.getId())
                            .header("codigoAcesso", empresa.getCodigoAcesso())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            assertFalse(chamadoRepository.existsById(chamado.getId()));
        }

        @Test
        @DisplayName("Cliente Basico tenta criar chamado Premium")
        void criarChamadoBasicoParaServicoPremium() throws Exception {
            ChamadoPostPutRequestDTO dto = ChamadoPostPutRequestDTO.builder()
                    .servicoId(servicoExclusivo.getId())
                    .empresaId(empresa.getId())

                    .enderecoAtendimento("Rua Teste, 123")
                    .build();

            driver.perform(post("/clientes/" + clienteBasico.getId() + "/chamados")
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(result -> assertTrue(result.getResolvedException() instanceof PlanoInvalidoException))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("Testes de notificações em chamado")
    class NotificacaoChamado {
        @Test
        @DisplayName("Deve entrar em EM_ANALISE sem técnico e sem notificar listener")
        void deveEntrarEmAnaliseSemTecnicoESemNotificar() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("CHAMADO_RECEBIDO")
                    .build();
            chamado.mudaEstado(new ChamadoEstadoRecebido());
            chamado = chamadoRepository.save(chamado);
            driver.perform(
                            put("/empresas/" + empresa.getId()
                                    + "/chamados/" + chamado.getId()
                                    + "/avancar-status")
                                    .header("codigoAcesso", empresa.getCodigoAcesso())
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());
            Chamado atualizado =
                    chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals(
                    "EM_ANALISE",
                    atualizado.getStatus()
            );
            verify(listenerChamado, never()).notificar(any(Tecnico.class));
        }

        @Test
        @DisplayName("Não deve notificar listener quando chamado for concluído")
        void naoDeveNotificarQuandoChamadoConcluido() throws Exception {
            Tecnico tecnico = Tecnico.builder()
                    .nome("Carlos Silva")
                    .especialidade("Eletricista")
                    .corVeiculo("Branco")
                    .tipoVeiculo(TipoVeiculo.CARRO)
                    .placaVeiculo("ABC-1234")
                    .acesso("123456")
                    .build();
            tecnico.getEmpresasAprovadoras().add(empresa);
            tecnico = tecnicoRepository.save(tecnico);
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("EM_ATENDIMENTO")
                    .tecnico(tecnico)
                    .build();
            chamado.mudaEstado(new ChamadoEstadoEmAtendimento());
            chamado = chamadoRepository.save(chamado);
            driver.perform(
                            put("/empresas/" + empresa.getId()
                                    + "/chamados/" + chamado.getId()
                                    + "/avancar-status")
                                    .header("codigoAcesso", empresa.getCodigoAcesso())
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());
            Chamado atualizado =
                    chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals(
                    "CONCLUIDO",
                    atualizado.getStatus()
            );
            verify(listenerChamado, never()).notificar(any(Tecnico.class));
            ;
        }

        @Test
        @DisplayName("Não deve notificar listener quando entrar em CHAMADO_RECEBIDO")
        void naoDeveNotificarQuandoChamadoRecebido() throws Exception {

            // Arrange

            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("AGUARDANDO_PAGAMENTO")
                    .build();
            chamado.mudaEstado(new ChamadoEstadoAguardandoPagamento());
            chamado = chamadoRepository.save(chamado);
            PagamentoRequestDTO pagamentoDTO = PagamentoRequestDTO.builder()
                    .metodoPagamento("PIX")
                    .valorTotal(BigDecimal.valueOf(servicoComum.getPreco()))
                    .build();
            driver.perform(
                            post("/empresas/{empresaId}/chamados/{chamadoId}/pagamentos",
                                    empresa.getId(),
                                    chamado.getId()
                            )
                                    .header("codigoAcesso", empresa.getCodigoAcesso())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(pagamentoDTO))
                    )
                    .andExpect(status().isOk())
                    .andDo(print());

            // Assert
            Chamado atualizado =
                    chamadoRepository.findById(chamado.getId()).orElseThrow();
            assertEquals(
                    "CHAMADO_RECEBIDO",
                    atualizado.getStatus()
            );
            verify(listenerChamado, never()).notificar(any(Tecnico.class));

        }

        @ExtendWith(OutputCaptureExtension.class)
        @Test
        @DisplayName("Deve notificar listener quando entrar em EM_ATENDIMENTO")
        void deveNotificarQuandoEntrarEmAtendimento(CapturedOutput output) throws Exception {

            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("AGUARDANDO_TECNICO")
                    .build();

            chamado = chamadoRepository.save(chamado);

            Tecnico tecnico = tecnicoRepository.save(
                    Tecnico.builder()
                            .nome("Carlos Silva")
                            .corVeiculo("Branco")
                            .tipoVeiculo(TipoVeiculo.CARRO)
                            .placaVeiculo("ABC-1234")
                            .acesso("123")
                            .especialidade("GERAL")
                            .build()
            );

            empresaService.aprovarTecnico(
                    empresa.getId(),
                    tecnico.getId(),
                    empresa.getCodigoAcesso()
            );

            driver.perform(
                            put("/empresas/" + empresa.getId()
                                    + "/chamados/" + chamado.getId()
                                    + "/tecnicos/" + tecnico.getId())
                                    .header("codigoAcesso", empresa.getCodigoAcesso())
                                    .param("nomeTecnico", "Carlos Silva")
                                    .param("corVeiculo", "Branco")
                                    .param("tipoVeiculo", "CARRO")
                                    .param("placaVeiculo", "ABC-1234")
                                    .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(status().isOk());

            assertTrue(output.getOut().contains(
                    "Notificação de atendimento"
            ));
        }
    }
}