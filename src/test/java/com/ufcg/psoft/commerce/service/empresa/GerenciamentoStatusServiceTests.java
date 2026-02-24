package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
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

    @Mock
    private TecnicoRepository tecnicoRepository;

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
    @DisplayName("Cenários de Sucesso")
    class SucessoTests {

        @Test
        @DisplayName("Avança estado de 'Chamado recebido' para 'Em análise' com sucesso")
        void avancarEstadoRecebidoParaAnalise() {
            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> {
                Chamado c = invocation.getArgument(0);
                c.setStatus("EM_ANALISE");
                return c;
            });

            ChamadoResponseDTO resultado = empresaService.avancarStatus(empresa.getId(), CODIGO_ACESSO, chamado.getId());

            assertNotNull(resultado);
            assertEquals("EM_ANALISE", resultado.getStatus());
            verify(chamadoRepository, times(1)).save(chamado);
        }

        @Test
        @DisplayName("Avança estado de 'Em análise' para 'Aguardando técnico' com sucesso")
        void avancarEstadoAnaliseParaAguardandoTecnico() {
            chamado.setStatus("EM_ANALISE");

            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));

            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> {
                Chamado c = invocation.getArgument(0);
                c.setStatus("AGUARDANDO_TECNICO");
                return c;
            });

            ChamadoResponseDTO resultado = empresaService.avancarStatus(empresa.getId(), CODIGO_ACESSO, chamado.getId());

            assertEquals("AGUARDANDO_TECNICO", resultado.getStatus());
            verify(chamadoRepository, times(1)).save(any(Chamado.class));
        }

        @Test
        @DisplayName("Atribui técnico com sucesso quando chamado está Aguardando Técnico")
        void atribuirTecnicoComSucesso() {
            chamado.setStatus("AGUARDANDO_TECNICO");
            Tecnico tecnico = Tecnico.builder().id(300L).build();
            tecnico.getEmpresasAprovadoras().add(empresa);

            when(empresaRepository.findById(empresa.getId())).thenReturn(Optional.of(empresa));
            when(chamadoRepository.findById(chamado.getId())).thenReturn(Optional.of(chamado));
            when(tecnicoRepository.findById(tecnico.getId())).thenReturn(Optional.of(tecnico));
            
            when(chamadoRepository.save(any(Chamado.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ChamadoResponseDTO resultado = empresaService.atribuirTecnico(empresa.getId(), CODIGO_ACESSO, chamado.getId(), tecnico.getId());

            assertNotNull(resultado);
            assertEquals("EM_ATENDIMENTO", resultado.getStatus());
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