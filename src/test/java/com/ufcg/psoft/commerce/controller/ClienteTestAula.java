package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.ClienteResponseDTO;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testes do controlador de Clientes")
public class ClienteTestAula {

    final String URI_CLIENTES = "/clientes";

    @Autowired
    MockMvc driver;

    @Autowired
    ClienteRepository clienteRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    List<ClienteResponseDTO> clientesDTO = new ArrayList<>();

    @BeforeEach
    void setup() {
        // Object Mapper suporte para LocalDateTime
        Cliente cliente1 = clienteRepository.save(
                Cliente.builder()
                        .nome("Cliente")
                        .endereco("Rua 123")
                        .codigo("123456")
                        .planoAtual(Plano.BASICO)
                        .dataCobranca(LocalDate.now())
                        .build()
        );

        Cliente cliente2 = clienteRepository.save(
                Cliente.builder()
                        .nome("Clienta")
                        .endereco("Rua 234")
                        .codigo("123456")
                        .planoAtual(Plano.BASICO)
                        .dataCobranca(LocalDate.now())
                        .build()
        );

        ClienteResponseDTO r1 = ClienteResponseDTO.builder()
                .id(cliente1.getId())
                .nome(cliente1.getNome())
                .endereco(cliente1.getEndereco())
                .planoAtual(cliente1.getPlanoAtual())
                .build();

        clientesDTO = new ArrayList<>();
        clientesDTO.add(r1);
    }

    @AfterEach
    void tearDown() {
        clienteRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de teste da aula")
    class ClienteVerificacaoNome {

        @Test
        @DisplayName("Quando recuperamos clientes")
        void quandoRecuperamosClientesValidos() throws Exception {

            String stringBusca = "Cliente";
            // Act
            String responseJsonString = driver.perform(get(URI_CLIENTES)
                            .param("nome", stringBusca))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            String expectedResult = objectMapper
                    .writeValueAsString(clientesDTO);

            System.out.println(expectedResult);
            System.out.println(responseJsonString);
            // Assert
            assertEquals(expectedResult, responseJsonString);
        }

    }
}


