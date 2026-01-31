package com.ufcg.psoft.commerce.service;

import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.PlanoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.ChamadoRepository;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import com.ufcg.psoft.commerce.service.chamado.ChamadoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private ChamadoServiceImpl chamadoService;

    private ChamadoPostPutRequestDTO chamadoDTO;
    private Cliente clienteBasico;
    private Cliente clientePremium;
    private Servico servicoComum;
    private Servico servicoExclusivo;
    private Chamado chamado;

    @BeforeEach
    void setUp() {

        clienteBasico = Cliente.builder()
                .id(1L)
                .codigoAcesso("123456")
                .plano("Basico")
                .enderecoPrincipal("Rua A, 100")
                .build();
        
        clientePremium = Cliente.builder()
                .id(2L)
                .codigoAcesso("654321")
                .plano("Premium")
                .enderecoPrincipal("Rua B, 200")
                .build();

        servicoComum = Servico.builder()
                .id(1L)
                .nome("Reparo Simples")
                .tipo("Basico")
                .valor(100.0)
                .build();

        servicoExclusivo = Servico.builder()
                .id(2L)
                .nome("Atendimento 24h")
                .tipo("Premium")
                .valor(250.0)
                .build();

        chamadoDTO = ChamadoPostPutRequestDTO.builder()
                .empresaId(1L)
                .codigoAcessoCliente("123456")
                .build();
                
        chamado = Chamado.builder()
                .id(1L)
                .cliente(clienteBasico)
                .servico(servicoComum)
                .status("AGUARDANDO_PAGAMENTO")
                .build();
    }

    @Test
    @DisplayName("Cliente Premium solicita serviço Premium com sucesso")
    void testClientePremiumSolicitaServicoPremium() {
        chamadoDTO.setServicoId(servicoExclusivo.getId());
        chamadoDTO.setCodigoAcessoCliente(clientePremium.getCodigoAcesso());

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clientePremium));
        when(servicoRepository.findById(servicoExclusivo.getId())).thenReturn(Optional.of(servicoExclusivo));
        when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(Empresa.builder().build()));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

        ChamadoResponseDTO resultado = chamadoService.criarChamado(clientePremium.getId(), chamadoDTO);

        assertNotNull(resultado);
        verify(chamadoRepository, times(1)).save(any(Chamado.class));
    }

    @Test
    @DisplayName("Cliente Premium solicita serviço Basico com sucesso")
    void testClientePremiumSolicitaServicoComum() {
        chamadoDTO.setServicoId(servicoComum.getId());
        chamadoDTO.setCodigoAcessoCliente(clientePremium.getCodigoAcesso());

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clientePremium));
        when(servicoRepository.findById(servicoComum.getId())).thenReturn(Optional.of(servicoComum));
        when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(Empresa.builder().build()));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

        ChamadoResponseDTO resultado = chamadoService.criarChamado(clientePremium.getId(), chamadoDTO);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Cliente Basico solicita serviço Premium deve falhar")
    void testClienteBasicoSolicitaServicoPremium() {
        chamadoDTO.setServicoId(servicoExclusivo.getId());
        chamadoDTO.setCodigoAcessoCliente(clienteBasico.getCodigoAcesso());

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clienteBasico));
        when(servicoRepository.findById(servicoExclusivo.getId())).thenReturn(Optional.of(servicoExclusivo));

        assertThrows(PlanoInvalidoException.class, () -> {
            chamadoService.criarChamado(clienteBasico.getId(), chamadoDTO);
        });
        
        verify(chamadoRepository, never()).save(any(Chamado.class));
    }

    @Test
    @DisplayName("Cliente Basico solicita serviço Basico com sucesso")
    void testClienteBasicoSolicitaServicoComum() {
        chamadoDTO.setServicoId(servicoComum.getId());
        
        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clienteBasico));
        when(servicoRepository.findById(servicoComum.getId())).thenReturn(Optional.of(servicoComum));
        when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(Empresa.builder().build()));
        when(chamadoRepository.save(any(Chamado.class))).thenReturn(chamado);

        ChamadoResponseDTO resultado = chamadoService.criarChamado(clienteBasico.getId(), chamadoDTO);

        assertNotNull(resultado);
    }

    @Test
    @DisplayName("Criar chamado sem endereço deve usar endereço principal do cliente")
    void testCriarChamadoUsaEnderecoCliente() {
        chamadoDTO.setServicoId(servicoComum.getId());
        chamadoDTO.setEnderecoAtendimento(null);

        when(clienteRepository.findById(anyLong())).thenReturn(Optional.of(clienteBasico));
        when(servicoRepository.findById(servicoComum.getId())).thenReturn(Optional.of(servicoComum));
        when(empresaRepository.findById(anyLong())).thenReturn(Optional.of(Empresa.builder().build()));
        
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> i.getArgument(0));

        ChamadoResponseDTO resultado = chamadoService.criarChamado(clienteBasico.getId(), chamadoDTO);

        assertEquals(clienteBasico.getEnderecoPrincipal(), resultado.getEnderecoAtendimento());
    }

    @Test
    @DisplayName("Confirmar pagamento deve alterar status do chamado")
    void testConfirmarPagamentoMudaStatus() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        
        when(chamadoRepository.save(any(Chamado.class))).thenAnswer(i -> {
            Chamado c = (Chamado) i.getArgument(0);
            c.setStatus("EM_PROCESSAMENTO");
            return c;
        });

        ChamadoResponseDTO resultado = chamadoService.confirmarPagamento(1L, clienteBasico.getCodigoAcesso(), "PIX");

        assertEquals("EM_PROCESSAMENTO", resultado.getStatus());
    }

    @Test
    @DisplayName("Falhar ao confirmar pagamento com código de acesso incorreto")
    void testConfirmarPagamentoCodigoInvalido() {
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        
        assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
            chamadoService.confirmarPagamento(1L, "000000", "PIX");
        });
    }
}