package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.math.BigDecimal;
import com.fasterxml.jackson.core.type.TypeReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Empresas")
public class EmpresaControllerTests {

    final String URI_EMPRESAS = "/empresas";
    final String SENHA_ADMIN = "admin123";
    final String CODIGO_ACESSO_PADRAO = "123456";
    final String CNPJ_PADRAO = "12.345.678/0001-90";
    final String NOME_PADRAO = "Empresa Exemplo";

    @Autowired
    MockMvc driver;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    ObjectMapper objectMapper;

    Empresa empresaPadrao;
    EmpresaPostPutRequestDTO empresaPostPutRequestDTO;

    @Autowired
    TecnicoRepository tecnicoRepository;

    Tecnico tecnicoPadrao;

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());

        empresaPadrao = empresaRepository.save(Empresa.builder()
                .nome(NOME_PADRAO)
                .cnpj(CNPJ_PADRAO)
                .codigoAcesso(CODIGO_ACESSO_PADRAO)
                .build()
        );

        empresaPostPutRequestDTO = EmpresaPostPutRequestDTO.builder()
                .nome(empresaPadrao.getNome())
                .cnpj(empresaPadrao.getCnpj())
                .codigoAcesso(empresaPadrao.getCodigoAcesso())
                .senhaAdmin(SENHA_ADMIN)
                .build();
        
        tecnicoPadrao = tecnicoRepository.save(Tecnico.builder()
                .nome("Tecnico Teste")
                .acesso("123456")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .placaVeiculo("XYZ-1234")
                .corVeiculo("Branco")
                .especialidade("Geral")
                .build());
    }

    @AfterEach
    void tearDown() {
        tecnicoRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Test
    @DisplayName("Criar empresa com sucesso")
    void criarEmpresaComSucesso() throws Exception {
        empresaPostPutRequestDTO.setCnpj("99.999.999/0001-99");

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EmpresaResponseDTO resultado = objectMapper.readValue(responseJsonString, EmpresaResponseDTO.class);
        
        assertNotNull(resultado.getId());
        assertEquals(empresaPostPutRequestDTO.getNome(), resultado.getNome());
        assertEquals(empresaPostPutRequestDTO.getCnpj(), resultado.getCnpj());
    }

    @Test
    @DisplayName("Criar empresa com código de acesso inválido (curto demais)")
    void criarEmpresaCodigoAcessoCurto() throws Exception {
        empresaPostPutRequestDTO.setCodigoAcesso("123");

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertAll(
                () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos", resultado.getErrors().get(0))
        );
    }

    @Test
    @DisplayName("Criar empresa com código de acesso inválido (não numérico)")
    void criarEmpresaCodigoAcessoNaoNumerico() throws Exception {
        empresaPostPutRequestDTO.setCodigoAcesso("abcdef");

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertAll(
                () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                () -> assertEquals("Codigo de acesso deve conter apenas digitos", resultado.getErrors().get(0))
        );
    }

    @Test
    @DisplayName("Criar empresa sem senha de administrador")
    void criarEmpresaSemSenhaAdmin() throws Exception {
        empresaPostPutRequestDTO.setSenhaAdmin("");

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertAll(
                () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                () -> assertEquals("Senha de administrador obrigatoria", resultado.getErrors().get(0))
        );
    }

    @Test
    @DisplayName("Alterar empresa com sucesso")
    void alterarEmpresaComSucesso() throws Exception {
        Empresa empresaSalva = empresaRepository.save(empresaPadrao);

        EmpresaPostPutRequestDTO updateDTO = EmpresaPostPutRequestDTO.builder()
                .nome("Empresa Atualizada")
                .cnpj(empresaSalva.getCnpj())
                .codigoAcesso(empresaSalva.getCodigoAcesso())
                .senhaAdmin(SENHA_ADMIN)
                .build();

        String responseJsonString = driver.perform(put(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EmpresaResponseDTO resultado = objectMapper.readValue(responseJsonString, EmpresaResponseDTO.class);
        
        assertEquals("Empresa Atualizada", resultado.getNome());
    }

    @Test
    @DisplayName("Alterar empresa com código de acesso inválido")
    void alterarEmpresaCodigoAcessoInvalido() throws Exception {
        Empresa empresaSalva = empresaRepository.save(empresaPadrao);

        EmpresaPostPutRequestDTO updateDTO = EmpresaPostPutRequestDTO.builder()
                .nome("Empresa Atualizada")
                .cnpj(empresaSalva.getCnpj())
                .codigoAcesso("000000")
                .senhaAdmin(SENHA_ADMIN)
                .build();

        String responseJsonString = driver.perform(put(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CodigoDeAcessoInvalidoException))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Codigo de acesso invalido!", resultado.getMessage());
    }

    @Test
    @DisplayName("Remover empresa com código de acesso inválido")
    void removerEmpresaCodigoAcessoInvalido() throws Exception {
        Empresa empresaSalva = empresaRepository.save(empresaPadrao);

        EmpresaPostPutRequestDTO deleteDTO = EmpresaPostPutRequestDTO.builder()
                .cnpj(empresaSalva.getCnpj())
                .codigoAcesso("000000")
                .senhaAdmin(SENHA_ADMIN)
                .nome(empresaSalva.getNome())
                .build();

        String responseJsonString = driver.perform(delete(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CodigoDeAcessoInvalidoException))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Codigo de acesso invalido!", resultado.getMessage());
    }

    @Test
    @DisplayName("Remover empresa com sucesso")
    void removerEmpresaComSucesso() throws Exception {
        Empresa empresaSalva = empresaRepository.save(empresaPadrao);

        EmpresaPostPutRequestDTO deleteDTO = EmpresaPostPutRequestDTO.builder()
                .cnpj(empresaSalva.getCnpj())
                .codigoAcesso(empresaSalva.getCodigoAcesso())
                .senhaAdmin(SENHA_ADMIN)
                .nome(empresaSalva.getNome())
                .build();

        driver.perform(delete(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDTO)))
                .andExpect(status().isNoContent())
                .andDo(print());
        
        assertFalse(empresaRepository.findById(empresaSalva.getId()).isPresent());
    }
    
    @Test
    @DisplayName("Falhar ao criar empresa com senha de administrador incorreta")
    void criarEmpresaSenhaAdminIncorreta() throws Exception {
        empresaPostPutRequestDTO.setSenhaAdmin("senha_errada");

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isForbidden())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Senha invalida!", resultado.getMessage());
    }

    @Test
    @DisplayName("Buscar empresa por id com sucesso")
    void buscarEmpresaPorIdComSucesso() throws Exception {
        Empresa empresaSalva = empresaRepository.save(empresaPadrao);

        String responseJsonString = driver.perform(get(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        EmpresaResponseDTO resultado = objectMapper.readValue(responseJsonString, EmpresaResponseDTO.class);

        assertEquals(empresaSalva.getNome(), resultado.getNome());
        assertEquals(empresaSalva.getCnpj(), resultado.getCnpj());
    }

    @Test
    @DisplayName("Buscar empresa por id não encontrada")
    void buscarEmpresaPorIdNaoEncontrada() throws Exception {
        String responseJsonString = driver.perform(get(URI_EMPRESAS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("A empresa consultada nao existe!", resultado.getMessage());
    }

    @Test
    @DisplayName("Listar empresas com sucesso")
    void listarEmpresasComSucesso() throws Exception {
        empresaRepository.save(empresaPadrao);

        String responseJsonString = driver.perform(get(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        List<EmpresaResponseDTO> resultado = objectMapper.readValue(responseJsonString, new TypeReference<List<EmpresaResponseDTO>>() {});

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("Criar empresa com nome vazio")
    void criarEmpresaNomeVazio() throws Exception {
        empresaPostPutRequestDTO.setNome("");

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertAll(
                () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                () -> assertEquals("Nome obrigatorio", resultado.getErrors().get(0))
        );
    }

    @Test
    @DisplayName("Criar empresa com CNPJ vazio")
    void criarEmpresaCnpjVazio() throws Exception {
        empresaPostPutRequestDTO.setCnpj("");

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertAll(
                () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                () -> assertEquals("CNPJ obrigatorio", resultado.getErrors().get(0))
        );
    }

    @Test
    @DisplayName("Alterar empresa não encontrada")
    void alterarEmpresaNaoEncontrada() throws Exception {
        EmpresaPostPutRequestDTO updateDTO = EmpresaPostPutRequestDTO.builder()
                .nome("Empresa Atualizada")
                .cnpj("98.765.432/0001-10")
                .codigoAcesso("654321")
                .senhaAdmin(SENHA_ADMIN)
                .build();

        String responseJsonString = driver.perform(put(URI_EMPRESAS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("A empresa consultada nao existe!", resultado.getMessage());
    }

    @Test
    @DisplayName("Alterar empresa com senha de administrador incorreta")
    void alterarEmpresaSenhaAdminIncorreta() throws Exception {
        Empresa empresaSalva = empresaRepository.save(empresaPadrao);

        EmpresaPostPutRequestDTO updateDTO = EmpresaPostPutRequestDTO.builder()
                .nome("Empresa Atualizada")
                .cnpj(empresaSalva.getCnpj())
                .codigoAcesso(empresaSalva.getCodigoAcesso())
                .senhaAdmin("senha_errada")
                .build();

        String responseJsonString = driver.perform(put(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Senha invalida!", resultado.getMessage());
    }

    @Test
    @DisplayName("Remover empresa não encontrada")
    void removerEmpresaNaoEncontrada() throws Exception {
        EmpresaPostPutRequestDTO deleteDTO = EmpresaPostPutRequestDTO.builder()
                .cnpj(CNPJ_PADRAO)
                .codigoAcesso(CODIGO_ACESSO_PADRAO)
                .senhaAdmin(SENHA_ADMIN)
                .nome("Teste") 
                .build();

        String responseJsonString = driver.perform(delete(URI_EMPRESAS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDTO)))
                .andExpect(status().isNotFound())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("A empresa consultada nao existe!", resultado.getMessage());
    }

    @Test
    @DisplayName("Remover empresa com senha de administrador incorreta")
    void removerEmpresaSenhaAdminIncorreta() throws Exception {
        Empresa empresaSalva = empresaRepository.save(empresaPadrao);

        EmpresaPostPutRequestDTO deleteDTO = EmpresaPostPutRequestDTO.builder()
                .cnpj(empresaSalva.getCnpj())
                .codigoAcesso(empresaSalva.getCodigoAcesso())
                .senhaAdmin("senha_errada")
                .nome(empresaSalva.getNome())
                .build();

        String responseJsonString = driver.perform(delete(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDTO)))
                .andExpect(status().isForbidden())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Senha invalida!", resultado.getMessage());
    }

    @Test
    @DisplayName("Criar empresa com CNPJ duplicado")
    void criarEmpresaCnpjDuplicado() throws Exception {
        empresaRepository.save(empresaPadrao);
        
        empresaPostPutRequestDTO.setCnpj(empresaPadrao.getCnpj());

        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Empresa ja cadastrada!", resultado.getMessage());
    }
    @Test
    @DisplayName("Empresa aprova técnico com sucesso")
    @org.springframework.transaction.annotation.Transactional
    void aprovarTecnicoComSucesso() throws Exception {
        driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/tecnicos/" + tecnicoPadrao.getId())
                        .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .param("aprovacao", "true") 
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Tecnico tecnicoAtualizado = tecnicoRepository.findById(tecnicoPadrao.getId()).orElseThrow();
        assertTrue(tecnicoAtualizado.getEmpresasAprovadoras().contains(empresaPadrao));
        assertTrue(tecnicoAtualizado.isAprovado());
    }    @Test
    @DisplayName("Empresa rejeita técnico com sucesso")
    @org.springframework.transaction.annotation.Transactional
    void rejeitarTecnicoComSucesso() throws Exception {
        tecnicoPadrao.getEmpresasAprovadoras().add(empresaPadrao);
        tecnicoRepository.save(tecnicoPadrao);

        driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/tecnicos/" + tecnicoPadrao.getId())
                        .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .param("aprovacao", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Tecnico tecnicoAtualizado = tecnicoRepository.findById(tecnicoPadrao.getId()).orElseThrow();
        
        assertFalse(tecnicoAtualizado.getEmpresasAprovadoras().contains(empresaPadrao));
        assertFalse(tecnicoAtualizado.isAprovado());
    }

    @Test
    @DisplayName("Tentar aprovar técnico com código de acesso errado")
    void aprovarTecnicoSenhaErrada() throws Exception {
        driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/tecnicos/" + tecnicoPadrao.getId())
                        .param("codigoAcesso", "000000")
                        .param("aprovacao", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CodigoDeAcessoInvalidoException))
                .andDo(print());
    }

    @Test
    @DisplayName("Tentar aprovar técnico inexistente")
    void aprovarTecnicoInexistente() throws Exception {
        driver.perform(put(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/tecnicos/" + 999999)
                        .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .param("aprovacao", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    @DisplayName("Pagamento credito nao aplica desconto")
    void pagamentoCreditoSemDesconto() throws Exception {
        PagamentoRequestDTO pagamentoRequest = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Credito")
                .build();

        String responseJsonString = driver.perform(post(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/1/pagamentos")
                        .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagamentoRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        PagamentoResponseDTO resultado = objectMapper.readValue(responseJsonString, PagamentoResponseDTO.class);

        assertEquals(new BigDecimal("100.00"), resultado.getValorFinal());
    }

    @Test
    @DisplayName("Pagamento debito aplica 2,5% de desconto")
    void pagamentoDebitoComDesconto() throws Exception {
        PagamentoRequestDTO pagamentoRequest = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Debito")
                .build();

        String responseJsonString = driver.perform(post(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/2/pagamentos")
                        .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagamentoRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        PagamentoResponseDTO resultado = objectMapper.readValue(responseJsonString, PagamentoResponseDTO.class);

        assertEquals(new BigDecimal("97.50"), resultado.getValorFinal());
    }

    @Test
    @DisplayName("Pagamento pix aplica 5% de desconto")
    void pagamentoPixComDesconto() throws Exception {
        PagamentoRequestDTO pagamentoRequest = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Pix")
                .build();

        String responseJsonString = driver.perform(post(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/3/pagamentos")
                        .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagamentoRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        PagamentoResponseDTO resultado = objectMapper.readValue(responseJsonString, PagamentoResponseDTO.class);

        assertEquals(new BigDecimal("95.00"), resultado.getValorFinal());
    }

    @Test
    @DisplayName("Pagamento com metodo invalido deve falhar")
    void pagamentoMetodoInvalido() throws Exception {
        PagamentoRequestDTO pagamentoRequest = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Boleto")
                .build();

        String responseJsonString = driver.perform(post(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/4/pagamentos")
                        .param("codigoAcesso", CODIGO_ACESSO_PADRAO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagamentoRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Metodo de pagamento nao suportado", resultado.getMessage());
    }

    @Test
    @DisplayName("Pagamento com codigo de acesso invalido deve ser rejeitado")
    void pagamentoCodigoAcessoInvalido() throws Exception {
        PagamentoRequestDTO pagamentoRequest = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Pix")
                .build();

        String responseJsonString = driver.perform(post(URI_EMPRESAS + "/" + empresaPadrao.getId() + "/chamados/5/pagamentos")
                        .param("codigoAcesso", "000000")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pagamentoRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CodigoDeAcessoInvalidoException))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

        assertEquals("Codigo de acesso invalido!", resultado.getMessage());
    }

}