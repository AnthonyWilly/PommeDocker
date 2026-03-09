package com.ufcg.psoft.commerce.service.tecnico;

import static org.junit.jupiter.api.Assertions.*;

import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.StatusDisponibilidade;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.model.TipoVeiculo;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.HistoricoDisponibilidadeRepository;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    private static final String ACESSO_OUTRO = "999999";

    @BeforeEach
    void setUp() {
        historicoDisponibilidadeRepository.deleteAll();
        tecnicoRepository.deleteAll();
        empresaRepository.deleteAll();

        empresa = empresaRepository.save(
            Empresa.builder()
                .nome("Empresa Teste")
                .cnpj("11.111.111/0001-11")
                .codigoAcesso("654321")
                .build()
        );

        tecnico = tecnicoRepository.save(
            Tecnico.builder()
                .nome("Tecnico Disponivel")
                .especialidade("eletrica")
                .placaVeiculo("ABC1D23")
                .tipoVeiculo(TipoVeiculo.MOTO)
                .corVeiculo("azul")
                .acesso(ACESSO_TECNICO)
                .build()
        );
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
            Tecnico tecnicoCriado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.DESCANSO,
                tecnicoCriado.getStatusDisponibilidade()
            );
        }

        @Test
        @DisplayName("Técnico aprovado por empresa deve manter status DESCANSO")
        void quandoTecnicoEAprovadoPorEmpresaStatusPermaneceDescanso() {
            tecnico.getEmpresasAprovadoras().add(empresa);
            tecnicoRepository.save(tecnico);

            Tecnico tecnicoAtualizado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.DESCANSO,
                tecnicoAtualizado.getStatusDisponibilidade()
            );
        }

        @Test
        @DisplayName("Status DESCANSO inicial não é disponível para atribuição")
        void quandoTecnicoIniciaEmDescansoNaoEhDisponivel() {
            Tecnico tecnicoCriado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertFalse(
                tecnicoCriado.getStatusDisponibilidade().isDisponivel()
            );
        }
    }

    @Nested
    @DisplayName("Alteração manual de disponibilidade pelo próprio técnico")
    class AlteracaoManualTests {

        @Test
        @DisplayName(
            "Técnico muda de DESCANSO para ATIVO e data/hora é registrada"
        )
        void quandoTecnicoMudaDeDescansoParaAtivoDataHoraEhRegistrada() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            Tecnico atualizado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.ATIVO,
                atualizado.getStatusDisponibilidade()
            );
            assertNotNull(atualizado.getDataUltimaMudancaDisponibilidade());
        }

        @Test
        @DisplayName(
            "Técnico muda de ATIVO para DESCANSO e data/hora é registrada"
        )
        void quandoTecnicoMudaDeAtivoParaDescansoDataHoraEhRegistrada() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.DESCANSO
            );

            Tecnico atualizado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.DESCANSO,
                atualizado.getStatusDisponibilidade()
            );
            assertNotNull(atualizado.getDataUltimaMudancaDisponibilidade());
        }

        @Test
        @DisplayName("Técnico não pode mudar manualmente para OCUPADO")
        void quandoTecnicoTentaMudarManualmenteParaOcupadoDeveSerRejeitado() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            assertThrows(RuntimeException.class, () ->
                tecnicoService.alterarDisponibilidade(
                    tecnico.getId(),
                    ACESSO_TECNICO,
                    StatusDisponibilidade.OCUPADO
                )
            );
        }

        @Test
        @DisplayName(
            "Técnico em OCUPADO não pode mudar manualmente para DESCANSO"
        )
        void quandoTecnicoEstaOcupadoNaoPodeMudarManualmenteParaDescanso() {
            tecnicoRepository.save(
                tecnico
                    .toBuilder()
                    .statusDisponibilidade(StatusDisponibilidade.OCUPADO)
                    .build()
            );

            assertThrows(RuntimeException.class, () ->
                tecnicoService.alterarDisponibilidade(
                    tecnico.getId(),
                    ACESSO_TECNICO,
                    StatusDisponibilidade.DESCANSO
                )
            );
        }

        @Test
        @DisplayName(
            "Após mudar para ATIVO a data/hora deve ser recente e não futura"
        )
        void quandoMudaParaAtivoDataHoraDeveSerRecenteENaoFutura() {
            LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            Tecnico atualizado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            LocalDateTime depois = LocalDateTime.now().plusSeconds(1);

            assertTrue(
                atualizado.getDataUltimaMudancaDisponibilidade().isAfter(antes)
            );
            assertTrue(
                atualizado
                    .getDataUltimaMudancaDisponibilidade()
                    .isBefore(depois)
            );
        }
    }

    @Nested
    @DisplayName("Idempotência — transição para o mesmo status")
    class IdempotenciaTests {

        @Test
        @DisplayName(
            "Solicitar DESCANSO quando já está em DESCANSO não gera erro nem entrada no histórico"
        )
        void quandoJaEstaEmDescansoSolicitarDescansoIgnorasilenciosamente() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.DESCANSO
            );

            Tecnico atualizado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.DESCANSO,
                atualizado.getStatusDisponibilidade()
            );

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            assertTrue(historico.isEmpty());
        }

        @Test
        @DisplayName(
            "Solicitar ATIVO quando já está em ATIVO não gera erro nem entrada no histórico"
        )
        void quandoJaEstaEmAtivoSolicitarAtivoIgnoraSilenciosamente() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );
            int entradasAntesDoSegundoChamado =
                historicoDisponibilidadeRepository
                    .findAllByTecnicoIdOrderByDataHoraDesc(tecnico.getId())
                    .size();

            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            assertEquals(entradasAntesDoSegundoChamado, historico.size());
        }
    }

    @Nested
    @DisplayName("Transições automáticas do sistema")
    class TransicoesAutomaticasTests {

        @Test
        @DisplayName(
            "Ao atribuir técnico ATIVO a um chamado o status muda para OCUPADO automaticamente"
        )
        void quandoTecnicoAtivoEhAtribuidoAUmChamadoStatusMudaParaOcupado() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            tecnicoService.marcarComoOcupado(tecnico.getId());

            Tecnico atualizado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.OCUPADO,
                atualizado.getStatusDisponibilidade()
            );
        }

        @Test
        @DisplayName(
            "Ao concluir um chamado o técnico OCUPADO volta para ATIVO automaticamente"
        )
        void quandoChamadoEhConcluidoTecnicoOcupadoVoltaParaAtivo() {
            tecnicoRepository.save(
                tecnico
                    .toBuilder()
                    .statusDisponibilidade(StatusDisponibilidade.OCUPADO)
                    .build()
            );

            tecnicoService.marcarComoAtivo(tecnico.getId());

            Tecnico atualizado = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.ATIVO,
                atualizado.getStatusDisponibilidade()
            );
        }

        @Test
        @DisplayName(
            "marcarComoOcupado registra entrada no histórico com status OCUPADO"
        )
        void quandoTecnicoEhMarcadoComoOcupadoHistoricoEhAtualizado() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );
            tecnicoService.marcarComoOcupado(tecnico.getId());

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            assertEquals(
                StatusDisponibilidade.OCUPADO,
                historico.get(0).getNovoStatus()
            );
        }

        @Test
        @DisplayName(
            "marcarComoAtivo registra entrada no histórico com status ATIVO"
        )
        void quandoTecnicoEhMarcadoComoAtivoHistoricoEhAtualizado() {
            tecnicoRepository.save(
                tecnico
                    .toBuilder()
                    .statusDisponibilidade(StatusDisponibilidade.OCUPADO)
                    .build()
            );
            tecnicoService.marcarComoAtivo(tecnico.getId());

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            assertEquals(
                StatusDisponibilidade.ATIVO,
                historico.get(0).getNovoStatus()
            );
        }
    }

    @Nested
    @DisplayName("Restrição de autorização")
    class AutorizacaoTests {

        @Test
        @DisplayName(
            "Outro usuário com acesso diferente não pode alterar disponibilidade do técnico"
        )
        void quandoOutroUsuarioTentaAlterarDisponibilidadeDeveSerRejeitado() {
            assertThrows(RuntimeException.class, () ->
                tecnicoService.alterarDisponibilidade(
                    tecnico.getId(),
                    ACESSO_OUTRO,
                    StatusDisponibilidade.ATIVO
                )
            );
        }

        @Test
        @DisplayName("Acesso inválido não altera o status persistido")
        void quandoAcessoInvalidoStatusNaoEhAlterado() {
            assertThrows(RuntimeException.class, () ->
                tecnicoService.alterarDisponibilidade(
                    tecnico.getId(),
                    ACESSO_OUTRO,
                    StatusDisponibilidade.ATIVO
                )
            );

            Tecnico semMudanca = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.DESCANSO,
                semMudanca.getStatusDisponibilidade()
            );
        }
    }

    @Nested
    @DisplayName("Histórico de mudanças de disponibilidade")
    class HistoricoDisponibilidadeTests {

        @Test
        @DisplayName(
            "Cada mudança de disponibilidade gera uma entrada no histórico"
        )
        void quandoAlteramosDisponibilidadeUmaEntradaEhSalvaNoHistorico() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            assertEquals(1, historico.size());
            assertEquals(
                StatusDisponibilidade.ATIVO,
                historico.get(0).getNovoStatus()
            );
            assertNotNull(historico.get(0).getDataHora());
        }

        @Test
        @DisplayName(
            "Ciclo completo DESCANSO→ATIVO→OCUPADO→ATIVO gera quatro entradas no histórico"
        )
        void quandoCicloCompletoDeTransicoesHistoricoRegistraTodasAsEntradas() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );
            tecnicoService.marcarComoOcupado(tecnico.getId());
            tecnicoService.marcarComoAtivo(tecnico.getId());
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.DESCANSO
            );

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            assertEquals(4, historico.size());
            assertEquals(
                StatusDisponibilidade.DESCANSO,
                historico.get(0).getNovoStatus()
            );
            assertEquals(
                StatusDisponibilidade.ATIVO,
                historico.get(1).getNovoStatus()
            );
            assertEquals(
                StatusDisponibilidade.OCUPADO,
                historico.get(2).getNovoStatus()
            );
            assertEquals(
                StatusDisponibilidade.ATIVO,
                historico.get(3).getNovoStatus()
            );
        }

        @Test
        @DisplayName(
            "Entrada do histórico contém data/hora preenchida automaticamente e não nula"
        )
        void quandoHistoricoEhSalvoDataHoraEhPreenchidaAutomaticamente() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            assertFalse(historico.isEmpty());
            historico.forEach(entrada -> assertNotNull(entrada.getDataHora()));
        }

        @Test
        @DisplayName(
            "Histórico registra o id do técnico correto em cada entrada"
        )
        void quandoHistoricoEhSalvoIdDoTecnicoEhCorreto() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            var historico =
                historicoDisponibilidadeRepository.findAllByTecnicoIdOrderByDataHoraDesc(
                    tecnico.getId()
                );
            historico.forEach(entrada ->
                assertEquals(tecnico.getId(), entrada.getTecnicoId())
            );
        }
    }

    @Nested
    @DisplayName("Validação de disponibilidade para atribuição")
    class ValidacaoDisponibilidadeTests {

        @Test
        @DisplayName("Técnico em DESCANSO não pode ser atribuído")
        void quandoTecnicoEstaEmDescansoNaoPodeSerAtribuido() {
            Tecnico emDescanso = tecnicoRepository
                .findById(tecnico.getId())
                .orElseThrow();
            assertEquals(
                StatusDisponibilidade.DESCANSO,
                emDescanso.getStatusDisponibilidade()
            );

            assertThrows(RuntimeException.class, () ->
                tecnicoService.validarTecnicoDisponivel(emDescanso.getId())
            );
        }

        @Test
        @DisplayName("Técnico em OCUPADO não pode ser atribuído a novo chamado")
        void quandoTecnicoEstaOcupadoNaoPodeSerAtribuidoANovoChamado() {
            tecnicoRepository.save(
                tecnico
                    .toBuilder()
                    .statusDisponibilidade(StatusDisponibilidade.OCUPADO)
                    .build()
            );

            assertThrows(RuntimeException.class, () ->
                tecnicoService.validarTecnicoDisponivel(tecnico.getId())
            );
        }

        @Test
        @DisplayName(
            "Técnico em ATIVO pode ser atribuído — validação não lança exceção"
        )
        void quandoTecnicoEstaAtivoValidacaoNaoLancaExcecao() {
            tecnicoService.alterarDisponibilidade(
                tecnico.getId(),
                ACESSO_TECNICO,
                StatusDisponibilidade.ATIVO
            );

            assertDoesNotThrow(() ->
                tecnicoService.validarTecnicoDisponivel(tecnico.getId())
            );
        }

        @Test
        @DisplayName("isDisponivel retorna true apenas para ATIVO")
        void isDisponivelRetornaTrueApenasParaAtivo() {
            assertTrue(StatusDisponibilidade.ATIVO.isDisponivel());
            assertFalse(StatusDisponibilidade.DESCANSO.isDisponivel());
            assertFalse(StatusDisponibilidade.OCUPADO.isDisponivel());
        }
    }
}
