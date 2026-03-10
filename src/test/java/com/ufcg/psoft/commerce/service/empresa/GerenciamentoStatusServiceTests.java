package com.ufcg.psoft.commerce.service.empresa;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
import com.ufcg.psoft.commerce.service.chamado.ChamadoServiceImpl;
import com.ufcg.psoft.commerce.service.tecnico.TecnicoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Serviço de Gerenciamento de Status ")
public class GerenciamentoStatusServiceTests {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    private ClienteRepository clienteRepository;
    
    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private HistoricoDisponibilidadeRepository historicoRepository;

    @Spy
    private ModelMapper modelMapper = new ModelMapper();
  
    @InjectMocks
    private TecnicoService tecnicoService;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    @InjectMocks
    private ChamadoServiceImpl chamadoService;

    private Empresa empresa;
    private Chamado chamado;
    private final String CODIGO_ACESSO = "123456";

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
            .id(1L)
            .nome("Service Corp")
            .codigoAcesso(CODIGO_ACESSO)
            .build();

        Cliente cliente = Cliente.builder().id(100L).build();
        Servico servico = Servico.builder().id(200L).build();

        chamado = Chamado.builder()
            .id(10L)
            .empresa(empresa)
            .cliente(cliente)
            .servico(servico)
            .status("CHAMADO_RECEBIDO")
            .build();
    }

    @Nested
    @DisplayName("Cenários de Sucesso no avanço dos estados")
    class SucessoTests {

        @Test
        @DisplayName(
            "Avança estado de 'Chamado recebido' para 'Em análise' com sucesso"
        )
        void avancarEstadoRecebidoParaAnalise() {
            when(empresaRepository.findById(empresa.getId())).thenReturn(
                Optional.of(empresa)
            );
            when(chamadoRepository.findById(chamado.getId())).thenReturn(
                Optional.of(chamado)
            );
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(
                invocation -> {
                    Chamado c = invocation.getArgument(0);
                    c.setStatus("EM_ANALISE");
                    return c;
                }
            );

            ChamadoResponseDTO resultado = empresaService.avancarStatus(
                empresa.getId(),
                CODIGO_ACESSO,
                chamado.getId()
            );

            assertNotNull(resultado);
            assertEquals("EM_ANALISE", resultado.getStatus());
            verify(chamadoRepository, times(1)).save(chamado);
        }

        @Test
        @DisplayName(
            "Avança estado de 'Em análise' para 'Aguardando técnico' com sucesso"
        )
        void avancarEstadoAnaliseParaAguardandoTecnico() {
            chamado.setStatus("EM_ANALISE");

            when(empresaRepository.findById(empresa.getId())).thenReturn(
                Optional.of(empresa)
            );
            when(chamadoRepository.findById(chamado.getId())).thenReturn(
                Optional.of(chamado)
            );

            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(
                invocation -> {
                    Chamado c = invocation.getArgument(0);
                    c.setStatus("AGUARDANDO_TECNICO");
                    return c;
                }
            );

            ChamadoResponseDTO resultado = empresaService.avancarStatus(
                empresa.getId(),
                CODIGO_ACESSO,
                chamado.getId()
            );

            assertEquals("AGUARDANDO_TECNICO", resultado.getStatus());
            verify(chamadoRepository, times(1)).save(any(Chamado.class));
        }

        @Test
        @DisplayName(
            "Atribui técnico com sucesso quando chamado está Aguardando Técnico"
        )
        void atribuirTecnicoComSucesso() {
            chamado.setStatus("AGUARDANDO_TECNICO");
            Tecnico tecnico = Tecnico.builder()
                .id(300L)
                .statusDisponibilidade(
                    com.ufcg.psoft.commerce.model.StatusDisponibilidade.ATIVO
                )
                .build();
            tecnico.getEmpresasAprovadoras().add(empresa);
            doNothing()
                .when(tecnicoService)
                .validarTecnicoDisponivel(tecnico.getId());
            doNothing().when(tecnicoService).marcarComoOcupado(tecnico.getId());

            when(empresaRepository.findById(empresa.getId())).thenReturn(
                Optional.of(empresa)
            );
            when(chamadoRepository.findById(chamado.getId())).thenReturn(
                Optional.of(chamado)
            );
            when(tecnicoRepository.findById(tecnico.getId())).thenReturn(
                Optional.of(tecnico)
            );

            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(
                invocation -> invocation.getArgument(0)
            );

            ChamadoResponseDTO resultado = empresaService.atribuirTecnico(
                empresa.getId(),
                CODIGO_ACESSO,
                chamado.getId(),
                tecnico.getId()
            );

            assertNotNull(resultado);
            assertEquals("EM_ATENDIMENTO", resultado.getStatus());
            verify(chamadoRepository, times(1)).save(any(Chamado.class));
        }
    }

    @Nested
    @DisplayName("Cenários de Erro e Validação no avanço dos estados")
    class FalhaTests {

        @Test
        @DisplayName(
            "Lança exceção ao tentar avançar status com código de acesso inválido"
        )
        void avancarEstadoFalhaCodigoAcesso() {
            when(empresaRepository.findById(empresa.getId())).thenReturn(
                Optional.of(empresa)
            );

            assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
                empresaService.avancarStatus(
                    empresa.getId(),
                    "000000",
                    chamado.getId()
                );
            });

            verify(chamadoRepository, never()).save(any(Chamado.class));
        }

        @Test
        @DisplayName(
            "Lança exceção se o chamado não pertencer à empresa informada"
        )
        void avancarEstadoChamadoDeOutraEmpresa() {
            Empresa outraEmpresa = Empresa.builder()
                .id(2L)
                .codigoAcesso("654321")
                .build();

            when(empresaRepository.findById(outraEmpresa.getId())).thenReturn(
                Optional.of(outraEmpresa)
            );
            when(chamadoRepository.findById(chamado.getId())).thenReturn(
                Optional.of(chamado)
            );

            assertThrows(RuntimeException.class, () -> {
                empresaService.avancarStatus(
                    outraEmpresa.getId(),
                    "654321",
                    chamado.getId()
                );
            });

            verify(chamadoRepository, never()).save(any(Chamado.class));
        }
    }

    @Nested
    @DisplayName("Testes de atribuição automática dos técnicos às chamadas")
    class AtribuicaoAutomaticaTecnicosChamadas {

        private Tecnico tecnicoDisponivel;
        private Empresa empresa2;
        private Cliente cliente2;
        private Servico servico2;
        private Chamado proximoChamado;
        private Chamado chamadoPrincipal;

        @BeforeEach
        void setupTestesDeAtribuicaoAutomatica() {
            empresa2 = Empresa.builder()
                .id(2L)
                .nome("Service Corp")
                .codigoAcesso("123456")
                .cnpj("12.345.678/0001-90")
                .build();
                
            tecnicoDisponivel = Tecnico.builder()
                .id(10L)
                .nome("Tecnico Um da Silva")
                .acesso("654321")
                .tipoVeiculo(TipoVeiculo.CARRO)
                .placaVeiculo("ABC-9999")
                .corVeiculo("Preto")
                .empresasAprovadoras(new ArrayList<>(List.of(empresa2)))
                .statusDisponibilidade(StatusDisponibilidade.ATIVO)
                .dataUltimaMudancaDisponibilidade(LocalDateTime.now().minusHours(5))
                .especialidade("Geral")
                .build();

            cliente2 = Cliente.builder()
                .id(30L)
                .nome("Teste Basico")
                .codigo("123456")
                .endereco("Rua Base, 100")
                .planoAtual(Plano.BASICO)
                .dataCobranca(LocalDate.now())
                .build();

            servico2 = Servico.builder()
                .id(70L)
                .nome("Manutenção Simples")
                .descricao("Reparo básico")
                .urgencia(Urgencia.NORMAL)
                .duracao(30.0)
                .preco(100.0)
                .empresa(empresa2)
                .plano(Plano.BASICO)
                .disponivel(true)
                .tipo(TipoServico.ELETRICA)
                .build();

            proximoChamado = Chamado.builder()
                .id(37L)
                .empresa(empresa2)
                .cliente(cliente2)
                .servico(servico2)
                .enderecoAtendimento("Rua dos Tocantins, 100")
                .dataCriacao(LocalDateTime.now())
                .status("CHAMADO_RECEBIDO")
                .build();

            chamadoPrincipal = Chamado.builder()
                .id(20L)
                .empresa(empresa2)
                .cliente(cliente2)
                .servico(servico2)
                .enderecoAtendimento("Rua dos Veredas, 200")
                .dataCriacao(LocalDateTime.now())
                .status("EM_ANALISE") 
                .build();
                
        }

        @Test
        @DisplayName("Atribui técnico automaticamente e muda seu status para indisponível for alocado")
        void atribuiETornaIndisponivelTecnicoAutomaticamente() {

            // Arrange
            proximoChamado.setStatus("EM_ANALISE");

            when(empresaRepository.findById(empresa2.getId())).thenReturn(Optional.of(empresa2));
            when(chamadoRepository.findById(proximoChamado.getId())).thenReturn(Optional.of(proximoChamado));
            when(tecnicoRepository.findTecnicoAtivoMaisTempoParaEmpresa(empresa2.getId())).thenReturn(Optional.of(tecnicoDisponivel));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));
            when(tecnicoRepository.save(any(Tecnico.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            ChamadoResponseDTO resultado = empresaService.avancarStatus(empresa2.getId(), "123456", proximoChamado.getId());

            // Assert
            assertAll(
                () -> assertEquals("EM_ATENDIMENTO", resultado.getStatus()),
                () -> assertEquals(StatusDisponibilidade.OCUPADO, tecnicoDisponivel.getStatusDisponibilidade()),
                () -> verify(tecnicoRepository, times(1)).save(tecnicoDisponivel),
                () -> verify(chamadoRepository, times(1)).save(proximoChamado)
            );

        }

        @Test
        @DisplayName("Deve apenas permanecer AGUARDANDO_TECNICO se não houver técnico disponivel")
        void devePermanecerAguardandoTecnicoSeNaoHouveTecnico() {
            
            proximoChamado.setStatus("EM_ANALISE");

            when(empresaRepository.findById(empresa2.getId())).thenReturn(Optional.of(empresa2));
            when(chamadoRepository.findById(proximoChamado.getId())).thenReturn(Optional.of(proximoChamado));
            when(tecnicoRepository.findTecnicoAtivoMaisTempoParaEmpresa(empresa2.getId())).thenReturn(Optional.empty());
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            ChamadoResponseDTO resultado = empresaService.avancarStatus(empresa2.getId(), "123456", proximoChamado.getId());

            // Assert
            assertAll(
                    () -> assertEquals("AGUARDANDO_TECNICO", resultado.getStatus()),
                    () -> verify(tecnicoRepository, never()).save(any()),
                    () -> verify(chamadoRepository, times(1)).save(proximoChamado)
            );

        }

        @Test
        @DisplayName("Técnico deve ser alocado para outra chamada ao ficar disponviel")
        void tecnicoDeveSerAlocadoParaOutraChamadaAoFicarDisponivel() {
            // Arrange
            proximoChamado.setStatus("AGUARDANDO_TECNICO");
            proximoChamado.setEstado(new ChamadoEstadoAguardandoTecnico());
            chamadoPrincipal.setStatus("AGUARDANDO_CONFIRMACAO");
            chamadoPrincipal.setEstado(new ChamadoEstadoAguardandoConfirmacao());
            tecnicoDisponivel.setStatusDisponibilidade(StatusDisponibilidade.OCUPADO); 
            chamadoPrincipal.setTecnico(tecnicoDisponivel);

            when(clienteRepository.findById(cliente2.getId())).thenReturn(Optional.of(cliente2));
            when(chamadoRepository.findById(chamadoPrincipal.getId())).thenReturn(Optional.of(chamadoPrincipal));
            when(chamadoRepository.findFirstByEmpresaIdAndStatusOrderByDataCriacaoAsc(empresa2.getId(), ChamadoStatus.AGUARDANDO_TECNICO.getNome())).thenReturn(Optional.of(proximoChamado));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));

            // Act: 
            ChamadoResponseDTO resultado = chamadoService.confirmarConclusao(cliente2.getId(), cliente2.getCodigo(), chamadoPrincipal.getId());

            // Assert
            assertAll(
                    () -> assertEquals("CONCLUIDO", resultado.getStatus()),
                    () -> assertEquals("EM_ATENDIMENTO", proximoChamado.getStatus()),
                    () -> assertNotNull(proximoChamado.getTecnico()),
                    () -> verify(chamadoRepository, times(2)).save(any(Chamado.class)),
                    () -> verify(tecnicoRepository, never()).save(any())
            );
        }

        @Test
        @DisplayName("Técnico deve ficar disponivel se não houver chamado AGUARDANDO_TECNICO")
        void tecnicoDeveFicarDisponivelSeNaoHouverChamado() {
            // Arrange
            chamadoPrincipal.setStatus("AGUARDANDO_CONFIRMACAO");
            tecnicoDisponivel.setStatusDisponibilidade(StatusDisponibilidade.OCUPADO); 
            chamadoPrincipal.setTecnico(tecnicoDisponivel);

            when(clienteRepository.findById(cliente2.getId())).thenReturn(Optional.of(cliente2));
            when(chamadoRepository.findById(chamadoPrincipal.getId())).thenReturn(Optional.of(chamadoPrincipal));
            when(chamadoRepository.findFirstByEmpresaIdAndStatusOrderByDataCriacaoAsc(empresa2.getId(), ChamadoStatus.AGUARDANDO_TECNICO.getNome())).thenReturn(Optional.empty());
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));
            when(tecnicoRepository.save(any(Tecnico.class))).thenAnswer(i -> i.getArgument(0));

            // Act
            ChamadoResponseDTO resultado = chamadoService.confirmarConclusao(cliente2.getId(), cliente2.getCodigo(), chamadoPrincipal.getId());

            // Assert
            assertAll(
                    () -> assertEquals("CONCLUIDO", resultado.getStatus()),
                    () -> assertEquals(StatusDisponibilidade.ATIVO, tecnicoDisponivel.getStatusDisponibilidade()),
                    () -> verify(tecnicoRepository, times(1)).save(tecnicoDisponivel)
            );
        }
    }
}
