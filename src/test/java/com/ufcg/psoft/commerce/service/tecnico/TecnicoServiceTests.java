package com.ufcg.psoft.commerce.service.tecnico;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ufcg.psoft.commerce.dto.TecnicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.TecnicoResponseDTO;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TecnicoServiceTests {

    @Autowired
    TecnicoService tecnicoService;

    @Autowired
    TecnicoRepository tecnicoRepository;

    ObjectMapper objectMapper = new ObjectMapper();

    TecnicoPostPutRequestDTO tecnicoDTO;
    Tecnico tecnico;

    private static final String CODIGO_ACESSO_OK = "123456";
    private static final String CODIGO_ACESSO_ERRADO = "000000";

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        tecnicoRepository.deleteAll();
        tecnico = tecnicoRepository.save(Tecnico.builder()
                .nome("Técnico Um da Silva")
                .especialidade("elétrica")
                .placaVeiculo("ABC1D34")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .corVeiculo("preto")
                .acesso(CODIGO_ACESSO_OK)
                .build()
        );
        tecnicoDTO = TecnicoPostPutRequestDTO.builder()
                .nome("Técnico Um da Silva")
                .especialidade("elétrica")
                .placaVeiculo("ABC1D34")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .corVeiculo("preto")
                .acesso(CODIGO_ACESSO_OK)
                .build();
    }

    @Test
    @DisplayName("O técnico não deve exibir o código de acesso nas leituras")
    void quandoCriamosTecnicoNaoExibimosCodigoNasLeituras() throws Exception {

        TecnicoResponseDTO res = tecnicoService.criar(tecnicoDTO);
        assertNotNull(res);
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Leitura de técnico retorna dados e não expõe código")
    void quandoRetornamosTecnicoValido() {
        TecnicoResponseDTO res = tecnicoService.recuperar(tecnico.getId());
        assertNotNull(res);
        assertEquals(tecnico.getId(), res.getId());
        assertEquals(tecnico.getNome(), res.getNome());
        assertEquals(tecnico.getEspecialidade(), res.getEspecialidade());
        assertEquals(tecnico.getPlacaVeiculo(), res.getPlacaVeiculo());
        assertEquals(tecnico.getTipoVeiculo(), res.getTipoVeiculo());
        assertEquals(tecnico.getCorVeiculo(), res.getCorVeiculo());
    }

    @Test
    @DisplayName("Listar técnicos retorna lista e não expõe código de acesso")
    void quandoRetornamosListaSemCodigoAcesso() throws Exception {
        List<TecnicoResponseDTO> res = tecnicoService.listar();

        assertNotNull(res);
        assertFalse(res.isEmpty());

        for (TecnicoResponseDTO dto : res) {
            assertNotNull(dto.getId());
            assertNotNull(dto.getNome());
            assertNotNull(dto.getEspecialidade());
            assertNotNull(dto.getPlacaVeiculo());
            assertNotNull(dto.getTipoVeiculo());
            assertNotNull(dto.getCorVeiculo());
        }

        String json = objectMapper.writeValueAsString(res);
        var root = objectMapper.readTree(json);

        assertTrue(root.isArray());
        for (var node : root) {
            assertNull(node.get("acesso"));}
    }

    @Test
    @DisplayName("Deve listar nome por filtro")
    void quandoListamosPorNomeFiltro() {
        List<TecnicoResponseDTO> res = tecnicoService.listarPorNome("Silva");
        assertNotNull(res);
        assertTrue(res.size() >= 1);
    }

    @Test
    @DisplayName("Deve alterar nome quando o código fornecido for o correto.")
    void quandoAlteramosTecnicoCodigoCorreto() throws Exception {
        tecnicoDTO.setNome("Técnico Um da Silva");

        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);

        assertNotNull(res);

        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals("Técnico Um da Silva", atualizado.getNome());
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Quando o código estiver errado, não deve ser possível alterar o código")
    void quandoAlteramosCodigoErrado() {
        assertThrows(RuntimeException.class,
                () -> tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_ERRADO, tecnicoDTO));
    }

    @Test
    @DisplayName("Deve alterar o código quando o código fornecido for o correto.")
    void quandoAlteramosCodigoCorreto() throws Exception {
        tecnicoDTO.setAcesso("654321");

        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);
        assertNotNull(res);
        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals("654321", atualizado.getAcesso());
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Quando dado o código correto, deve ser possível remover o tecnico")
    void quandoRemovemosComCodigoCorreto() {
        tecnicoService.remover(tecnico.getId(), CODIGO_ACESSO_OK);
        assertFalse(tecnicoRepository.existsById(tecnico.getId()));
    }

    @Test
    @DisplayName("Não deve ser possível remover quando o código estiver errado")
    void quandoRemovemosComCodigoErrado() {
        assertThrows(RuntimeException.class,
                () -> tecnicoService.remover(tecnico.getId(), CODIGO_ACESSO_ERRADO));
        assertTrue(tecnicoRepository.existsById(tecnico.getId()));
    }

    @Test
    @DisplayName("Deve alterar apenas o código de acesso mantendo os demais campos inalterados.")
    void quandoAlteramosApenasCodigoAcesso() throws Exception {
        String nomeOriginal = tecnico.getNome();
        String especialidadeOriginal = tecnico.getEspecialidade();
        String placaOriginal = tecnico.getPlacaVeiculo();
        TipoVeiculo tipoVeiculoOriginal = tecnico.getTipoVeiculo();
        String corVeiculoOriginal = tecnico.getCorVeiculo();

        String novoCodigoAcesso = "183476";
        tecnicoDTO.setAcesso(novoCodigoAcesso);
        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);
        assertNotNull(res);
        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals(novoCodigoAcesso, atualizado.getAcesso());
        assertEquals(nomeOriginal, atualizado.getNome());
        assertEquals(especialidadeOriginal, atualizado.getEspecialidade());
        assertEquals(placaOriginal, atualizado.getPlacaVeiculo());
        assertEquals(tipoVeiculoOriginal, atualizado.getTipoVeiculo());
        assertEquals(corVeiculoOriginal, atualizado.getCorVeiculo());

        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Deve alterar apenas a placa do veiculo mantendo os demais campos inalterados.")
    void quandoAlteramosApenasPlacaVeiculo() throws Exception {
        String nomeOriginal = tecnico.getNome();
        String especialidadeOriginal = tecnico.getEspecialidade();
        TipoVeiculo tipoVeiculoOriginal = tecnico.getTipoVeiculo();
        String corVeiculoOriginal = tecnico.getCorVeiculo();
        String acessoOriginal = tecnico.getAcesso();

        String novaPlacaVeiculo = "ABG1E34";
        tecnicoDTO.setPlacaVeiculo(novaPlacaVeiculo);
        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);
        assertNotNull(res);
        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals(novaPlacaVeiculo, atualizado.getPlacaVeiculo());
        assertEquals(nomeOriginal, atualizado.getNome());
        assertEquals(especialidadeOriginal, atualizado.getEspecialidade());
        assertEquals(tipoVeiculoOriginal, atualizado.getTipoVeiculo());
        assertEquals(corVeiculoOriginal, atualizado.getCorVeiculo());
        assertEquals(acessoOriginal, atualizado.getAcesso());
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Deve alterar apenas o nome mantendo os demais campos inalterados.")
    void quandoAlteramosApenasNome() throws Exception {
        String especialidadeOriginal = tecnico.getEspecialidade();
        String placaOriginal = tecnico.getPlacaVeiculo();
        TipoVeiculo tipoVeiculoOriginal = tecnico.getTipoVeiculo();
        String corVeiculoOriginal = tecnico.getCorVeiculo();
        String acessoOriginal = tecnico.getAcesso();

        String novoNome = "Tecnico Um Alterado";
        tecnicoDTO.setNome(novoNome);
        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);
        assertNotNull(res);
        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals(novoNome, atualizado.getNome());
        assertEquals(placaOriginal, atualizado.getPlacaVeiculo());
        assertEquals(especialidadeOriginal, atualizado.getEspecialidade());
        assertEquals(tipoVeiculoOriginal, atualizado.getTipoVeiculo());
        assertEquals(corVeiculoOriginal, atualizado.getCorVeiculo());
        assertEquals(acessoOriginal, atualizado.getAcesso());
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Deve alterar apenas a escpecialidade mantendo os demais campos inalterados.")
    void quandoAlteramosApenasEspecialidade() throws Exception {
        String nomeOriginal = tecnico.getNome();
        String placaOriginal = tecnico.getPlacaVeiculo();
        TipoVeiculo tipoVeiculoOriginal = tecnico.getTipoVeiculo();
        String corVeiculoOriginal = tecnico.getCorVeiculo();
        String acessoOriginal = tecnico.getAcesso();

        String novaEspecialidade = "Cozinhar";
        tecnicoDTO.setEspecialidade(novaEspecialidade);

        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);
        assertNotNull(res);
        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals(nomeOriginal, atualizado.getNome());
        assertEquals(placaOriginal, atualizado.getPlacaVeiculo());
        assertEquals(novaEspecialidade, atualizado.getEspecialidade());
        assertEquals(tipoVeiculoOriginal, atualizado.getTipoVeiculo());
        assertEquals(corVeiculoOriginal, atualizado.getCorVeiculo());
        assertEquals(acessoOriginal, atualizado.getAcesso());
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Deve alterar apenas a cor do veiculo mantendo os demais campos inalterados.")
    void quandoAlteramosApenasCorVeiculo() throws Exception {
        String nomeOriginal = tecnico.getNome();
        String placaOriginal = tecnico.getPlacaVeiculo();
        TipoVeiculo tipoVeiculoOriginal = tecnico.getTipoVeiculo();
        String especialidadeOriginal = tecnico.getEspecialidade();
        String acessoOriginal = tecnico.getAcesso();

        String novaCor = "verde";
        tecnicoDTO.setCorVeiculo(novaCor);
        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);
        assertNotNull(res);
        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals(nomeOriginal, atualizado.getNome());
        assertEquals(placaOriginal, atualizado.getPlacaVeiculo());
        assertEquals(especialidadeOriginal, atualizado.getEspecialidade());
        assertEquals(tipoVeiculoOriginal, atualizado.getTipoVeiculo());
        assertEquals(novaCor, atualizado.getCorVeiculo());
        assertEquals(acessoOriginal, atualizado.getAcesso());
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Deve alterar apenas o tipo do veiculo mantendo os demais campos inalterados.")
    void quandoAlteramosApenasTipoVeiculo() throws Exception {
        String nomeOriginal = tecnico.getNome();
        String placaOriginal = tecnico.getPlacaVeiculo();
        String corVeiculoOriginal = tecnico.getCorVeiculo();
        String especialidadeOriginal = tecnico.getEspecialidade();
        String acessoOriginal = tecnico.getAcesso();

        TipoVeiculo novoTipo = TipoVeiculo.MOTO;
        tecnicoDTO.setTipoVeiculo(novoTipo);
        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, tecnicoDTO);
        assertNotNull(res);
        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals(nomeOriginal, atualizado.getNome());
        assertEquals(placaOriginal, atualizado.getPlacaVeiculo());
        assertEquals(especialidadeOriginal, atualizado.getEspecialidade());
        assertEquals(novoTipo, atualizado.getTipoVeiculo());
        assertEquals(corVeiculoOriginal, atualizado.getCorVeiculo());
        assertEquals(acessoOriginal, atualizado.getAcesso());
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }





}
