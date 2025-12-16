package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
    }

    @AfterEach
    void tearDown() {
        empresaRepository.deleteAll();
    }

    @Test
    @DisplayName("Criar empresa com sucesso")
    void criarEmpresaComSucesso() throws Exception {
        String responseJsonString = driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Empresa resultado = objectMapper.readValue(responseJsonString, Empresa.class);
        
        assertNotNull(resultado.getId());
        assertEquals(empresaPostPutRequestDTO.getNome(), resultado.getNome());
        assertEquals(empresaPostPutRequestDTO.getCnpj(), resultado.getCnpj());
    }

    @Test
    @DisplayName("Criar empresa com código de acesso inválido (curto demais)")
    void criarEmpresaCodigoAcessoCurto() throws Exception {
        empresaPostPutRequestDTO.setCodigoAcesso("123");

        driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Codigo de acesso deve ter exatamente 6 digitos"))
                .andDo(print());
    }

    @Test
    @DisplayName("Criar empresa com código de acesso inválido (não numérico)")
    void criarEmpresaCodigoAcessoNaoNumerico() throws Exception {
        empresaPostPutRequestDTO.setCodigoAcesso("abcdef");

        driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Codigo de acesso deve conter apenas digitos"))
                .andDo(print());
    }

    @Test
    @DisplayName("Criar empresa sem senha de administrador")
    void criarEmpresaSemSenhaAdmin() throws Exception {
        empresaPostPutRequestDTO.setSenhaAdmin("");

        driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Senha de administrador obrigatoria"))
                .andDo(print());
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

        Empresa resultado = objectMapper.readValue(responseJsonString, Empresa.class);
        
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

        driver.perform(put(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CodigoDeAcessoInvalidoException))
                .andExpect(jsonPath("$.message").value("Codigo de acesso invalido!"))
                .andDo(print());
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

        driver.perform(delete(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof CodigoDeAcessoInvalidoException))
                .andExpect(jsonPath("$.message").value("Codigo de acesso invalido!"))
                .andDo(print());
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

        driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Senha invalida!"))
                .andDo(print());
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

        Empresa resultado = objectMapper.readValue(responseJsonString, Empresa.class);

        assertEquals(empresaSalva.getNome(), resultado.getNome());
        assertEquals(empresaSalva.getCnpj(), resultado.getCnpj());
    }

    @Test
    @DisplayName("Buscar empresa por id não encontrada")
    void buscarEmpresaPorIdNaoEncontrada() throws Exception {
        driver.perform(get(URI_EMPRESAS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("A empresa consultada nao existe!"))
                .andDo(print());
    }

    @Test
    @DisplayName("Listar empresas com sucesso")
    void listarEmpresasComSucesso() throws Exception {
        empresaRepository.save(empresaPadrao);

        driver.perform(get(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("Criar empresa com nome vazio")
    void criarEmpresaNomeVazio() throws Exception {
        empresaPostPutRequestDTO.setNome("");

        driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("Nome obrigatorio"))
                .andDo(print());
    }

    @Test
    @DisplayName("Criar empresa com CNPJ vazio")
    void criarEmpresaCnpjVazio() throws Exception {
        empresaPostPutRequestDTO.setCnpj("");

        driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").value("CNPJ obrigatorio"))
                .andDo(print());
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

        driver.perform(put(URI_EMPRESAS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("A empresa consultada nao existe!"))
                .andDo(print());
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

        driver.perform(put(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Senha invalida!"))
                .andDo(print());
    }

    @Test
    @DisplayName("Remover empresa não encontrada")
    void removerEmpresaNaoEncontrada() throws Exception {
        EmpresaPostPutRequestDTO deleteDTO = EmpresaPostPutRequestDTO.builder()
                .cnpj(CNPJ_PADRAO)
                .codigoAcesso(CODIGO_ACESSO_PADRAO)
                .senhaAdmin(SENHA_ADMIN)
                .build();

        driver.perform(delete(URI_EMPRESAS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("A empresa consultada nao existe!"))
                .andDo(print());
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

        driver.perform(delete(URI_EMPRESAS + "/" + empresaSalva.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deleteDTO)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Senha invalida!"))
                .andDo(print());
    }

    @Test
    @DisplayName("Criar empresa com CNPJ duplicado")
    void criarEmpresaCnpjDuplicado() throws Exception {
        empresaRepository.save(empresaPadrao);
        
        empresaPostPutRequestDTO.setCnpj(empresaPadrao.getCnpj());

        driver.perform(post(URI_EMPRESAS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empresaPostPutRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Empresa ja cadastrada!"))
                .andDo(print());
    }
}
