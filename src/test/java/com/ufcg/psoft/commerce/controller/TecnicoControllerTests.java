package com.ufcg.psoft.commerce.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.TecnicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.TecnicoResponseDTO;
import com.ufcg.psoft.commerce.exception.CustomErrorType;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TecnicoControllerTests {
    final String URI_TECNICOS = "/tecnicos";

    @Autowired
    MockMvc driver;

    @Autowired
    TecnicoRepository tecnicoRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    Tecnico tecnico;

    TecnicoPostPutRequestDTO tecnicoPostPutRequestDTO;

    @BeforeEach
    void setup() {
        // Object Mapper suporte para LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        tecnico = tecnicoRepository.save(Tecnico.builder()
                .nome("Tecnico Um da Silva")
                .tipoVeiculo(TipoVeiculo.MOTO)
                .placaVeiculo("ABC1D23")
                .corVeiculo("Verde")
                .acesso("123456")
                .especialidade("Consertos")
                .build()
        );
        tecnicoPostPutRequestDTO = TecnicoPostPutRequestDTO.builder()
                .nome(tecnico.getNome())
                .acesso(tecnico.getAcesso())
                .tipoVeiculo(tecnico.getTipoVeiculo())
                .corVeiculo(tecnico.getCorVeiculo())
                .placaVeiculo(tecnico.getPlacaVeiculo())
                .especialidade(tecnico.getEspecialidade())
                .build();
    }

    @AfterEach
    void tearDown() {
        tecnicoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação de nome")
    class TecnicoVerificacaoNome {

        @Test
        @DisplayName("Quando recuperamos um tecnico com dados válidos")
        void quandoRecuperamosNomeDoTecnicoValido() throws Exception {
            // Act
            String responseJsonString = driver.perform(get(URI_TECNICOS + "/" + tecnico.getId()))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Tecnico resultado = objectMapper.readValue(responseJsonString, Tecnico.TecnicoBuilder.class).build();

            // Assert
            assertEquals("Tecnico Um da Silva", resultado.getNome());
        }

        @Test
        @DisplayName("Quando alteramos o nome do tecnico com dados válidos")
        void quandoAlteramosNomeDoTecnicoValido() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setNome("Tecnico Um Alterado");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Tecnico resultado = objectMapper.readValue(responseJsonString, Tecnico.TecnicoBuilder.class).build();

            // Assert
            assertEquals("Tecnico Um Alterado", resultado.getNome());
        }

        @Test
        @DisplayName("Quando alteramos o nome do tecnico nulo")
        void quandoAlteramosNomeDoTecnicoNulo() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setNome(null);

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Nome obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o nome do tecnico vazio")
        void quandoAlteramosNomeDoTecnicoVazio() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setNome("");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Nome obrigatorio", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação do tipo do veiculo")
    class TecnicoVerificacaoVeiculo {

        @Test
        @DisplayName("Quando alteramos o tipo do veiculo do tecnico com dados válidos")
        void quandoAlteramosTipoVeiculoDoTecnicoValido() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setTipoVeiculo(TipoVeiculo.CARRO);

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            TecnicoResponseDTO resultado = objectMapper.readValue(responseJsonString, TecnicoResponseDTO.TecnicoResponseDTOBuilder.class).build();

            // Assert
            assertEquals(TipoVeiculo.CARRO, resultado.getTipoVeiculo());
        }

        @Test
        @DisplayName("Quando alteramos o tipo do veiculo do tecnico nulo")
        void quandoAlteramosTipoVeiculoDoTecnicoNulo() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setTipoVeiculo(null);

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Tipo veiculo obrigatorio", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação da placa do veiculo")
    class TecnicoVerificacaoPlaca {

        @Test
        @DisplayName("Quando alteramos a placa do veiculo do tecnico com dados válidos")
        void quandoAlteramosPlacaDoTecnicoValida() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setPlacaVeiculo("ABG1D23");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            TecnicoResponseDTO resultado = objectMapper.readValue(responseJsonString, TecnicoResponseDTO.TecnicoResponseDTOBuilder.class).build();

            // Assert
            assertEquals("ABG1D23", resultado.getPlacaVeiculo());
        }

        @Test
        @DisplayName("Quando alteramos placa do veiculo do tecnico nulo")
        void quandoAlteramosTipoVeiculoDoTecnicoNulo() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setPlacaVeiculo(null);

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("placa do veiculo obrigatoria", resultado.getErrors().get(0))
            );
        }


        @Test
        @DisplayName("Quando alteramos placa de veiculo invalida")
        void quandoAlteramosPlacaVeiculoInvalida() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setPlacaVeiculo("abcdefgh");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("placa do veiculoFormatoInvalido", resultado.getErrors().get(0))
            );
        }
    }
    @Nested
    @DisplayName("Conjunto de casos de verificação da cor do veiculo")
    class TecnicoVerificacaoCorVeiculo {

        @Test
        @DisplayName("Quando alteramos a cor do veiculo do tecnico com dados válidos")
        void quandoAlteramosACorDoVeiculoDoTecnicoValido() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setCorVeiculo("branco");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            TecnicoResponseDTO resultado = objectMapper.readValue(responseJsonString, TecnicoResponseDTO.TecnicoResponseDTOBuilder.class).build();

            // Assert
            assertEquals("branco", resultado.getCorVeiculo());
        }

        @Test
        @DisplayName("Quando alteramos a cor do veiculo do tecnico nulo")
        void quandoAlteramosCorVeiculoTecnicoNulo() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setCorVeiculo(null);
            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Cor veiculo obrigatoria", resultado.getErrors().get(0))
            );
        }
        @Test
        @DisplayName("Quando alteramos a corVeiculo do tecnico vazia")
        void quandoAlteramosCorVeiculoDoTecnicoVazio() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setCorVeiculo("");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Cor veiculo obrigatoria", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação da especialidade do tecnico")
    class TecnicoVerificacaoEspecialidade {

        @Test
        @DisplayName("Quando alteramos a especialidade do tecnico com dados válidos")
        void quandoAlteramosEspecialidadeDoTecnicoValido() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setEspecialidade("encanamento");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            TecnicoResponseDTO resultado = objectMapper.readValue(responseJsonString, TecnicoResponseDTO.TecnicoResponseDTOBuilder.class).build();

            // Assert
            assertEquals("encanamento", resultado.getEspecialidade());
        }

        @Test
        @DisplayName("Quando alteramos a especialidade do tecnico nulo")
        void quandoAlteramosEspecialidadeTecnicoNulo() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setEspecialidade(null);
            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Especialidade obrigatoria", resultado.getErrors().get(0))
            );
        }
        @Test
        @DisplayName("Quando alteramos a especialidade do tecnico vazia")
        void quandoAlteramosEspecialidadeDoTecnicoVazio() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setEspecialidade("");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Especialidade obrigatoria", resultado.getErrors().get(0))
            );
        }
    }




    @Nested
    @DisplayName("Conjunto de casos de verificação do código de acesso")
    class TecnicoVerificacaoCodigoAcesso {

        @Test
        @DisplayName("Quando alteramos o código de acesso do tecnico nulo")
        void quandoAlteramosCodigoAcessoDoTecnicoNulo() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setAcesso(null);

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso obrigatorio", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do tecnico mais de 6 digitos")
        void quandoAlteramosCodigoAcessoDoTecnicoMaisDe6Digitos() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setAcesso("1234567");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do tecnico menos de 6 digitos")
        void quandoAlteramosCodigoAcessoDoTecnicoMenosDe6Digitos() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setAcesso("12345");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }

        @Test
        @DisplayName("Quando alteramos o código de acesso do tecnico caracteres não numéricos")
        void quandoAlteramosCodigoAcessoDoTecnicoCaracteresNaoNumericos() throws Exception {
            // Arrange
            tecnicoPostPutRequestDTO.setAcesso("a*c4e@");

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest())
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Erros de validacao encontrados", resultado.getMessage()),
                    () -> assertEquals("Codigo de acesso deve ter exatamente 6 digitos numericos", resultado.getErrors().get(0))
            );
        }
    }

    @Nested
    @DisplayName("Conjunto de casos de verificação dos fluxos básicos API Rest")
    class TecnicoVerificacaoFluxosBasicosApiRest {

        @Test
        @DisplayName("Quando buscamos por todos tecnicos salvos")
        void quandoBuscamosPorTodosTecnicosSalvos() throws Exception {
            // Arrange
            Tecnico tecnico1 = Tecnico.builder()
                    .nome("Tecnico Dois Almeida")
                    .tipoVeiculo(TipoVeiculo.MOTO)
                    .acesso("246810")
                    .placaVeiculo("ABG2D23")
                    .corVeiculo("Amarelo")
                    .especialidade("Pias")
                    .build();
            Tecnico tecnico2 = Tecnico.builder()
                    .nome("Tecnico Tres Almeida")
                    .tipoVeiculo(TipoVeiculo.CARRO)
                    .acesso("123456")
                    .placaVeiculo("BAG5D13")
                    .corVeiculo("Rosa")
                    .especialidade("Carpintaria")
                    .build();
            tecnicoRepository.saveAll(Arrays.asList(tecnico1, tecnico2));
            // Act
            String responseJsonString = driver.perform(get(URI_TECNICOS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk()) // Codigo 200
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            List<Tecnico> resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {
            });

            // Assert
            assertAll(
                    () -> assertEquals(3, resultado.size())
            );
        }

        @Test
        @DisplayName("Quando buscamos um tecnico salvo pelo id")
        void quandoBuscamosPorUmTecnicoSalvo() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(get(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk()) // Codigo 200
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            TecnicoResponseDTO resultado = objectMapper.readValue(responseJsonString, new TypeReference<>() {});

            // Assert
            assertAll(
                    () -> assertEquals(tecnico.getId().longValue(), resultado.getId().longValue()),
                    () -> assertEquals(tecnico.getNome(), resultado.getNome())
            );
        }

        @Test
        @DisplayName("Quando buscamos um tecnico inexistente")
        void quandoBuscamosPorUmTecnicoInexistente() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(get(URI_TECNICOS + "/" + 999999999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O tecnico consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando criamos um novo tecnico com dados válidos")
        void quandoCriarTecnicoValido() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(post(URI_TECNICOS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isCreated()) // Codigo 201
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Tecnico resultado = objectMapper.readValue(responseJsonString, Tecnico.TecnicoBuilder.class).build();

            // Assert
            assertAll(
                    () -> assertNotNull(resultado.getId()),
                    () -> assertEquals(tecnicoPostPutRequestDTO.getNome(), resultado.getNome())
            );

        }

        @Test
        @DisplayName("Quando alteramos o tecnico com dados válidos")
        void quandoAlteramosTecnicoValido() throws Exception {
            // Arrange
            Long tecnicoId = tecnico.getId();
            tecnicoPostPutRequestDTO.setNome("Nome Realmente Alterado");
            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isOk()) // Codigo 200
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            Tecnico resultado = objectMapper.readValue(responseJsonString, Tecnico.TecnicoBuilder.class).build();

            // Assert
            assertAll(
                    () -> assertEquals(resultado.getId().longValue(), tecnicoId),
                    () -> assertEquals(tecnicoPostPutRequestDTO.getNome(), resultado.getNome())
            );
        }

        @Test
        @DisplayName("Quando alteramos o tenico inexistente")
        void quandoAlteramosTecnicoInexistente() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + 99999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso())
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O tecnico consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando alteramos o tecnico passando código de acesso inválido")
        void quandoAlteramosTecnicoCodigoAcessoInvalido() throws Exception {
            // Arrange
            Long tecnicoId = tecnico.getId();

            // Act
            String responseJsonString = driver.perform(put(URI_TECNICOS + "/" + tecnicoId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", "invalido")
                            .content(objectMapper.writeValueAsString(tecnicoPostPutRequestDTO)))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando excluímos um tecnico salvo")
        void quandoExcluimosTecnicoValido() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(delete(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso()))
                    .andExpect(status().isNoContent()) // Codigo 204
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            // Assert
            assertTrue(responseJsonString.isBlank());
        }

        @Test
        @DisplayName("Quando excluímos um tecnico inexistente")
        void quandoExcluimosTecnicoInexistente() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(delete(URI_TECNICOS + "/" + 999999)
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", tecnico.getAcesso()))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("O tecnico consultado nao existe!", resultado.getMessage())
            );
        }

        @Test
        @DisplayName("Quando excluímos um tecnico salvo passando código de acesso inválido")
        void quandoExcluimosTecnicoCodigoAcessoInvalido() throws Exception {
            // Arrange
            // nenhuma necessidade além do setup()

            // Act
            String responseJsonString = driver.perform(delete(URI_TECNICOS + "/" + tecnico.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("acesso", "invalido"))
                    .andExpect(status().isBadRequest()) // Codigo 400
                    .andDo(print())
                    .andReturn().getResponse().getContentAsString();

            CustomErrorType resultado = objectMapper.readValue(responseJsonString, CustomErrorType.class);

            // Assert
            assertAll(
                    () -> assertEquals("Codigo de acesso invalido!", resultado.getMessage())
            );
        }
    }
}



