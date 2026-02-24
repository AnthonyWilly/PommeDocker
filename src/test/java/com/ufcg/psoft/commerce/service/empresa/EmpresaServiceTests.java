package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CommerceException;
import com.ufcg.psoft.commerce.exception.EmpresaNaoExisteException;
import com.ufcg.psoft.commerce.model.Chamado;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Pagamento;
import com.ufcg.psoft.commerce.model.PagamentoCredito;
import com.ufcg.psoft.commerce.model.PagamentoDebito;
import com.ufcg.psoft.commerce.model.PagamentoPix;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ChamadoRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import com.ufcg.psoft.commerce.service.empresa.EmpresaServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do Serviço de Empresa")
public class EmpresaServiceTests {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ChamadoRepository chamadoRepository;

    @Mock
    private ServicoRepository servicoRepository;

        @Spy
        private Pagamento pagamento = new Pagamento(List.of(
            new PagamentoCredito(),
            new PagamentoDebito(),
            new PagamentoPix()
        ));

    @InjectMocks
    private EmpresaServiceImpl empresaService;
    private EmpresaPostPutRequestDTO empresaDTO;
    private Empresa empresa;


    @BeforeEach
    void setUp() {
        
        empresaDTO = EmpresaPostPutRequestDTO.builder()
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .senhaAdmin("admin123")
                .build();
        
        empresa = Empresa.builder()
                .id(1L)
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .build();

        lenient().when(chamadoRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long chamadoId = invocation.getArgument(0);

            if (chamadoId == null || chamadoId <= 0 || chamadoId > 100) {
                return Optional.empty();
            }

            Chamado chamado = new Chamado();
            chamado.setId(chamadoId);
            chamado.setEmpresa(empresa);
            return Optional.of(chamado);
        });

        lenient().when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> invocation.getArgument(0));

    }

    @Test
    @DisplayName("Cadastrar empresa com sucesso")
    void testCadastrarEmpresaSucesso() {
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);
        EmpresaResponseDTO resultado = empresaService.cadastrar(empresaDTO);
        assertNotNull(resultado);
        assertEquals(empresa.getNome(), resultado.getNome());
        assertEquals(empresa.getCnpj(), resultado.getCnpj());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Recuperar empresa com sucesso")
    void testRecuperarEmpresaSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        EmpresaResponseDTO resultado = empresaService.recuperar(1L);
        assertNotNull(resultado);
        assertEquals(empresa.getId(), resultado.getId());
        verify(empresaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Alterar empresa com sucesso")
    void testAlterarEmpresaSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(empresaRepository.save(any(Empresa.class))).thenReturn(empresa);
        EmpresaResponseDTO resultado = empresaService.alterar(1L, "123456", empresaDTO);
        assertNotNull(resultado);
        assertEquals(empresa.getNome(), resultado.getNome());
        verify(empresaRepository, times(1)).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Remover empresa com sucesso")
    void testRemoverEmpresaSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        doNothing().when(empresaRepository).delete(any(Empresa.class));
        empresaService.remover(1L, "123456", "admin123");
        verify(empresaRepository, times(1)).delete(any(Empresa.class));
    }

    @Test
    @DisplayName("Alterar empresa com código de acesso inválido")
    void testAlterarEmpresaCodigoAcessoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
            empresaService.alterar(1L, "000000", empresaDTO);
        });
        verify(empresaRepository, never()).save(any(Empresa.class));
    }

    @Test
    @DisplayName("Remover empresa com código de acesso inválido")
    void testRemoverEmpresaCodigoAcessoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
            empresaService.remover(1L, "000000", "admin123");
        });
        verify(empresaRepository, never()).delete(any(Empresa.class));
    }

    @Test
    @DisplayName("Pagamento credito nao aplica desconto (empresa disponibiliza)")
    void pagamentoCreditoSemDesconto() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Credito")
                .build();

        PagamentoResponseDTO response = empresaService.confirmarPagamento(1L, 10L, "123456", request);

        assertEquals(new BigDecimal("100.00"), response.getValorFinal());
    }

    @Test
    @DisplayName("Pagamento debito aplica 2,5% de desconto")
    void pagamentoDebitoComDesconto() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Debito")
                .build();

        PagamentoResponseDTO response = empresaService.confirmarPagamento(1L, 10L, "123456", request);

        assertEquals(new BigDecimal("97.50"), response.getValorFinal());
    }

    @Test
    @DisplayName("Pagamento pix aplica 5% de desconto")
    void pagamentoPixComDesconto() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Pix")
                .build();

        PagamentoResponseDTO response = empresaService.confirmarPagamento(1L, 10L, "123456", request);

        assertEquals(new BigDecimal("95.00"), response.getValorFinal());
    }

    @Test
    @DisplayName("Pagamento com metodo desconhecido deve falhar")
    void pagamentoMetodoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Boleto")
                .build();

        assertThrows(CommerceException.class, () ->
                empresaService.confirmarPagamento(1L, 10L, "123456", request)
        );
    }

    @Test
    @DisplayName("Pagamento sem chamado valido deve falhar")
    void pagamentoSemChamado() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
                .valorTotal(new BigDecimal("100.00"))
                .metodoPagamento("Pix")
                .build();

        assertThrows(RuntimeException.class, () ->
                empresaService.confirmarPagamento(1L, 999L, "123456", request)
        );
    }

        @Test
        @DisplayName("Pagamento falha quando empresa nao existe")
        void pagamentoEmpresaInexistente() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.empty());

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
            .valorTotal(new BigDecimal("50.00"))
            .metodoPagamento("Pix")
            .build();

        assertThrows(EmpresaNaoExisteException.class, () ->
            empresaService.confirmarPagamento(1L, 10L, "123456", request)
        );
        }

        @Test
        @DisplayName("Pagamento falha com codigo de acesso invalido")
        void pagamentoCodigoAcessoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
            .valorTotal(new BigDecimal("80.00"))
            .metodoPagamento("Pix")
            .build();

        assertThrows(CodigoDeAcessoInvalidoException.class, () ->
            empresaService.confirmarPagamento(1L, 10L, "000000", request)
        );
        }

        @Test
        @DisplayName("Pagamento sem metodo informado deve falhar")
        void pagamentoSemMetodo() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
            .valorTotal(new BigDecimal("100.00"))
            .build();

        assertThrows(CommerceException.class, () ->
            empresaService.confirmarPagamento(1L, 10L, "123456", request)
        );
        }

        @Test
        @DisplayName("Pagamento com chamado zero deve falhar")
        void pagamentoChamadoZero() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        PagamentoRequestDTO request = PagamentoRequestDTO.builder()
            .valorTotal(new BigDecimal("40.00"))
            .metodoPagamento("Debito")
            .build();

        assertThrows(RuntimeException.class, () ->
            empresaService.confirmarPagamento(1L, 0L, "123456", request)
        );
        }
}