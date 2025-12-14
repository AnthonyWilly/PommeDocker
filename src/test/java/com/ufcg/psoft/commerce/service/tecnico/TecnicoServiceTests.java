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

    Tecnico tecnico;
    TecnicoPostPutRequestDTO tecnicoPostPutRequestDTO;

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

        tecnicoPostPutRequestDTO = TecnicoPostPutRequestDTO.builder()
                .nome(tecnico.getNome())
                .especialidade(tecnico.getEspecialidade())
                .placaVeiculo(tecnico.getPlacaVeiculo())
                .tipoVeiculo(tecnico.getTipoVeiculo())
                .corVeiculo(tecnico.getCorVeiculo())
                .acesso(CODIGO_ACESSO_OK)
                .build();
    }

    @Test
    @DisplayName("O técnico não deve exibir o código de acesso nas leituras")
    void testCriarTecnicoENaoExibirCodigoNasLeituras() throws Exception {
        TecnicoPostPutRequestDTO req = TecnicoPostPutRequestDTO.builder()
                .nome("Técnica Ana Souza")
                .especialidade("hidráulica")
                .placaVeiculo("ABC3D09")
                .tipoVeiculo(TipoVeiculo.MOTO)
                .corVeiculo("azul")
                .acesso("654321")
                .build();

        TecnicoResponseDTO res = tecnicoService.criar(req);

        assertNotNull(res);
        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Leitura de técnico retorna dados e não expõe código")
    void testRetornarTecnicoValido() {
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
    void testRetornarLista_semCodigoAcesso() throws Exception {
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
    void testListarPorNomeFiltro() {
        List<TecnicoResponseDTO> res = tecnicoService.listarPorNome("Silva");
        assertNotNull(res);
        assertTrue(res.size() >= 1);
    }

    @Test
    @DisplayName("Deve alterar o código quando o código fornecido for o correto.")
    void testarAlterarCodigoCorreto() throws Exception {
        TecnicoPostPutRequestDTO req = TecnicoPostPutRequestDTO.builder()
                .nome("Técnico Um Alterado")
                .especialidade("elétrica")
                .placaVeiculo("ABO1E32")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .corVeiculo("branco")
                .acesso(CODIGO_ACESSO_OK)
                .build();

        TecnicoResponseDTO res = tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_OK, req);

        assertNotNull(res);

        Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
        assertEquals("Técnico Um Alterado", atualizado.getNome());
        assertEquals("ABO1E32", atualizado.getPlacaVeiculo());

        String json = objectMapper.writeValueAsString(res);
        assertFalse(json.contains("acesso"));
    }

    @Test
    @DisplayName("Quando o código estiver errado, não deve ser possível alterar o código")
    void testAlterarCodigoErrado() {
        TecnicoPostPutRequestDTO req = TecnicoPostPutRequestDTO.builder()
                .nome("Nao Deve Alterar")
                .especialidade("elétrica")
                .placaVeiculo("RGB1P12")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .corVeiculo("branco")
                .acesso(CODIGO_ACESSO_OK)
                .build();

        assertThrows(RuntimeException.class,
                () -> tecnicoService.alterar(tecnico.getId(), CODIGO_ACESSO_ERRADO, req));
    }

    @Test
    @DisplayName("Quando dado o código correto, deve ser possível remover o tecnico")
    void testRemoverCodigoCorreto() {
        tecnicoService.remover(tecnico.getId(), CODIGO_ACESSO_OK);
        assertFalse(tecnicoRepository.existsById(tecnico.getId()));
    }

    @Test
    @DisplayName("Não deve ser possível remover quando o código estiver errado")
    void testRemoverCodigoErrado() {
        assertThrows(RuntimeException.class,
                () -> tecnicoService.remover(tecnico.getId(), CODIGO_ACESSO_ERRADO));
        assertTrue(tecnicoRepository.existsById(tecnico.getId()));
    }
}
