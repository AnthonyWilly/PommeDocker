package com.ufcg.psoft.commerce.controller;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
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
public class ServicoControllerTests {
    
    @Autowired
    MockMvc driver;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    EmpresaRepository empresaRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    ServicoRepository servicoRepository;
    
    Empresa empresaPadrao;
    Empresa empresa;
    Cliente clienteBasico;
    Cliente clientePremium;
    Servico servicoPadrao;
    Servico servicoBasico;
    Servico servicoPremium;
    ServicoPostPutRequestDTO servicoDTO;
    
    final String URI_SERVICOS = "/servicos";
    final String URI_CATALOGO = "/catalogo";
    final String CODIGO_ACESSO_PADRAO = "123456";
    final String CNPJ_PADRAO = "12.345.678/0001-90";
    final String NOME_PADRAO = "Empresa Exemplo";
    
    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        empresaPadrao = empresaRepository.save(Empresa.builder()
                .nome(NOME_PADRAO)
                .cnpj(CNPJ_PADRAO)
                .codigoAcesso(CODIGO_ACESSO_PADRAO)
                .build()
        );
        
        empresa = empresaRepository.save(Empresa.builder()
                .nome("Empresa Exemplo 2")
                .cnpj("99.999.999/9999-99")
                .codigoAcesso(CODIGO_ACESSO_PADRAO)
                .build()
        );
        
        clienteBasico = clienteRepository.save(Cliente.builder()
                .nome("Cliente Basico")
                .endereco("Rua dos Testes, 123")
                .planoAtual(Plano.BASICO)
                .codigo("111111")
                .proxPlano(null)
                .dataCobranca(LocalDate.now().plusDays(30))
                .build());

        clientePremium = clienteRepository.save(Cliente.builder()
                .nome("Cliente Premium")
                .endereco("Rua dos Testes, 123")
                .planoAtual(Plano.PREMIUM)
                .codigo("222222")
                .proxPlano(null)
                .dataCobranca(LocalDate.now().plusDays(30))
                .build());

        servicoPadrao = servicoRepository.save(Servico.builder()
                .nome("Pintura")
                .tipo(TipoServico.PINTURA)
                .urgencia(Urgencia.NORMAL)
                .descricao("Pintar determinada area")
                .preco(100.0)
                .duracao(3.0)
                .disponivel(false)
                .empresa(empresaPadrao)
                .plano(Plano.BASICO)
                .build()
        );

        servicoDTO = ServicoPostPutRequestDTO.builder()
                .nome("Pintura")
                .tipo(TipoServico.PINTURA)
                .urgencia(Urgencia.URGENTE)
                .descricao("Pintar determinada area")
                .preco(100.0)
                .duracao(3.0)
                .disponivel(false)
                .plano(Plano.PREMIUM)
                .build();

        servicoBasico = servicoRepository.save(Servico.builder()
                .nome("Reparo Hidraulico")
                .tipo(TipoServico.HIDRAULICA)
                .urgencia(Urgencia.BAIXA)
                .descricao("Reparo Hidraulico completo")
                .preco(100.0)
                .duracao(1.0)
                .disponivel(true)
                .plano(Plano.BASICO) 
                .empresa(empresa)
                .build());

