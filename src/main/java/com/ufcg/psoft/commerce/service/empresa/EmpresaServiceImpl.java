package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CommerceException;
import com.ufcg.psoft.commerce.exception.EmpresaJaCadastradaException;
import com.ufcg.psoft.commerce.exception.EmpresaNaoExisteException;
import com.ufcg.psoft.commerce.exception.SenhaInvalidaException;
import com.ufcg.psoft.commerce.exception.TecnicoNaoExisteException;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.model.Pagamento;
import com.ufcg.psoft.commerce.model.PagamentoCredito;
import com.ufcg.psoft.commerce.model.PagamentoDebito;
import com.ufcg.psoft.commerce.model.PagamentoPix;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    private static final String SENHA_ADMIN_PADRAO = "admin123";

    @Autowired
    private PagamentoCredito pagamentoCredito;

    @Autowired
    private PagamentoDebito pagamentoDebito;

    @Autowired
    private PagamentoPix pagamentoPix;

    private Map<String, Pagamento> pagamentos = new HashMap<>();

    private static final long CHAMADO_MAX_VALIDO = 100;

    @PostConstruct
    public void inicializarMapaDePagamentos() {
        pagamentos.clear();

        pagamentoCredito = pagamentoCredito != null ? pagamentoCredito : new PagamentoCredito();
        pagamentoDebito = pagamentoDebito != null ? pagamentoDebito : new PagamentoDebito();
        pagamentoPix = pagamentoPix != null ? pagamentoPix : new PagamentoPix();

        pagamentos.put(pagamentoCredito.getMetodo().toUpperCase(), pagamentoCredito);
        pagamentos.put(pagamentoDebito.getMetodo().toUpperCase(), pagamentoDebito);
        pagamentos.put(pagamentoPix.getMetodo().toUpperCase(), pagamentoPix);
    }

    @Override
    public EmpresaResponseDTO cadastrar(EmpresaPostPutRequestDTO dto) {
        validarSenhaAdmin(dto.getSenhaAdmin());

        if (empresaRepository.findByCnpj(dto.getCnpj()).isPresent()) {
            throw new EmpresaJaCadastradaException();
        }

        Empresa empresa = Empresa.builder()
                .nome(dto.getNome())
                .cnpj(dto.getCnpj())
                .codigoAcesso(dto.getCodigoAcesso())
                .build();

        return new EmpresaResponseDTO(empresaRepository.save(empresa));
    }

    @Override
    public EmpresaResponseDTO recuperar(Long id) {
        Empresa empresa = buscarEmpresaPeloId(id);
        return new EmpresaResponseDTO(empresa);
    }

    @Override
    public List<EmpresaResponseDTO> listar() {
        return empresaRepository.findAll().stream()
                .map(EmpresaResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public EmpresaResponseDTO alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO dto) {
        validarSenhaAdmin(dto.getSenhaAdmin());

        Empresa empresa = buscarEmpresaPeloId(id);
        validarCodigoAcesso(empresa, codigoAcesso);

        empresa.setNome(dto.getNome());
        empresa.setCnpj(dto.getCnpj());

        return new EmpresaResponseDTO(empresaRepository.save(empresa));
    }

    @Override
    public void remover(Long id, String codigoAcesso, String senhaAdmin) {
        validarSenhaAdmin(senhaAdmin);

        Empresa empresa = buscarEmpresaPeloId(id);
        validarCodigoAcesso(empresa, codigoAcesso);

        empresaRepository.delete(empresa);
    }

    private Empresa buscarEmpresaPeloId(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(EmpresaNaoExisteException::new);
    }

    private void validarSenhaAdmin(String senhaAdmin) {
        if (!SENHA_ADMIN_PADRAO.equals(senhaAdmin)) {
            throw new SenhaInvalidaException();
        }
    }

    private void validarCodigoAcesso(Empresa empresa, String codigoAcesso) {
        if (!empresa.getCodigoAcesso().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }
    }

    @Override
    public void aprovarTecnico(Long empresaId, Long tecnicoId, String codigoAcesso) {
        Empresa empresa = buscarEmpresaPeloId(empresaId);
        validarCodigoAcesso(empresa, codigoAcesso);
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(TecnicoNaoExisteException::new);
        if (!tecnico.getEmpresasAprovadoras().contains(empresa)) {
            tecnico.getEmpresasAprovadoras().add(empresa);
            tecnicoRepository.save(tecnico);
        }
    }

    @Override
    public void rejeitarTecnico(Long empresaId, Long tecnicoId, String codigoAcesso) {
        Empresa empresa = buscarEmpresaPeloId(empresaId);
        validarCodigoAcesso(empresa, codigoAcesso);
        
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(TecnicoNaoExisteException::new);
        
        tecnico.getEmpresasAprovadoras().remove(empresa);
        tecnicoRepository.save(tecnico);
    }

    @Override
    public PagamentoResponseDTO confirmarPagamento(Long empresaId, Long chamadoId, String codigoAcesso, PagamentoRequestDTO pagamentoRequestDTO) {
        Empresa empresa = buscarEmpresaPeloId(empresaId);
        validarCodigoAcesso(empresa, codigoAcesso);
        validarChamado(chamadoId);

        if (pagamentoRequestDTO == null) {
            throw new CommerceException("Metodo de pagamento nao suportado");
        }

        if (pagamentos.isEmpty()) {
            inicializarMapaDePagamentos();
        }

        Pagamento pagamento = obterPagamento(pagamentoRequestDTO.getMetodoPagamento());

        BigDecimal valorTotal = pagamentoRequestDTO.getValorTotal();
        if (valorTotal == null || valorTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new CommerceException("Valor total invalido");
        }

        BigDecimal valorFinal = pagamento.aplicarDesconto(valorTotal);

        return PagamentoResponseDTO.builder()
                .valorFinal(valorFinal)
                .build();
    }

    private Pagamento obterPagamento(String metodoPagamento) {
        if (metodoPagamento == null || metodoPagamento.isBlank()) {
            throw new CommerceException("Metodo de pagamento nao suportado");
        }

        Pagamento pagamento = pagamentos.get(metodoPagamento.toUpperCase());
        if (pagamento == null) {
            throw new CommerceException("Metodo de pagamento nao suportado");
        }
        return pagamento;
    }

    private void validarChamado(Long chamadoId) {
        if (chamadoId == null || chamadoId <= 0 || chamadoId > CHAMADO_MAX_VALIDO) {
            throw new CommerceException("Chamado invalido");
        }
    }
}