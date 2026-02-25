package com.ufcg.psoft.commerce.service.servico;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.DemonstrarInteresseInvalidoException;
import com.ufcg.psoft.commerce.exception.DemonstrarInteressePlanoInvalidoException;
import com.ufcg.psoft.commerce.exception.ServicoNaoExisteException;
import com.ufcg.psoft.commerce.model.Cliente;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import com.ufcg.psoft.commerce.service.notificacao.ServicoObserver;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do service dos serviços")
public class ServicoServiceTests {

    @Mock
    private EmpresaRepository empresaRepository;
    @Mock
    private ServicoRepository servicoRepository;
    @Mock
    ClienteRepository clienteRepository;
    
    @Spy
    private ModelMapper modelMapper = new ModelMapper();

    @InjectMocks
    private ServicoServiceImpl servicoService;

    private Empresa empresa;
    private Cliente clienteBasico;
    private Cliente clientePremium;
    private Servico servicoEletrica;
    private ServicoPostPutRequestDTO servicoDTO;
    private Servico servicoBasico;
    private Servico servicoPremium;
    private Servico servicoDisponivelBasico;
    private Servico servicoDisponivelPremium;
    private Servico servicoIndisponivelBasico;
    private Servico servicoIndisponivelPremium;

    @BeforeEach
    void setUp() {

        empresa = Empresa.builder()
                .id(1L)
                .nome("Empresa Exemplo")
                .cnpj("12345678901234")
                .codigoAcesso("123456")
                .build();
        
       clienteBasico = Cliente.builder()
                .id(1L)
                .nome("João da Silva")
                .endereco("Rua A, 123")
                .codigo("123456")
                .planoAtual(Plano.BASICO)
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build();

        clientePremium = Cliente.builder()
                .id(2L)
                .nome("João da Silva 2")
                .endereco("Rua B, 123")
                .codigo("654321")
                .planoAtual(Plano.PREMIUM)
                .dataCobranca((LocalDate.now().plusDays(30)))
                .proxPlano(null)
                .build(); 

        servicoEletrica = Servico.builder()
                .id(10L)
                .nome("Instalacao de Chuveiro")
                .tipo(TipoServico.ELETRICA)
                .descricao("instala um chuveiro")
                .urgencia(Urgencia.NORMAL)
                .preco(150.0)
                .disponivel(true)
                .plano(Plano.BASICO)
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
                .plano(Plano.BASICO)
                .build();

        servicoBasico = Servico.builder()
                .id(1L)
                .nome("Reparo Hidraulico")
                .tipo(TipoServico.HIDRAULICA)
                .preco(100.0)
                .plano(Plano.BASICO)
                .empresa(empresa)
                .build();

        servicoPremium = Servico.builder()
                .id(2L)
                .nome("Guinchar carro")
                .tipo(TipoServico.LIMPEZA)
                .preco(500.0)
                .plano(Plano.PREMIUM)
                .empresa(empresa)
                .build();

        servicoDisponivelBasico = Servico.builder()
                .id(3L)
                .nome("Instalacao de Chuveiro")
                .tipo(TipoServico.ELETRICA)
                .descricao("instala um chuveiro")
                .urgencia(Urgencia.NORMAL)
                .preco(150.0)
                .disponivel(true)
                .plano(Plano.BASICO)
                .duracao(3.0)
                .empresa(empresa)
                .build();
                
        servicoDisponivelPremium = Servico.builder()
                .id(4L)
                .nome("Instalacao de Chuveiro de última geração")
                .tipo(TipoServico.ELETRICA)
                .descricao("instala um chuveiro que cobrimos o custo caso a fiação da sua casa queime")
                .urgencia(Urgencia.ALTA)
                .preco(350.0)
                .disponivel(true)
                .plano(Plano.PREMIUM)
                .duracao(4.0)
                .empresa(empresa)
                .build();

        servicoIndisponivelBasico = Servico.builder()
                .id(5L)
                .nome("Pintura de parede")
                .tipo(TipoServico.PINTURA)
                .descricao("Pintura de uma parede")
                .urgencia(Urgencia.BAIXA)
                .preco(150.0)
                .disponivel(false)
                .plano(Plano.BASICO)
                .duracao(4.0)
                .empresa(empresa)
                .build();

        servicoIndisponivelPremium = Servico.builder()
                .id(6L)
                .nome("Pintura de parede alto padrão")
                .tipo(TipoServico.PINTURA)
                .descricao("Pintura de uma parede no estilo alto padrão")
                .urgencia(Urgencia.ALTA)
                .preco(550.0)
                .disponivel(false)
                .plano(Plano.PREMIUM)
                .duracao(8.0)
                .empresa(empresa)
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
                .plano(Plano.BASICO)
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

    @Nested
    @DisplayName("Conlunto de testes de listagem do catálogo de serviços")
    class ListagemDeServico {

        @Test
        @DisplayName("Listar serviços básicos para clientes básicos")
        void listarServicosBasicos() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO))))
                .thenReturn(Arrays.asList(servicoBasico));

            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), new ServicoFiltroDTO());

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO)))
            );
        }

        @Test
        @DisplayName("Listar serviços premium para clientes premium")
        void listarServicosPremium() {

            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO, Plano.PREMIUM))))
                .thenReturn(Arrays.asList(servicoBasico, servicoPremium));
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clientePremium.getId(), new ServicoFiltroDTO());

            // Assert
            assertAll(
                () -> assertEquals(2, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> assertEquals("Guinchar carro", resultado.get(1).getNome()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO, Plano.PREMIUM)))
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando o cliente não existir")
        void quandoClienteNaoExistir() {
            // Arrange
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                servicoService.listarCatalogoServicoCliente(99L, new ServicoFiltroDTO());
            });
        } 

        @Test
        @DisplayName("Listar serviços por tipo existente")
        void listarServicosPorTipoExistente() {

            // arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findAllComFiltros(eq(TipoServico.HIDRAULICA), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO, Plano.PREMIUM))))
                .thenReturn(Arrays.asList(servicoBasico));
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .tipo(TipoServico.HIDRAULICA)
                                .build(); 
            // act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clientePremium.getId(), filtro); 

            // assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals("Reparo Hidraulico", resultado.get(0).getNome()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(eq(TipoServico.HIDRAULICA), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO, Plano.PREMIUM)))
            );
        }

        @Test
        @DisplayName("Listar serviços por tipo inexistente")
        void listarServicosPorTipoInexistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findAllComFiltros(eq(TipoServico.LIMPEZA), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO))))
                .thenReturn(Arrays.asList());
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .tipo(TipoServico.LIMPEZA)
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), filtro);

            // Assert
            assertAll(
                () -> assertEquals(0, resultado.size()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(eq(TipoServico.LIMPEZA), any(), any(), any(), any(), eq(Arrays.asList(Plano.BASICO)))
            );
        }

        @Test
        @DisplayName("Listar serviços por empresa existente")
        void listarServicosPorEmpresaExistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findAllComFiltros(any(), eq(empresa.getId()), any(), any(), any(), eq(Arrays.asList(Plano.BASICO))))
                .thenReturn(Arrays.asList(servicoBasico));
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .empresaId(empresa.getId())
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), filtro);

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertEquals(1L, resultado.get(0).getEmpresaId()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), eq(empresa.getId()), any(), any(), any(), eq(Arrays.asList(Plano.BASICO)))
            );
        }

        @Test
        @DisplayName("Listar serviços por empresa inexistente")
        void listarServicosPorEmpresaInexistente() {

            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findAllComFiltros(any(), eq(2L), any(), any(), any(), eq(Arrays.asList(Plano.BASICO))))
                .thenReturn(Arrays.asList());
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .empresaId(2L)
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clienteBasico.getId(), filtro); 

            // Assert
            assertAll(
                () -> assertEquals(0, resultado.size()),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), eq(2L), any(), any(), any(), eq(Arrays.asList(Plano.BASICO)))
            );
        }

        @Test
        @DisplayName("Listar serviços por faixa de preço")
        void listarServicosPorFaixaDePreco() {

            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findAllComFiltros(any(), any(), any(), any(), eq(200.00), eq(Arrays.asList(Plano.BASICO, Plano.PREMIUM))))
                .thenReturn(Arrays.asList(servicoBasico));
            ServicoFiltroDTO filtro = ServicoFiltroDTO.builder()
                                .precoMax(200.00)
                                .build(); 
            
            // Act
            List<ServicoResponseDTO> resultado = servicoService.listarCatalogoServicoCliente(clientePremium.getId(), filtro);

            // Assert
            assertAll(
                () -> assertEquals(1, resultado.size()),
                () -> assertTrue(resultado.get(0).getPreco() <= 200.00),
                () -> verify(servicoRepository, times(1)).findAllComFiltros(any(), any(), any(), any(), eq(200.00), eq(Arrays.asList(Plano.BASICO, Plano.PREMIUM)))
            );
        }
    }

    @Nested
    @DisplayName("Testes de demonstração de interesse com sucesso")
    class registrarInteresseSucesso {

        @Test
        @DisplayName("Deve demonstrar interesse com sucesso quando serviço indisponível e cliente compatível com plano básico")
        void registrarInteresseComSucessoBasico() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(5L)).thenReturn(Optional.of(servicoIndisponivelBasico));
            
            when(servicoRepository.save(any(Servico.class))).thenAnswer(r -> r.getArguments()[0]);

            // Act
            servicoService.registrarInteresse(1L, 5L);

            // Assert
            assertAll(
                    () -> assertTrue(servicoIndisponivelBasico.getInteressados().contains(clienteBasico)),
                    () -> verify(servicoRepository, times(1)).save(any(Servico.class))
            );
        }
        
        @Test
        @DisplayName("Deve demonstrar interesse com sucesso quando serviço indisponível e cliente compatível com plano premium")
        void registrarInteresseComSucessoPremium() {
            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findById(6L)).thenReturn(Optional.of(servicoIndisponivelPremium));
            
            when(servicoRepository.save(any(Servico.class))).thenAnswer(r -> r.getArguments()[0]);

            // Act
            servicoService.registrarInteresse(2L, 6L);

            // Assert
            assertAll(
                    () -> assertTrue(servicoIndisponivelPremium.getInteressados().contains(clientePremium)),
                    () -> verify(servicoRepository, times(1)).save(any(Servico.class))
            );
        }

        @Test
        @DisplayName("Deve demonstrar interesse com sucesso quando cliente premium se interessa por serviço básico")
        void registrarServicoPremiumParaBasico() {
            // Arrange
            when(clienteRepository.findById(2L)).thenReturn(Optional.of(clientePremium));
            when(servicoRepository.findById(5L)).thenReturn(Optional.of(servicoIndisponivelBasico));
            when(servicoRepository.save(any(Servico.class))).thenAnswer(i -> i.getArguments()[0]);

            // Act
            servicoService.registrarInteresse(2L, 5L);

            // Assert
            assertAll(
                    () -> assertTrue(servicoIndisponivelBasico.getInteressados().contains(clientePremium)),
                    () -> verify(servicoRepository, times(1)).save(any(Servico.class))
            );
        }
    }

    @Nested
    @DisplayName("Testes de demonstração de interesse que devem lançar erro")
    class registrarInteresseFalha {

        @Test
        @DisplayName("Deve lançar erro ao tentar demonstrar interesse em serviço disponível")
        void registrarInteresseEmServicoDisponivel() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(3L)).thenReturn(Optional.of(servicoDisponivelBasico));

            // Act & Assert
            RuntimeException exception = assertThrows(DemonstrarInteresseInvalidoException.class, 
                () -> servicoService.registrarInteresse(1L, 3L)
            );

            assertAll(
                    () -> assertEquals("Nao e possivel demonstrar interesse em servico disponivel.", exception.getMessage()),
                    () -> verify(servicoRepository, never()).save(any())
            );
        }

        @Test
        @DisplayName("Deve lançar erro quando cliente básico tenta serviço premium")
        void registrarInteresseClienteBasicoServicoPremium() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(6L)).thenReturn(Optional.of(servicoIndisponivelPremium));

            // Act & Assert
            RuntimeException exception = assertThrows(DemonstrarInteressePlanoInvalidoException.class, 
                () -> servicoService.registrarInteresse(1L, 6L));

            assertAll(
                    () -> assertEquals("Nao e possivel demonstrar interesse a um servico premium com seu plano atual.", exception.getMessage()),
                    () -> verify(servicoRepository, never()).save(any())
            );
        }

        @Test
        @DisplayName("Deve lançar erro se o cliente não existir")
        void registrarInteresseClienteInexistente() {
            // Arrange
            when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(ClienteNaoExisteException.class,
                () -> servicoService.registrarInteresse(99L, 3L));
            
            assertAll(
                () -> assertEquals("O cliente consultado nao existe!", exception.getMessage()),
                () -> verify(servicoRepository, never()).save(any())
            );
        }

        @Test
        @DisplayName("Deve lançar exceção se o serviço não existir")
        void registrarInteresseServicoInexistente() {
            // Arrange
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

            // Act & Assert
            RuntimeException exception = assertThrows(ServicoNaoExisteException.class, () -> {
                servicoService.registrarInteresse(1L, 99L);
            });

            assertEquals("O servico consultado nao existe", exception.getMessage());
            verify(servicoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Disponibilidade de serviço")
    class DisponibilidadeDeServico {

        ServicoObserver observer;
        Servico servicoIndisponivel;
        Servico servicoJaDisponivel;

        @BeforeEach
        void setupDisponibilidade() {
            observer = mock(ServicoObserver.class);
            servicoService.adicionarObservador(observer);

            servicoIndisponivel = Servico.builder()
                    .id(10L)
                    .nome("Instalacao de Chuveiro")
                    .tipo(TipoServico.ELETRICA)
                    .urgencia(Urgencia.NORMAL)
                    .descricao("instala um chuveiro")
                    .preco(150.0)
                    .duracao(3.0)
                    .disponivel(false)
                    .plano(Plano.BASICO)
                    .empresa(empresa)
                    .build();

            servicoJaDisponivel = Servico.builder()
                    .id(11L)
                    .nome("Reparo Hidraulico")
                    .tipo(TipoServico.HIDRAULICA)
                    .urgencia(Urgencia.BAIXA)
                    .descricao("reparo completo")
                    .preco(100.0)
                    .duracao(1.0)
                    .disponivel(true)
                    .plano(Plano.BASICO)
                    .empresa(empresa)
                    .build();
        }

        @Test
        @DisplayName("marcar serviço como disponível com código válido")
        void marcarServicoComoDisponivelComCodigoValido() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(servicoRepository.findById(10L)).thenReturn(Optional.of(servicoIndisponivel));
            when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServicoResponseDTO resultado = servicoService.alterarDisponibilidade(1L, 10L, "123456", true);

            assertTrue(resultado.getDisponivel());
        }

        @Test
        @DisplayName("marcar serviço como indisponível com código válido")
        void marcarServicoComoIndisponivelComCodigoValido() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(servicoRepository.findById(11L)).thenReturn(Optional.of(servicoJaDisponivel));
            when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServicoResponseDTO resultado = servicoService.alterarDisponibilidade(1L, 11L, "123456", false);

            assertFalse(resultado.getDisponivel());
        }

        @Test
        @DisplayName("lança exceção ao alterar disponibilidade com código inválido")
        void lancaExcecaoComCodigoAcessoInvalidoNaAlteracaoDisponibilidade() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

            assertThrows(CodigoDeAcessoInvalidoException.class,
                    () -> servicoService.alterarDisponibilidade(1L, 10L, "000000", true));

            verify(servicoRepository, never()).save(any());
        }

        @Test
        @DisplayName("notifica observador ao mudar de indisponível para disponível")
        void notificarObservadorQuandoServicoFicaDisponivel() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(servicoRepository.findById(10L)).thenReturn(Optional.of(servicoIndisponivel));
            when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            servicoService.alterarDisponibilidade(1L, 10L, "123456", true);

            verify(observer, times(1)).notificar(any(Servico.class));
        }

        @Test
        @DisplayName("não notifica observador ao mudar para indisponível")
        void naoNotificarObservadorQuandoServicoFicaIndisponivel() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(servicoRepository.findById(11L)).thenReturn(Optional.of(servicoJaDisponivel));
            when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            servicoService.alterarDisponibilidade(1L, 11L, "123456", false);

            verify(observer, never()).notificar(any(Servico.class));
        }

        @Test
        @DisplayName("não notifica observador quando disponibilidade não muda")
        void naoNotificarObservadorQuandoDisponibilidadeNaoMuda() {
            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(servicoRepository.findById(11L)).thenReturn(Optional.of(servicoJaDisponivel));
            when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            servicoService.alterarDisponibilidade(1L, 11L, "123456", true);

            verify(observer, never()).notificar(any(Servico.class));
        }

        @Test
        @DisplayName("notifica apenas uma vez por mudança de estado para disponível")
        void notificarObservadorUmaVezQuandoServicoFicaDisponivel() {
            servicoService.adicionarObservador(observer);

            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(servicoRepository.findById(10L)).thenReturn(Optional.of(servicoIndisponivel));
            when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            servicoService.alterarDisponibilidade(1L, 10L, "123456", true);

            verify(observer, times(1)).notificar(any(Servico.class));
        }

        @Test
        @DisplayName("registrar interesse adiciona cliente como interessado no serviço")
        void adicionarClienteComoInteressado() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(10L)).thenReturn(Optional.of(servicoIndisponivel));

            servicoService.registrarInteresse(1L, 10L);

            verify(clienteRepository, times(1)).findById(1L);
            verify(servicoRepository, times(1)).findById(10L);
        }

        @Test
        @DisplayName("notifica cliente interessado quando serviço fica disponível")
        void notificarClienteInteressadoQuandoServicoFicaDisponivel() {
            when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteBasico));
            when(servicoRepository.findById(10L)).thenReturn(Optional.of(servicoIndisponivel));
            servicoService.registrarInteresse(1L, 10L);

            when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));
            when(servicoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ArgumentCaptor<Servico> captor = ArgumentCaptor.forClass(Servico.class);
            servicoService.alterarDisponibilidade(1L, 10L, "123456", true);

            verify(observer, times(1)).notificar(captor.capture());
            assertEquals(10L, captor.getValue().getId());
        }
    }

}
