package com.ufcg.psoft.commerce.service.chamado;
import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.exception.ChamadoNaoPodeSerCancelado;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.PlanoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
import com.ufcg.psoft.commerce.service.empresa.EmpresaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.model.TipoServico;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Chamado")
public class ChamadoServiceTests {

    @Mock
    private ChamadoRepository chamadoRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    private TecnicoRepository tecnicoRepository;

    @Mock
    EmpresaService empresaService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ChamadoServiceImpl chamadoService;

    private ChamadoPostPutRequestDTO chamadoDTO;
    private Cliente clienteBasico;
    private Cliente clientePremium;
    private Servico servicoComum;
    private Servico servicoExclusivo;
    private Chamado chamado;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        lenient().when(modelMapper.map(any(), eq(ChamadoResponseDTO.class)))
                .thenAnswer(invocation -> {
                    Chamado c = invocation.getArgument(0);
                    return ChamadoResponseDTO.builder()
                            .id(c.getId())
                            .status(c.getStatus())
                            .enderecoAtendimento(c.getEnderecoAtendimento())
                            .build();
                });

        empresa = Empresa.builder()
                .id(1L)
                .nome("Service Corp")
                .codigoAcesso("101010")
                .cnpj("12.345.678/0001-90")
                .build();

        clienteBasico = Cliente.builder()
                .id(1L)
                .codigo("123456")
                .planoAtual(Plano.BASICO)
                .endereco("Rua A, 100")
                .dataCobranca(LocalDate.now())
                .build();

        clientePremium = Cliente.builder()
                .id(2L)
                .codigo("654321")
                .planoAtual(Plano.PREMIUM)
                .endereco("Rua B, 200")
                .dataCobranca(LocalDate.now())
                .build();

        servicoComum = Servico.builder()
                .id(1L)
                .nome("Reparo Simples")
                .preco(100.0)
                .urgencia(Urgencia.NORMAL)
                .plano(Plano.BASICO)
                .empresa(empresa)
                .descricao("Reparo simples de tomadas")
                .duracao(30.0)
                .disponivel(true)
                .tipo(TipoServico.ELETRICA)
                .build();

        servicoExclusivo = Servico.builder()
                .id(2L)
                .nome("Instalação 24h")
                .preco(300.0)
                .urgencia(Urgencia.ALTA)
                .plano(Plano.PREMIUM)
                .empresa(empresa)
                .descricao("Instalação completa urgente")
                .duracao(120.0)
                .disponivel(true)
                .tipo(TipoServico.PINTURA)
                .build();

        chamadoDTO = ChamadoPostPutRequestDTO.builder()
                .empresaId(1L)
                .build();

        chamado = Chamado.builder()
                .id(1L)
                .cliente(clienteBasico)
                .servico(servicoComum)
                .status("AGUARDANDO_PAGAMENTO")
                .build();

