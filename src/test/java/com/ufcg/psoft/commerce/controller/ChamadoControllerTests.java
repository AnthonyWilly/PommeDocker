package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.exception.ChamadoNaoPodeSerCancelado;
import com.ufcg.psoft.commerce.exception.PlanoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.service.empresa.EmpresaServiceImpl;
import jakarta.servlet.ServletException;
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
import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

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

    @Nested
    @DisplayName("Conjunto de casos de teste de verificação de Listagem de Chamados")

        class ListagemChamados {

            @Test
            @DisplayName("Quando buscamos um chamado de um cliente")
            void quandoBuscamosChamadoDeUmCliente() throws Exception {
                Chamado chamado = Chamado.builder()
                .cliente(clienteBasico)
                .empresa(empresa)
                .servico(servicoComum)
                .status("AGUARDANDO_PAGAMENTO")
                .build();
        
                chamado.setEstado(new ChamadoEstadoAguardandoPagamento());
                Chamado chamadoSalvo = chamadoRepository.save(chamado);
                String responseJsonString = driver.perform(get("/clientes/{clienteId}/chamados/{chamadoId}",
                                clienteBasico.getId(),
                                chamadoSalvo.getId())
                                .header("codigoAcesso", clienteBasico.getCodigo())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn().getResponse().getContentAsString();

                ChamadoResponseDTO resultado = objectMapper.readValue(responseJsonString, ChamadoResponseDTO.class);
                assertAll(
                        () -> assertNotNull(resultado),
                        () -> assertEquals(chamadoSalvo.getId(), resultado.getId()),
                        () -> assertEquals("AGUARDANDO_PAGAMENTO", resultado.getStatus())
                );
            }

            @Test
            @DisplayName("Quando listamos chamados filtrando por status")
            void quandoListamosPorStatus() throws Exception {
                ChamadoStatus statusAlvo = ChamadoStatus.AGUARDANDO_PAGAMENTO;
                Chamado chamadoStatus = Chamado.builder()
                        .cliente(clienteBasico)
                        .empresa(empresa)
                        .servico(servicoComum)
                        .status(statusAlvo.name())
                        .build();
                chamadoRepository.save(chamadoStatus);
                String responseJsonString = driver.perform(get("/clientes/{clienteId}/chamados/status", clienteBasico.getId())
                                .param("status", statusAlvo.name())
                                .header("codigoAcesso", clienteBasico.getCodigo())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn().getResponse().getContentAsString();
                List<ChamadoResponseDTO> resultado = objectMapper.readValue(responseJsonString,
                        new TypeReference<List<ChamadoResponseDTO>>() {});
                assertAll(
                        () -> assertFalse(resultado.isEmpty(), "A lista não deveria estar vazia"),
                        () -> assertEquals(statusAlvo.name(), resultado.get(0).getStatus()),
                        () -> assertTrue(resultado.stream().allMatch(c -> c.getStatus().equals(statusAlvo.name())))
                );
            }

            @Test
            @DisplayName("Quando listamos todos os chamados de um cliente com sucesso")
            void quandoListamosTodosOsChamados() throws Exception {
                Chamado chamado1 = Chamado.builder()
                        .cliente(clienteBasico)
                        .empresa(empresa)
                        .servico(servicoComum)
                        .status("AGUARDANDO_PAGAMENTO")
                        .build();
                Chamado chamado2 = Chamado.builder()
                        .cliente(clienteBasico)
                        .empresa(empresa)
                        .servico(servicoComum)
                        .status("CONCLUIDO")
                        .build();
                chamadoRepository.saveAll(Arrays.asList(chamado1, chamado2));
                String responseJsonString = driver.perform(get("/clientes/{clienteId}/chamados", clienteBasico.getId())
                                .header("codigoAcesso", clienteBasico.getCodigo())
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andDo(print())
                        .andReturn().getResponse().getContentAsString();
                List<ChamadoResponseDTO> resultado = objectMapper.readValue(responseJsonString,
                        new TypeReference<List<ChamadoResponseDTO>>() {});
                assertAll(
                        () -> assertNotNull(resultado),
                        () -> assertEquals(2, resultado.size(), "Deveria retornar os 2 chamados do cliente"),
                        () -> assertEquals("AGUARDANDO_PAGAMENTO", resultado.get(0).getStatus()),
                        () -> assertEquals("CONCLUIDO", resultado.get(1).getStatus())
                );
            }

            @Test
            @DisplayName("Quando listamos chamados com código de acesso inválido")
            void quandoListamosComCodigoInvalido() throws Exception {
                String codigoErrado = "000000";
                driver.perform(get("/clientes/{clienteId}/chamados", clienteBasico.getId())
                                .header("codigoAcesso", codigoErrado)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }

            @Test
            @DisplayName("Quando buscamos um chamado específico com código de acesso inválido")
            void quandoBuscamosUmChamadoComCodigoInvalido() throws Exception {
                Chamado chamado = Chamado.builder()
                        .cliente(clienteBasico)
                        .status("AGUARDANDO_PAGAMENTO")
                        .build();
                Chamado chamadoSalvo = chamadoRepository.save(chamado);
                String codigoErrado = "000000";
                driver.perform(get("/clientes/{clienteId}/chamados/{chamadoId}",
                                clienteBasico.getId(), chamadoSalvo.getId())
                                .header("codigoAcesso", codigoErrado)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }

            @Test
            @DisplayName("Quando listamos chamados por status com código de acesso inválido")
            void quandoListamosPorStatusComCodigoInvalido() throws Exception {
                String codigoErrado = "000000";
                driver.perform(get("/clientes/{clienteId}/chamados/status", clienteBasico.getId())
                                .param("status", "AGUARDANDO_PAGAMENTO")
                                .header("codigoAcesso", codigoErrado)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andDo(print());
            }
    }
    
    @Nested
    @DisplayName("Conjunto de casos de teste de verificação de Cancelamento de chamados")
    class CancelamentoChamado {

        @Test
        @DisplayName("Deve cancelar chamado com sucesso em aguardando tecnico")
        void cancelarChamadoComSucessoAguardandoTecnico() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("AGUARDANDO_TECNICO")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            Chamado chamadoSalvo = chamadoRepository.save(chamado);
            driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                            clienteBasico.getId(), chamadoSalvo.getId())
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andDo(print());
            assertFalse(chamadoRepository.findById(chamadoSalvo.getId()).isPresent());
        }

        @Test
        @DisplayName("Deve cancelar chamado com sucesso em aguardando pagamento")
        void cancelarChamadoComSucessoAguardandoPagamento() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("AGUARDANDO_PAGAMENTO")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            Chamado chamadoSalvo = chamadoRepository.save(chamado);
            driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                            clienteBasico.getId(), chamadoSalvo.getId())
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andDo(print());
            assertFalse(chamadoRepository.findById(chamadoSalvo.getId()).isPresent());
        }

        @Test
        @DisplayName("Deve cancelar chamado com sucesso quando chamado foi recebido")
        void cancelarChamadoComSucessoChamadoRecebido() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("CHAMADO_RECEBIDO")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            Chamado chamadoSalvo = chamadoRepository.save(chamado);
            driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                            clienteBasico.getId(), chamadoSalvo.getId())
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andDo(print());
            assertFalse(chamadoRepository.findById(chamadoSalvo.getId()).isPresent());
        }

        @Test
        @DisplayName("Deve cancelar chamado com sucesso em analise")
        void cancelarChamadoComSucessoEmAnalise() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("EM_ANALISE")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            Chamado chamadoSalvo = chamadoRepository.save(chamado);
            driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                            clienteBasico.getId(), chamadoSalvo.getId())
                            .header("codigoAcesso", clienteBasico.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andDo(print());
            assertFalse(chamadoRepository.findById(chamadoSalvo.getId()).isPresent());
        }

        @Test
        @DisplayName("Não deve cancelar chamado quando o status for EM_ATENDIMENTO")
        void naoDeveCancelarChamadoEmAtendimento() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("EM_ATENDIMENTO")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            Chamado chamadoSalvo = chamadoRepository.save(chamado);
            Exception resolvedException = assertThrows(ServletException.class, () -> {
                driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                                clienteBasico.getId(), chamadoSalvo.getId())
                                .header("codigoAcesso", clienteBasico.getCodigo()))
                        .andReturn();
            });
            assertTrue(resolvedException.getCause() instanceof ChamadoNaoPodeSerCancelado);
            assertEquals("O Chamado Não Pode Ser Cancelado no Estado Atual!", resolvedException.getCause().getMessage());
            assertTrue(chamadoRepository.findById(chamadoSalvo.getId()).isPresent());
        }

        @Test
        @DisplayName("Não deve cancelar chamado quando o status for concluido")
        void naoDeveCancelarChamadoConcluido() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .enderecoAtendimento(clienteBasico.getEndereco())
                    .status("CONCLUIDO")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            Chamado chamadoSalvo = chamadoRepository.save(chamado);
            Exception resolvedException = assertThrows(ServletException.class, () -> {
                driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                                clienteBasico.getId(), chamadoSalvo.getId())
                                .header("codigoAcesso", clienteBasico.getCodigo()))
                        .andReturn();
            });
            assertTrue(resolvedException.getCause() instanceof ChamadoNaoPodeSerCancelado);
            assertEquals("O Chamado Não Pode Ser Cancelado no Estado Atual!", resolvedException.getCause().getMessage());
            assertTrue(chamadoRepository.findById(chamadoSalvo.getId()).isPresent());
        }

        @Test
        @DisplayName("Não deve cancelar chamado se o ID do cliente não for o dono do chamado")
        void naoDeveCancelarChamadoDeOutroCliente() throws Exception {
            Cliente invasor = clienteRepository.save(Cliente.builder()
                    .nome("Cliente Invasor")
                    .codigo("654321")
                    .dataCobranca(LocalDate.now())
                    .planoAtual(Plano.BASICO)
                    .endereco("Rua dos Bobos, 0")
                    .build());
            Chamado chamadoDoBasico = chamadoRepository.save(Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .status("EM_ANALISE")
                    .dataCriacao(LocalDateTime.now())
                    .build());
            driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                            invasor.getId(), chamadoDoBasico.getId())
                            .header("codigoAcesso", invasor.getCodigo())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andDo(print());
            assertTrue(chamadoRepository.findById(chamadoDoBasico.getId()).isPresent());
        }
        @Test
        
        @DisplayName("Não deve cancelar chamado quando o código de acesso for inválido")
        void naoDeveCancelarChamadoComSenhaInvalida() throws Exception {
            Chamado chamado = Chamado.builder()
                    .cliente(clienteBasico)
                    .empresa(empresa)
                    .servico(servicoComum)
                    .status("EM_ANALISE")
                    .dataCriacao(LocalDateTime.now())
                    .build();
            Chamado chamadoSalvo = chamadoRepository.save(chamado);
            driver.perform(delete("/clientes/{clienteId}/chamados/{chamadoId}",
                            clienteBasico.getId(), chamadoSalvo.getId())
                            .header("codigoAcesso", "SENHA_ERRADA") // Senha incorreta
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
            assertTrue(chamadoRepository.findById(chamadoSalvo.getId()).isPresent());
        }
    }
}