        servicoPremium = servicoRepository.save(Servico.builder()
                .nome("Guinchar carro")
                .tipo(TipoServico.LIMPEZA)
                .urgencia(Urgencia.URGENTE)
                .descricao("Recuperar carro")
                .preco(500.0)
                .duracao(2.0)
                .disponivel(true)
                .plano(Plano.PREMIUM)
                .empresa(empresa)
                .build());
        
    }
    
    @AfterEach
    void tearDown() {
        servicoRepository.deleteAll();
        clienteRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Test
    @DisplayName("Criar serviço com sucesso")
    void criarServicoComSucesso() throws Exception {
        String response = driver.perform(
                        post("/empresas/" + empresaPadrao.getId() + "/servicos")
                                .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(servicoDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ServicoResponseDTO resultado =
                objectMapper.readValue(response, ServicoResponseDTO.class);

        assertAll(
                () -> assertEquals("Pintura", resultado.getNome()),
                () -> assertEquals(empresaPadrao.getId(), resultado.getEmpresaId())
        );
    }
    @Test
    @DisplayName("Falhar ao adicionar serviço com código de acesso incorreto")
    void adicionarServicoCodigoAcessoIncorreto() throws Exception {
        String responseJsonString = driver.perform(
                        post("/empresas/" + empresaPadrao.getId() + "/servicos")
                                .header("codigoAcesso", "999999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(servicoDTO))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomErrorType resultado =
                objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Codigo de acesso invalido!", resultado.getMessage());
    }

    @Test
    @DisplayName("Alterar um serviço existente para Hidráulica")
    void alterarServicoParaHidraulicaValido() throws Exception {
        ServicoPostPutRequestDTO hidraulicaDTO = ServicoPostPutRequestDTO.builder()
                .nome("Reparo Hidraulico")
                .tipo(TipoServico.HIDRAULICA)
                .urgencia(Urgencia.URGENTE)
                .descricao("Troca de tubulação")
                .preco(150.0)
                .duracao(1.5)
                .disponivel(true)
                .plano(Plano.BASICO)
                .build();

        String responseJsonString = driver.perform(
                        put("/empresas/" + empresaPadrao.getId() +
                                "/servicos/" + servicoPadrao.getId())
                                .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(hidraulicaDTO))
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ServicoResponseDTO resultado =
                objectMapper.readValue(responseJsonString, ServicoResponseDTO.class);

        assertEquals("Reparo Hidraulico", resultado.getNome());
    }

    @Test
    @DisplayName("Falhar ao alterar serviço com código de acesso inválido")
    void alterarServicoCodigoAcessoInvalido() throws Exception {
        ServicoPostPutRequestDTO marcenariaDTO = ServicoPostPutRequestDTO.builder()
                .nome("Reparo de Marcenaria")
                .tipo(TipoServico.MARCENARIA)
                .urgencia(Urgencia.NORMAL)
                .descricao("Troca de puxadores")
                .preco(80.0)
                .duracao(1.0)
                .disponivel(true)
                .plano(Plano.BASICO)
                .build();

        String responseJsonString = driver.perform(
                        put("/empresas/" + empresaPadrao.getId() +
                                "/servicos/" + servicoPadrao.getId())
                                .header("codigoAcesso", "000000")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(marcenariaDTO))
                )
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CustomErrorType resultado =
                objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertAll(
                () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage()),
                () -> assertEquals(
                        "Pintura",
                        servicoRepository.findById(servicoPadrao.getId()).get().getNome()
                )
        );
    }

    @Test
    @DisplayName("Remover serviço com código de acesso válido")
    void removerServicoComSucesso() throws Exception {
        driver.perform(
                        delete("/empresas/" + empresaPadrao.getId() +
                                "/servicos/" + servicoPadrao.getId())
                                .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                )
                .andExpect(status().isNoContent());

        assertFalse(servicoRepository.findById(servicoPadrao.getId()).isPresent());
    }
    @Test
    @DisplayName("Falhar ao remover serviço com código de acesso inválido")
    void removerServicoCodigoAcessoInvalido() throws Exception {
        driver.perform(
                        delete("/empresas/" + empresaPadrao.getId() +
                                "/servicos/" + servicoPadrao.getId())
                                .header("codigoAcesso", "000000")
                )
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException().getMessage()
                                .contains("Codigo de acesso invalido!"))
                );
    }
    @Test
    @DisplayName("Listar serviços de uma empresa com sucesso")
    void listarServicosDaEmpresaComSucesso() throws Exception {
        servicoRepository.save(Servico.builder()
                .nome("Instalacao Eletrica")
                .tipo(TipoServico.ELETRICA)
                .urgencia(Urgencia.URGENTE)
                .descricao("Troca de fiação")
                .preco(200.0)
                .duracao(2.0)
                .disponivel(true)
                .empresa(empresaPadrao)
                .plano(Plano.PREMIUM)
                .build());

        String responseJsonString = driver.perform(
                        get("/empresas/" + empresaPadrao.getId() + "/servicos")
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ServicoResponseDTO> resultado =
                objectMapper.readValue(
                        responseJsonString,
                        new TypeReference<List<ServicoResponseDTO>>() {}
                );

        assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertTrue(resultado.stream().anyMatch(s -> s.getNome().equals("Pintura"))),
                () -> assertTrue(resultado.stream().anyMatch(s -> s.getNome().equals("Instalacao Eletrica")))
        );
    }

    @Nested
    @DisplayName("Conjunto de testes de verificação de catálogo de serviços por plano")
    class catalogoDeServicosPorPlano {
    
        @Test
        @DisplayName("Deve listar serviços do plano básico para cliente com plano básico (disponíveis primeiro)")
        void quandoClienteBasicoAcessaCatalogo() throws Exception {

            String responseJsoString = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                            .param("clienteId", clienteBasico.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<ServicoResponseDTO> resultados = objectMapper.readValue(responseJsoString, new TypeReference<List<ServicoResponseDTO>>() {});

            assertAll(
                () -> assertEquals(2, resultados.size()),
                () -> assertEquals("Reparo Hidraulico", resultados.get(0).getNome()),
                () -> assertTrue(resultados.get(0).getDisponivel()),
                () -> assertEquals("Pintura", resultados.get(1).getNome()),
                () -> assertFalse(resultados.get(1).getDisponivel())
            );

        }                    

        @Test
        @DisplayName("Deve listar serviços do plano premium para cliente com plano premium (disponíveis primeiro)")
        void quandoClientePremiumAcessaCatalogo() throws Exception {

            String responseJsoString = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                            .param("clienteId", clientePremium.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<ServicoResponseDTO> resultados = objectMapper.readValue(responseJsoString, new TypeReference<List<ServicoResponseDTO>>() {});

            assertAll(
                () -> assertEquals(3, resultados.size()),
                () -> assertTrue(resultados.get(0).getDisponivel()),
                () -> assertTrue(resultados.get(1).getDisponivel()),
                () -> assertEquals("Pintura", resultados.get(2).getNome()),
                () -> assertFalse(resultados.get(2).getDisponivel())
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
                        .param("tipo", "HIDRAULICA")
                        .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<Servico> resultado = objectMapper.readValue(responJsoString, new TypeReference<List<Servico>>() {});

            assertTrue(resultado.stream().allMatch(s -> s.getTipo().equals(TipoServico.HIDRAULICA)));
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
                () -> assertEquals(2, resultado.size()),
                () -> assertTrue(resultado.stream().allMatch(s -> s.getPreco() <= 200.0))
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

    @Nested
    @DisplayName("Disponibilidade de serviço")
    class DisponibilidadeDeServicoController {

        @Test
        @DisplayName("alterar disponibilidade para false com código válido retorna 200")
        void alterarDisponibilidadeParaFalsoComCodigoValido() throws Exception {
            driver.perform(
                            patch("/empresas/" + empresaPadrao.getId() +
                                    "/servicos/" + servicoPadrao.getId() + "/disponibilidade")
                                    .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                                    .param("disponivel", "false")
                    )
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("alterar disponibilidade para true com código válido retorna 200")
        void alterarDisponibilidadeParaVerdadeiroComCodigoValido() throws Exception {
            driver.perform(
                            patch("/empresas/" + empresaPadrao.getId() +
                                    "/servicos/" + servicoPadrao.getId() + "/disponibilidade")
                                    .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                                    .param("disponivel", "true")
                    )
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
        }

        @Test
        @DisplayName("código de acesso inválido ao alterar disponibilidade retorna 400")
        void codigoAcessoInvalidoNaAlteracaoDisponibilidade() throws Exception {
            String response = driver.perform(
                            patch("/empresas/" + empresaPadrao.getId() +
                                    "/servicos/" + servicoPadrao.getId() + "/disponibilidade")
                                    .header("codigoAcesso", "000000")
                                    .param("disponivel", "true")
                    )
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType erro = objectMapper.readValue(response, CustomErrorType.class);
            assertEquals("Codigo de acesso invalido!", erro.getMessage());
        }

        @Test
        @DisplayName("serviço inexistente ao alterar disponibilidade retorna 404")
        void servicoNaoExisteNaAlteracaoDisponibilidade() throws Exception {
            driver.perform(
                            patch("/empresas/" + empresaPadrao.getId() +
                                    "/servicos/999999/disponibilidade")
                                    .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                                    .param("disponivel", "true")
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("serviço de outra empresa ao alterar disponibilidade retorna 404")
        void servicoNaoPertenceAEmpresaNaAlteracaoDisponibilidade() throws Exception {
            driver.perform(
                            patch("/empresas/" + empresaPadrao.getId() +
                                    "/servicos/" + servicoBasico.getId() + "/disponibilidade")
                                    .header("codigoAcesso", CODIGO_ACESSO_PADRAO)
                                    .param("disponivel", "true")
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("catálogo exibe disponíveis primeiro e indisponíveis por último")
        void listarServicosDisponivelPrimeiroEIndisponivelPorUltimo() throws Exception {
            String response = driver.perform(get(URI_SERVICOS + URI_CATALOGO)
                            .param("clienteId", clientePremium.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            List<ServicoResponseDTO> resultados = objectMapper.readValue(
                    response, new TypeReference<List<ServicoResponseDTO>>() {});

            boolean disponivelVeioAntes = true;
            boolean encontrouIndisponivel = false;
            for (ServicoResponseDTO s : resultados) {
                if (!s.getDisponivel()) {
                    encontrouIndisponivel = true;
                }
                if (encontrouIndisponivel && s.getDisponivel()) {
                    disponivelVeioAntes = false;
                    break;
                }
            }
            assertTrue(disponivelVeioAntes);
        }

        @Test
        @DisplayName("registrar interesse em serviço retorna 201")
        void clienteRegistraInteresseEmServico() throws Exception {
            driver.perform(
                            post("/servicos/" + servicoPadrao.getId() + "/interesse")
                                    .param("clienteId", clienteBasico.getId().toString())
                    )
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("cliente inexistente ao registrar interesse retorna 404")
        void clienteNaoExisteAoRegistrarInteresse() throws Exception {
            driver.perform(
                            post("/servicos/" + servicoPadrao.getId() + "/interesse")
                                    .param("clienteId", "999999")
                    )
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("serviço inexistente ao registrar interesse retorna 404")
        void servicoNaoExisteAoRegistrarInteresse() throws Exception {
            driver.perform(
                            post("/servicos/999999/interesse")
                                    .param("clienteId", clienteBasico.getId().toString())
                    )
                    .andExpect(status().isNotFound());
        }
    }
}