        chamado.setEstado(new ChamadoEstadoAguardandoPagamento());
    }


    @Nested
    @DisplayName("Testes de Operações Basicas Em Chamado")
    class CRUDChamado {
        @Test
        @DisplayName("Cliente Premium solicita serviço Premium com sucesso")
        void testClientePremiumSolicitaServicoPremium() {
            chamadoDTO.setServicoId(servicoExclusivo.getId());

            when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findById(servicoExclusivo.getId())).thenReturn(Optional.of(servicoExclusivo));
            when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);
            ChamadoResponseDTO resultado = chamadoService.criarChamado(clientePremium.getId(), clientePremium.getCodigo(), chamadoDTO);
            assertNotNull(resultado);
            verify(chamadoRepository, times(1)).save(any(Chamado.class));
        }

        @Test
        @DisplayName("Cliente Premium solicita serviço Basico com sucesso")
        void testClientePremiumSolicitaServicoComum() {
            chamadoDTO.setServicoId(servicoComum.getId());

            when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findById(servicoComum.getId())).thenReturn(Optional.of(servicoComum));
            when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

            ChamadoResponseDTO resultado = chamadoService.criarChamado(clientePremium.getId(), clientePremium.getCodigo(), chamadoDTO);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Cliente Basico solicita serviço Premium deve falhar")
        void testClienteBasicoSolicitaServicoPremium() {
            chamadoDTO.setServicoId(servicoExclusivo.getId());
            when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(servicoExclusivo.getId())).thenReturn(Optional.of(servicoExclusivo));
            when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(empresa));
            assertThrows(PlanoInvalidoException.class, () -> {
                chamadoService.criarChamado(clienteBasico.getId(), clienteBasico.getCodigo(), chamadoDTO);
            });

            verify(chamadoRepository, never()).save(any(Chamado.class));
        }

        @Test
        @DisplayName("Cliente Basico solicita serviço Basico com sucesso")
        void testClienteBasicoSolicitaServicoComum() {
            chamadoDTO.setServicoId(servicoComum.getId());

            when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(servicoComum.getId())).thenReturn(Optional.of(servicoComum));
            when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

            ChamadoResponseDTO resultado = chamadoService.criarChamado(clienteBasico.getId(), clienteBasico.getCodigo(), chamadoDTO);

            assertNotNull(resultado);
        }

        @Test
        @DisplayName("Criar chamado sem endereço deve usar endereço principal do cliente")
        void testCriarChamadoUsaEnderecoCliente() {
            chamadoDTO.setServicoId(servicoComum.getId());
            chamadoDTO.setEnderecoAtendimento(null);

            when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(servicoComum.getId())).thenReturn(Optional.of(servicoComum));
            when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(empresa));

            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));

            ChamadoResponseDTO resultado = chamadoService.criarChamado(clienteBasico.getId(), clienteBasico.getCodigo(), chamadoDTO);

            assertEquals(clienteBasico.getEndereco(), resultado.getEnderecoAtendimento());
        }
    }

    @Nested
    @DisplayName("Testes Envolvendo Estados do Chamado")
    class NotificacoChamado {
        @Test
        @DisplayName("Confirmar pagamento deve alterar status do chamado")
        void testConfirmarPagamentoMudaStatus() {
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> {
                Chamado c = (Chamado) i.getArgument(0);
                c.setStatus("CHAMADO_RECEBIDO");
                return c;
            });

            ChamadoResponseDTO resultado = chamadoService.confirmarPagamento(1L, clienteBasico.getCodigo(), "PIX");

            assertEquals("CHAMADO_RECEBIDO", resultado.getStatus());
        }

        @Test
        @DisplayName("Falhar ao confirmar pagamento com código de acesso incorreto")
        void testConfirmarPagamentoCodigoInvalido() {
            when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

            assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
                chamadoService.confirmarPagamento(1L, "000000", "PIX");
            });
        }

        @Test
        @DisplayName("Deve notificar o cliente")
        void deveNotificarCliente() {
            Cliente clienteSpy = spy(clienteBasico);
            chamado.setCliente(clienteSpy);
            chamado.getEstado().confirmarPagamento(chamado);
            chamado.getEstado().avancar(chamado);
            chamado.getEstado().avancar(chamado);
            Tecnico tecnico =
                    Tecnico.builder()
                            .id(1L)
                            .nome("Carlos Silva")
                            .corVeiculo("Branco")
                            .tipoVeiculo(TipoVeiculo.CARRO)
                            .placaVeiculo("ABC-1234")
                            .acesso("123")
                            .especialidade("GERAL")
                            .build();
            chamado.atribuirTecnico(tecnico);
            chamado.getEstado().avancar(chamado);
            verify(clienteSpy, times(1)).notificar(any(Tecnico.class));

        }

        @Test
        @DisplayName("Não deve notificar listener quando entrar em CHAMADO_RECEBIDO")
        void naoDeveNotificarQuandoChamadoRecebido() {

            ListenerChamado listener = mock(ListenerChamado.class);

            chamado.setEstado(new ChamadoEstadoAguardandoPagamento());
            chamado.setStatus("AGUARDANDO_PAGAMENTO");
            chamado.confirmarPagamento();

            verify(listener, never()).notificar(any(Tecnico.class));
        }

        @Test
        @DisplayName("Não deve notificar quando mudar de CHAMADO_RECEBIDO para EM_ANALISE")
        void naoDeveNotificarQuandoMudarDeRecebidoParaEmAnalise() {
            Chamado chamado = new Chamado();
            chamado.mudaEstado(new ChamadoEstadoRecebido());
            ListenerChamado listener = mock(ListenerChamado.class);
            chamado.mudaEstado(new ChamadoEstadoEmAnalise());
            verify(listener, never()).notificar(any());
        }

        @Test
        @DisplayName("Não deve notificar listener quando entrar em AGUARDANDO_TECNICO")
        void naoDeveNotificarQuandoAguardandoTecnico() {

            ListenerChamado listener = mock(ListenerChamado.class);

            chamado.setEstado(new ChamadoEstadoAguardandoPagamento());
            chamado.setStatus("AGUARDANDO_PAGAMENTO");
            chamado.confirmarPagamento();
            chamado.getEstado().avancar(chamado);
            verify(listener, never()).notificar(any(Tecnico.class));
        }

        @Test
        @DisplayName("Não deve notificar listener quando chamado for CONCLUIDO")
        void naoDeveNotificarQuandoFinalizado() {

            ListenerChamado listener = mock(ListenerChamado.class);
            chamado.setEstado(new ChamadoEstadoAguardandoPagamento());
            chamado.confirmarPagamento();
            chamado.getEstado().avancar(chamado);
            chamado.getEstado().avancar(chamado);
            Tecnico tecnico = Tecnico.builder()
                    .id(1L)
                    .nome("Carlos Silva")
                    .corVeiculo("Branco")
                    .tipoVeiculo(TipoVeiculo.CARRO)
                    .placaVeiculo("ABC-1234")
                    .acesso("123")
                    .especialidade("GERAL")
                    .build();
            empresaService.aprovarTecnico(empresa.getId(), tecnico.getId(), empresa.getCodigoAcesso());
            chamado.getEstado().atribuirTecnico(chamado, tecnico);
            chamado.getEstado().avancar(chamado);
            verify(listener, never()).notificar(any(Tecnico.class));

        }
    }
    @Nested
    @DisplayName("Conjunto de testes de listagem de chamados - US20")
    class ListagemDeChamados {

        @Test
        @DisplayName("Listar histórico ordenado - Garantir que a ordem (Não concluídos primeiro) é mantida")
        void listarHistoricoOrdenadoComVariosStatus() {
            Chamado chamado1 = Chamado.builder().id(1L).status("AGUARDANDO_TECNICO").build();
            Chamado chamado2 = Chamado.builder().id(2L).status("EM_ANALISE").build();
            Chamado chamado3 = Chamado.builder().id(3L).status("CONCLUIDO").build();
            List<Chamado> listaDoBanco = Arrays.asList(chamado1, chamado2, chamado3);
            when(clienteRepository.findById(clienteBasico.getId())).thenReturn(Optional.of(clienteBasico));
            when(chamadoRepository.findByClienteIdOrderByStatusEData(clienteBasico.getId()))
                    .thenReturn(listaDoBanco);
            List<ChamadoResponseDTO> resultado = chamadoService.listarChamadosCliente(clienteBasico.getId(), "123456");
            assertAll(
                    () -> assertEquals(3, resultado.size(), "Deveria retornar 3 chamados"),
                    () -> assertEquals("AGUARDANDO_TECNICO", resultado.get(0).getStatus()),
                    () -> assertEquals("EM_ANALISE", resultado.get(1).getStatus()),
                    () -> assertEquals("CONCLUIDO", resultado.get(2).getStatus()),
                    () -> verify(chamadoRepository, times(1)).findByClienteIdOrderByStatusEData(clienteBasico.getId())
            );
        }

        @Test
        @DisplayName("Filtrar chamados por status usando Enum")
        void listarChamadosPorStatus() {
            ChamadoStatus statusEnum = ChamadoStatus.AGUARDANDO_PAGAMENTO;
            when(clienteRepository.findById(clienteBasico.getId())).thenReturn(Optional.of(clienteBasico));
            when(chamadoRepository.findByClienteIdAndStatusOrderByDataCriacaoDesc(clienteBasico.getId(), statusEnum.name()))
                    .thenReturn(Arrays.asList(chamado));
            List<ChamadoResponseDTO> resultado = chamadoService.listarChamadosClientePorStatus(
                    clienteBasico.getId(), ChamadoStatus.AGUARDANDO_PAGAMENTO, "123456"
            );
            assertAll(
                    () -> assertEquals(1, resultado.size()),
                    () -> verify(chamadoRepository, times(1)).findByClienteIdAndStatusOrderByDataCriacaoDesc(clienteBasico.getId(), statusEnum.name())
            );
        }

        @Test
        @DisplayName("Buscar chamado específico por ID e ClienteID (Segurança)")
        void buscarChamadoPorId() {
            when(clienteRepository.findById(clienteBasico.getId())).thenReturn(Optional.of(clienteBasico));
            when(chamadoRepository.findByIdAndClienteId(chamado.getId(), clienteBasico.getId()))
                    .thenReturn(Optional.of(chamado));
            ChamadoResponseDTO resultado = chamadoService.buscarChamadoPorCliente(
                    chamado.getId(), clienteBasico.getId(), "123456"
            );
            assertAll(
                    () -> assertNotNull(resultado),
                    () -> assertEquals(chamado.getId(), resultado.getId()),
                    () -> verify(chamadoRepository, times(1)).findByIdAndClienteId(chamado.getId(), clienteBasico.getId())
            );
        }
        @Test
        @DisplayName("Listar histórico garantindo que a ordem vinda do repositorio é mantida")
        void listarHistoricoOrdenadoVeridico() {
            Chamado chamado1 = Chamado.builder().id(1L).status("AGUARDANDO_TECNICO").build();
            Chamado chamado2 = Chamado.builder().id(2L).status("CONCLUIDO").build();
            List<Chamado> listaDoBanco = Arrays.asList(chamado1, chamado2);
            when(clienteRepository.findById(clienteBasico.getId())).thenReturn(Optional.of(clienteBasico));
            when(chamadoRepository.findByClienteIdOrderByStatusEData(clienteBasico.getId()))
                    .thenReturn(listaDoBanco);
            List<ChamadoResponseDTO> resultado = chamadoService.listarChamadosCliente(clienteBasico.getId(), "123456");
            assertAll(
                    () -> assertEquals(2, resultado.size()),
                    () -> assertEquals("AGUARDANDO_TECNICO", resultado.get(0).getStatus()),
                    () -> assertEquals("CONCLUIDO", resultado.get(1).getStatus()),
                    () -> assertEquals(1L, resultado.get(0).getId())
            );
        }
        @Test
        @DisplayName("Filtrar por status corretamente")
        void deveRetornarApenasStatusSolicitadoIgnorandoOutros() {
            ChamadoStatus statusDesejado = ChamadoStatus.EM_ANALISE;
            Chamado correto = Chamado.builder().id(10L).status(statusDesejado.name()).build();
            Chamado incorreto = Chamado.builder().id(20L).status(ChamadoStatus.CONCLUIDO.name()).build();
            when(clienteRepository.findById(clienteBasico.getId())).thenReturn(Optional.of(clienteBasico));
            when(chamadoRepository.findByClienteIdAndStatusOrderByDataCriacaoDesc(clienteBasico.getId(), statusDesejado.name()))
                    .thenReturn(Arrays.asList(correto));
            List<ChamadoResponseDTO> resultado = chamadoService.listarChamadosClientePorStatus(
                    clienteBasico.getId(), statusDesejado, "123456"
            );
            assertAll(
                    () -> assertEquals(1, resultado.size(), "Deveria vir apenas 1 chamado"),
                    () -> assertEquals(10L, resultado.get(0).getId(), "O ID deveria ser o do chamado correto"),
                    () -> assertTrue(resultado.stream().noneMatch(c -> c.getId().equals(20L)), "O chamado CONCLUIDO não deveria estar aqui")
            );
        }
    }
    @Nested
    @DisplayName("Testes de Cancelamento de Chamado no Service")
    class CancelamentoChamadoService {

        @Test
        @DisplayName("Deve cancelar chamado com sucesso")
        void deveCancelarComSucesso() {
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            chamadoService.cancelar(chamado.getId(), clienteBasico.getId(), "123456");
            verify(chamadoRepository, times(1)).deleteById(chamado.getId());
        }

        @Test
        @DisplayName("Deve falhar quando o status for EM_ATENDIMENTO")
        void deveFalharSeStatusEmAtendimento() {
            chamado.setStatus("EM_ATENDIMENTO");
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            assertThrows(ChamadoNaoPodeSerCancelado.class, () ->
                    chamadoService.cancelar(chamado.getId(), clienteBasico.getId(), "123456")
            );
            verify(chamadoRepository, never()).deleteById(anyLong());
        }
        @Test
        @DisplayName("Deve cancelar quando o status for CHAMADO_RECEBIDO")
        void deveCancelarSeStatusChamadoRecebido() {
            chamado.setStatus("CHAMADO_RECEBIDO");
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            chamadoService.cancelar(chamado.getId(), clienteBasico.getId(), "123456");
            verify(chamadoRepository, times(1)).deleteById(chamado.getId());
        }
        @Test
        @DisplayName("Deve cancelar quando o status for AGUARDANDO_TECNICO")
        void deveCancelarAguardandoTecnico() {
            chamado.setStatus("AGUARDANDO_TECNICO");
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            chamadoService.cancelar(chamado.getId(), clienteBasico.getId(), "123456");
            verify(chamadoRepository, times(1)).deleteById(chamado.getId());
        }
        @Test
        @DisplayName("Deve cancelar quando o status for AGUARDANDO_PAGAMENTO")
        void deveCancelarSeStatusAguardandoPagamento() {
            chamado.setStatus("AGUARDANDO_PAGAMENTO");
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            chamadoService.cancelar(chamado.getId(), clienteBasico.getId(), "123456");
            verify(chamadoRepository, times(1)).deleteById(chamado.getId());
        }
        @Test
        @DisplayName("Deve falhar quando o ID do cliente for diferente do dono do chamado")
        void deveFalharSeChamadoForDeOutroCliente() {
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            assertThrows(ClienteNaoExisteException.class, () ->
                    chamadoService.cancelar(chamado.getId(), 99L, "123456")
            );
            verify(chamadoRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Deve falhar com código de acesso inválido")
        void deveFalharComCodigoInvalido() {
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));

            assertThrows(CodigoDeAcessoInvalidoException.class, () ->
                    chamadoService.cancelar(chamado.getId(), clienteBasico.getId(), "CODIGO_ERRADO")
            );
            verify(chamadoRepository, never()).deleteById(anyLong());
        }
    }
}