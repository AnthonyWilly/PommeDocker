package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ServicoControllerTests {
    @Autowired
    MockMvc driver;
    @Autowired
    EmpresaRepository empresaRepository;
    @Autowired
    ObjectMapper objectMapper;
    Empresa empresaPadrao;
    @Autowired
    ServicoRepository servicoRepository;
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


        servicoPadrao = servicoRepository.save(Servico.builder()
                .nome("Pintura")
                .tipo(TipoServico.PINTURA)
                .urgencia(Urgencia.BAIXA)
                .descricao("Pintar determinada area")
                .preco(100.0)
                .duracao(3.0)
                .disponivel(true)
                .idEmpresa(empresaPadrao.getId())
                .idPlano("Basico")
                .build()

        );

        servicoDTO = ServicoPostPutRequestDTO.builder()
                .nome("Pintura")
                .tipo(TipoServico.PINTURA)
                .urgencia(Urgencia.MEDIA)
                .descricao("Pintar determinada area")
                .preco(100.0)
                .duracao(3.0)
                .disponivel(true)
                .idPlano("Basico")
                .build();
    }
    @AfterEach
    void tearDown() {
        servicoRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Test
    @DisplayName("Criar serviço com sucesso")
    void criarServicoComSucesso() throws Exception {
        String response = driver.perform(
                        post("/servicos")
                                .param("empresaId", empresaPadrao.getId().toString())
                                .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
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
                () -> assertEquals(empresaPadrao.getId(), resultado.getIdEmpresa())
        );
    }
    @Test
    @DisplayName("Falhar ao adicionar serviço com código de acesso incorreto")
    void adicionarServicoCodigoAcessoIncorreto() throws Exception {
        String responseJsonString = driver.perform(
                        post("/servicos")
                                .param("empresaId", empresaPadrao.getId().toString())
                                .param("codigoAcesso", "999999")
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
                .urgencia(Urgencia.MEDIA)
                .descricao("Troca de tubulação")
                .preco(150.0)
                .duracao(1.5)
                .disponivel(true)
                .idPlano("Basico")
                .build();

        String responseJsonString = driver.perform(
                        put("/servicos/" + servicoPadrao.getId())
                                .param("empresaId", empresaPadrao.getId().toString())
                                .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
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
                .urgencia(Urgencia.BAIXA)
                .descricao("Troca de puxadores")
                .preco(80.0)
                .duracao(1.0)
                .disponivel(true)
                .idPlano("Basico")
                .build();

        String responseJsonString = driver.perform(
                        put("/servicos/" + servicoPadrao.getId())
                                .param("empresaId", empresaPadrao.getId().toString())
                                .param("codigoAcesso", "000000")
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
                        delete("/servicos/" + servicoPadrao.getId())
                                .param("empresaId", empresaPadrao.getId().toString())
                                .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                )
                .andExpect(status().isNoContent());

        assertFalse(servicoRepository.findById(servicoPadrao.getId()).isPresent());
    }
    @Test
    @DisplayName("Falhar ao remover serviço com código de acesso inválido")
    void removerServicoCodigoAcessoInvalido() throws Exception {
        driver.perform(
                        delete("/servicos/" + servicoPadrao.getId())
                                .param("empresaId", empresaPadrao.getId().toString())
                                .param("codigoAcesso", "000000")
                )
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException()
                                instanceof CodigoDeAcessoInvalidoException)
                );
    }
    @Test
    @DisplayName("Listar serviços de uma empresa com sucesso")
    void listarServicosDaEmpresaComSucesso() throws Exception {
        servicoRepository.save(Servico.builder()
                .nome("Instalacao Eletrica")
                .tipo(TipoServico.ELETRICA)
                .urgencia(Urgencia.ALTA)
                .descricao("Troca de fiação")
                .preco(200.0)
                .duracao(2.0)
                .disponivel(true)
                .idEmpresa(empresaPadrao.getId())
                .idPlano("Premium")
                .build());

        String responseJsonString = driver.perform(
                        get("/servicos")
                                .param("empresaId", empresaPadrao.getId().toString())
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





}
