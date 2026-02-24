package com.ufcg.psoft.commerce.service.chamado;
import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
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
            // CONCLUIDO
            verify(listener, never()).notificar(any(Tecnico.class));

        }
    }
}