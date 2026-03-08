package com.ufcg.psoft.commerce.service.tecnico;

import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.StatusDisponibilidade;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.HistoricoDisponibilidadeRepository;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayName("Testes de Serviço - Disponibilidade do Técnico")
class DisponibilidadeTecnicoServiceTests {

    @Autowired
    TecnicoService tecnicoService;

    @Autowired
    TecnicoRepository tecnicoRepository;

    @Autowired
    EmpresaRepository empresaRepository;

    @Autowired
    HistoricoDisponibilidadeRepository historicoDisponibilidadeRepository;

    Tecnico tecnico;
    Empresa empresa;

    private static final String ACESSO_TECNICO = "123456";
    private static final String ACESSO_OUTRO   = "999999";

    @BeforeEach
    void setUp() {
        historicoDisponibilidadeRepository.deleteAll();
        tecnicoRepository.deleteAll();
        empresaRepository.deleteAll();

        empresa = empresaRepository.save(Empresa.builder()
                .nome("Empresa Teste")
                .cnpj("11.111.111/0001-11")
                .codigoAcesso("654321")
                .build());

        tecnico = tecnicoRepository.save(Tecnico.builder()
                .nome("Tecnico Disponivel")
                .especialidade("eletrica")
                .placaVeiculo("ABC1D23")
                .tipoVeiculo(TipoVeiculo.MOTO)
                .corVeiculo("azul")
                .acesso(ACESSO_TECNICO)
                .build());
    }

    @AfterEach
    void tearDown() {
        historicoDisponibilidadeRepository.deleteAll();
        tecnicoRepository.deleteAll();
        empresaRepository.deleteAll();
    }

    @Nested
    @DisplayName("Status inicial do técnico")
    class StatusInicialTests {

        @Test
        @DisplayName("Técnico recém-criado deve ter status DESCANSO por padrão")
        void quandoCriamosUmTecnicoOStatusInicialDeveSerDESCANSO() {
            Tecnico tecnicoCriado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.DESCANSO, tecnicoCriado.getStatusDisponibilidade());
        }

