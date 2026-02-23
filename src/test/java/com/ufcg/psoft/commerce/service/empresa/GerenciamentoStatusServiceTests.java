package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.ChamadoRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
@DisplayName("Testes de Serviço de Gerenciamento de Status ")
public class GerenciamentoStatusServiceTests {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ChamadoRepository chamadoRepository;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

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

        chamado = Chamado.builder()
                .id(10L)
                .empresa(empresa)
                .status("Chamado recebido")
                .build();
    }

    @Nested
    @DisplayName("Cenários de Sucesso")
    class SucessoTests {

        @Test
        @DisplayName("Avança estado de 'Chamado recebido' para 'Em análise' com sucesso")
        void avancarEstadoRecebidoParaAnalise() {
            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> {
                Chamado c = invocation.getArgument(0);
                c.setStatus("Em análise");
                return c;
            });

            ChamadoResponseDTO resultado = empresaService.avancarStatus(empresa.getId(), CODIGO_ACESSO, chamado.getId());

            assertNotNull(resultado);
            assertEquals("Em análise", resultado.getStatus());
            verify(chamadoRepository, times(1)).save(chamado);
        }

        @Test
        @DisplayName("Avança estado de 'Em análise' para 'Aguardando técnico' com sucesso")
        void avancarEstadoAnaliseParaAguardandoTecnico() {
            chamado.setStatus("Em análise");

            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));

            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> {
                Chamado c = invocation.getArgument(0);
                c.setStatus("Aguardando técnico");
                return c;
            });

            ChamadoResponseDTO resultado = empresaService.avancarStatus(empresa.getId(), CODIGO_ACESSO, chamado.getId());

            assertEquals("Aguardando técnico", resultado.getStatus());
            verify(chamadoRepository, times(1)).save(any(Chamado.class));
        }
    }

    @Nested
    @DisplayName("Cenários de Erro e Validação")
    class FalhaTests {

        @Test
        @DisplayName("Lança exceção ao tentar avançar status com código de acesso inválido")
        void avancarEstadoFalhaCodigoAcesso() {
            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));

            assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
                empresaService.avancarStatus(empresa.getId(), "000000", chamado.getId());
            });

            verify(chamadoRepository, never()).save(any(Chamado.class));
        }

        @Test
        @DisplayName("Lança exceção se o chamado não pertencer à empresa informada")
        void avancarEstadoChamadoDeOutraEmpresa() {
            Empresa outraEmpresa = Empresa.builder().id(2L).codigoAcesso("654321").build();
            
            when(empresaRepository.findById(outraEmpresa.getId())).thenReturn(Optional.of(outraEmpresa));
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            
            assertThrows(RuntimeException.class, () -> {
                empresaService.avancarStatus(outraEmpresa.getId(), "654321", chamado.getId());
            });
            
            verify(chamadoRepository, never()).save(any(Chamado.class));
        }
    }
}