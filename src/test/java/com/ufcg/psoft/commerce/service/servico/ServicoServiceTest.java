package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ServicoServiceTest {
    @Mock
    private EmpresaRepository empresaRepository;
    @Spy
    private ModelMapper modelMapper = new ModelMapper();
    @Mock
    private ServicoRepository servicoRepository;
    @InjectMocks
    private ServicoServiceImpl servicoService;
    private Servico servicoEletrica;
    private ServicoPostPutRequestDTO servicoDTO;
    private Empresa empresa;
    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .build();

        servicoEletrica = Servico.builder()
                .id(10L)
                .nome("Instalacao de Chuveiro")
                .tipo(TipoServico.ELETRICA)
                .descricao("instala um chuveiro")
                .urgencia(Urgencia.NORMAL)
                .preco(150.0)
                .disponivel(true)
                .plano("Basico")
                .duracao(3.0)
                .empresa(empresa)
                .build();

        servicoDTO = ServicoPostPutRequestDTO.builder()
                .nome("Instalacao de Chuveiro")
                .tipo(TipoServico.ELETRICA)
                .urgencia(Urgencia.ALTA)
                .duracao(3.0)
                .disponivel(true)
                .descricao("instala um chuveiro")
                .preco(150.0)
                .plano("Basico")
                .build();
    }

    @Test
    @DisplayName("Adicionar serviço à empresa com sucesso")
    void testAdicionarServicoSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(servicoRepository.save(any(Servico.class))).thenReturn(servicoEletrica);

        ServicoResponseDTO resultado =
                servicoService.criar(1L, "123456", servicoDTO);

        assertNotNull(resultado);
        assertEquals("Instalacao de Chuveiro", resultado.getNome());
        verify(servicoRepository, times(1)).save(any(Servico.class));
    }

    @Test
    @DisplayName("Falhar ao adicionar serviço com código inválido")
    void testAdicionarServicoCodigoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        assertThrows(CodigoDeAcessoInvalidoException.class,
                () -> servicoService.criar(1L, "000000", servicoDTO));

        verify(servicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Alterar serviço com sucesso")
    void testAlterarServicoSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(servicoRepository.findById(10L)).thenReturn(Optional.of(servicoEletrica));
        when(servicoRepository.save(any(Servico.class)))
                .thenReturn(servicoEletrica);

        ServicoPostPutRequestDTO updateDTO = ServicoPostPutRequestDTO.builder()
                .nome("Reparo de Cano")
                .tipo(TipoServico.HIDRAULICA)
                .urgencia(Urgencia.NORMAL)
                .duracao(3.0)
                .disponivel(true)
                .descricao("instala um chuveiro")
                .preco(80.0)
                .plano("Basico")
                .build();

        ServicoResponseDTO resultado =
                servicoService.alterar(1L, 10L, "123456", updateDTO);

        assertEquals("Reparo de Cano", resultado.getNome());
    }


    @Test
    @DisplayName("Falhar ao alterar serviço com código de acesso inválido")
    void testAlterarServicoCodigoAcessoInvalido() {

        when(empresaRepository.findById(1L))
                .thenReturn(Optional.of(empresa));

        assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
            servicoService.alterar(1L, 10L, "000000", servicoDTO);
        });

        verify(servicoRepository, never()).save(any(Servico.class));
    }


    @Test
    @DisplayName("Remover serviço com sucesso")
    void testRemoverServicoSucesso() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
        when(servicoRepository.findById(10L)).thenReturn(Optional.of(servicoEletrica));
        servicoService.remover(1L, 10L, "123456");
        verify(servicoRepository, times(1)).delete(servicoEletrica);
    }


    @Test
    @DisplayName("Falhar ao remover serviço com código de acesso inválido")
    void testRemoverServicoCodigoAcessoInvalido() {
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        assertThrows(CodigoDeAcessoInvalidoException.class, () -> {
            servicoService.remover(1L, 10L, "000000");
        });

        verify(servicoRepository, never()).delete(any(Servico.class));
    }

}