        @Test
        @DisplayName("Técnico aprovado por empresa deve manter status DESCANSO")
        void quandoTecnicoEAprovadoPorEmpresaStatusPermaneceDescanso() {
            tecnico.getEmpresasAprovadoras().add(empresa);
            tecnicoRepository.save(tecnico);

            Tecnico tecnicoAtualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.DESCANSO, tecnicoAtualizado.getStatusDisponibilidade());
        }
    }

    @Nested
    @DisplayName("Alteração de disponibilidade pelo próprio técnico")
    class AlteracaoDisponibilidadeTests {

        @Test
        @DisplayName("Técnico muda de DESCANSO para ATIVO e data/hora é registrada")
        void quandoTecnicoMudaDeDescansoParaAtivoDataHoraEhRegistrada() {
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);

            Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.ATIVO, atualizado.getStatusDisponibilidade());
            assertNotNull(atualizado.getDataUltimaMudancaDisponibilidade());
        }

        @Test
        @DisplayName("Técnico muda de ATIVO para DESCANSO e data/hora é registrada")
        void quandoTecnicoMudaDeAtivoParaDescansoDataHoraEhRegistrada() {
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.DESCANSO);

            Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.DESCANSO, atualizado.getStatusDisponibilidade());
            assertNotNull(atualizado.getDataUltimaMudancaDisponibilidade());
        }

        @Test
        @DisplayName("Após mudar para ATIVO a data/hora deve ser recente (não nula e não futura)")
        void quandoMudaParaAtivoDataHoraDeveSerRecenteENaoFutura() {
            java.time.LocalDateTime antes = java.time.LocalDateTime.now().minusSeconds(1);

            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);

            Tecnico atualizado = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            java.time.LocalDateTime depois = java.time.LocalDateTime.now().plusSeconds(1);

            assertTrue(atualizado.getDataUltimaMudancaDisponibilidade().isAfter(antes));
            assertTrue(atualizado.getDataUltimaMudancaDisponibilidade().isBefore(depois));
        }
    }

    @Nested
    @DisplayName("Restrição de autorização")
    class AutorizacaoTests {

        @Test
        @DisplayName("Outro usuário com acesso diferente não pode alterar disponibilidade do técnico")
        void quandoOutroUsuarioTentaAlterarDisponibilidadeDeveSerRejeitado() {
            assertThrows(RuntimeException.class, () ->
                    tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_OUTRO, StatusDisponibilidade.ATIVO));
        }

        @Test
        @DisplayName("Acesso inválido não altera o status persistido")
        void quandoAcessoInvalidoStatusNaoEhAlterado() {
            assertThrows(RuntimeException.class, () ->
                    tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_OUTRO, StatusDisponibilidade.ATIVO));

            Tecnico semMudanca = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.DESCANSO, semMudanca.getStatusDisponibilidade());
        }
    }

    @Nested
    @DisplayName("Histórico de mudanças de disponibilidade")
    class HistoricoDisponibilidadeTests {

        @Test
        @DisplayName("Cada mudança de disponibilidade gera uma entrada no histórico")
        void quandoAlteramosDisponibilidadeUmaEntradaEhSalvaNoHistorico() {
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);

            var historico = historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId());
            assertEquals(1, historico.size());
            assertEquals(StatusDisponibilidade.ATIVO, historico.get(0).getNovoStatus());
            assertNotNull(historico.get(0).getDataHora());
        }

        @Test
        @DisplayName("Múltiplas mudanças geram múltiplas entradas no histórico na ordem correta")
        void quandoAlteramosDisponibilidadeVariasVezesHistoricoRegistraTodasAsEntradas() {
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.DESCANSO);
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);

            var historico = historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId());
            assertEquals(3, historico.size());
            // mais recente primeiro
            assertEquals(StatusDisponibilidade.ATIVO,    historico.get(0).getNovoStatus());
            assertEquals(StatusDisponibilidade.DESCANSO, historico.get(1).getNovoStatus());
            assertEquals(StatusDisponibilidade.ATIVO,    historico.get(2).getNovoStatus());
        }

        @Test
        @DisplayName("Entrada do histórico contém data/hora preenchida automaticamente e não nula")
        void quandoHistoricoEhSalvoDataHoraEhPreenchidaAutomaticamente() {
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);

            var historico = historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId());
            assertFalse(historico.isEmpty());
            historico.forEach(entrada -> assertNotNull(entrada.getDataHora()));
        }

        @Test
        @DisplayName("Histórico registra o id do técnico correto em cada entrada")
        void quandoHistoricoEhSalvoIdDoTecnicoEhCorreto() {
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);

            var historico = historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId());
            historico.forEach(entrada -> assertEquals(tecnico.getId(), entrada.getTecnicoId()));
        }
    }

    @Nested
    @DisplayName("Restrição de atribuição de chamado a técnico em DESCANSO")
    class AtribuicaoComStatusDescansoTests {

        @Test
        @DisplayName("Técnico em DESCANSO não pode ser atribuído — validação na camada de serviço")
        void quandoTecnicoEstaEmDescansoNaoPodeSerAtribuido() {
            // garante status DESCANSO (padrão)
            Tecnico emDescanso = tecnicoRepository.findById(tecnico.getId()).orElseThrow();
            assertEquals(StatusDisponibilidade.DESCANSO, emDescanso.getStatusDisponibilidade());

            assertThrows(RuntimeException.class, () ->
                    tecnicoService.validarTecnicoDisponivel(emDescanso.getId()));
        }

        @Test
        @DisplayName("Técnico em ATIVO pode ser atribuído — validação não lança exceção")
        void quandoTecnicoEstaAtivoValidacaoNaoLancaExcecao() {
            tecnicoService.alterarDisponibilidade(tecnico.getId(), ACESSO_TECNICO, StatusDisponibilidade.ATIVO);

            assertDoesNotThrow(() ->
                    tecnicoService.validarTecnicoDisponivel(tecnico.getId()));
        }
    }
}
