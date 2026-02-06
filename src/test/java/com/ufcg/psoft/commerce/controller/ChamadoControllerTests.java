package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
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

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do Controlador de Chamados")
public class ChamadoControllerTests {

    final String URI_CHAMADOS = "/chamados";

    @Autowired
    MockMvc driver;
    @Autowired
    ChamadoRepository chamadoRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    EmpresaRepository empresaRepository;
    @Autowired
    ServicoRepository servicoRepository;
    @Autowired
    ObjectMapper objectMapper;

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
                .codigoAcesso("123456")
                .enderecoPrincipal("Rua Base, 100")
                .plano("Basico")
                .build());
        
        clientePremium = clienteRepository.save(Cliente.builder()
                .nome("Teste Premium")
                .codigoAcesso("654321")
                .enderecoPrincipal("Rua VIP, 200")
                .plano("Premium")
                .build());

        servicoComum = servicoRepository.save(Servico.builder()
                .nome("Manutenção Simples")
                .valor(150.00)
                .empresa(empresa)
                .tipo("Basico")
                .build());
        
        servicoExclusivo = servicoRepository.save(Servico.builder()
                .nome("Instalação 24h")
                .valor(300.00)
                .empresa(empresa)
                .tipo("Premium")
                .build());
    }

    @AfterEach
    void tearDown() {
        chamadoRepository.deleteAll();
        servicoRepository.deleteAll();
        empresaRepository.deleteAll();
        clienteRepository.deleteAll();
    }

    @Test
    @DisplayName("Criar chamado Básico com sucesso")
    void criarChamadoBasicoSucesso() throws Exception {
        ChamadoPostPutRequestDTO dto = ChamadoPostPutRequestDTO.builder()
                .servicoId(servicoComum.getId())
                .empresaId(empresa.getId())
                .codigoAcessoCliente(clienteBasico.getCodigoAcesso())
                .build();

        String response = driver.perform(post("/clientes/" + clienteBasico.getId() + "/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        ChamadoResponseDTO result = objectMapper.readValue(response, ChamadoResponseDTO.class);
        
        assertNotNull(result.getId());
        assertEquals(clienteBasico.getEnderecoPrincipal(), result.getEnderecoAtendimento());
    }

    @Test
    @DisplayName("Criar chamado Premium com sucesso")
    void criarChamadoPremiumSucesso() throws Exception {
        ChamadoPostPutRequestDTO dto = ChamadoPostPutRequestDTO.builder()
                .servicoId(servicoExclusivo.getId())
                .empresaId(empresa.getId())
                .codigoAcessoCliente(clientePremium.getCodigoAcesso())
                .enderecoAtendimento("Casa de Praia")
                .build();

        String response = driver.perform(post("/clientes/" + clientePremium.getId() + "/chamados")
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
        Chamado chamado = chamadoRepository.save(Chamado.builder()
                .cliente(clienteBasico)
                .empresa(empresa)
                .servico(servicoComum)
                .status("AGUARDANDO_PAGAMENTO")
                .build());

        driver.perform(put(URI_CHAMADOS + "/" + chamado.getId() + "/pagamento")
                        .param("codigoAcesso", clienteBasico.getCodigoAcesso())
                        .param("metodoPagamento", "PIX")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());

        Chamado chamadoAtualizado = chamadoRepository.findById(chamado.getId()).orElseThrow();
        assertEquals("EM_PROCESSAMENTO", chamadoAtualizado.getStatus());
    }

    @Test
    @DisplayName("Cliente tenta remover chamado com código errado")
    void removerChamadoCodigoErrado() throws Exception {
        Chamado chamado = chamadoRepository.save(Chamado.builder()
                .cliente(clienteBasico)
                .empresa(empresa)
                .servico(servicoComum)
                .build());

        driver.perform(delete(URI_CHAMADOS + "/" + chamado.getId())
                        .param("codigoAcesso", "000000")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());
        
        assertTrue(chamadoRepository.existsById(chamado.getId()));
    }

    @Test
    @DisplayName("Remover chamado com sucesso pelo cliente")
    void removerChamadoSucesso() throws Exception {
        Chamado chamado = chamadoRepository.save(Chamado.builder()
                .cliente(clienteBasico)
                .empresa(empresa)
                .servico(servicoComum)
                .build());

        driver.perform(delete(URI_CHAMADOS + "/" + chamado.getId())
                        .param("codigoAcesso", clienteBasico.getCodigoAcesso())
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
                .build());

        driver.perform(delete(URI_CHAMADOS + "/" + chamado.getId())
                        .param("codigoAcesso", empresa.getCodigoAcesso())
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
                .codigoAcessoCliente(clienteBasico.getCodigoAcesso())
                .enderecoAtendimento("Rua Teste, 123")
                .build();

        driver.perform(post("/clientes/" + clienteBasico.getId() + "/chamados")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof PlanoInvalidoException))
                .andDo(print());
    }
}
