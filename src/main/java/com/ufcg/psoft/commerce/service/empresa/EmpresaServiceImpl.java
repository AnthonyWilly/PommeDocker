package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoResponseDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CommerceException;
import com.ufcg.psoft.commerce.exception.EmpresaJaCadastradaException;
import com.ufcg.psoft.commerce.exception.EmpresaNaoExisteException;
import com.ufcg.psoft.commerce.exception.SenhaInvalidaException;
import com.ufcg.psoft.commerce.exception.TecnicoNaoExisteException;
import com.ufcg.psoft.commerce.exception.ResourceNotFoundException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.HistoricoDisponibilidadeRepository;
import com.ufcg.psoft.commerce.repository.ChamadoRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import jakarta.persistence.Transient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Autowired
    private HistoricoDisponibilidadeRepository HistoricoDisponibilidadeRepository;

    private static final String SENHA_ADMIN_PADRAO = "admin123";

    @Autowired
    private Pagamento pagamento;

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
    @Transactional
    public void remover(Long id, String codigoAcesso, String senhaAdmin) {
        validarSenhaAdmin(senhaAdmin);

        Empresa empresa = buscarEmpresaPeloId(id);
        validarCodigoAcesso(empresa, codigoAcesso);

        chamadoRepository.deleteAllByEmpresa(empresa);
        servicoRepository.deleteAllByEmpresa_Id(empresa.getId());
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
    @Transactional
    public PagamentoResponseDTO confirmarPagamento(Long empresaId, Long chamadoId, String codigoAcesso, PagamentoRequestDTO pagamentoRequestDTO) {
        Empresa empresa = buscarEmpresaPeloId(empresaId);
        Chamado chamado = buscarChamadoPeloId(chamadoId);

        validarCodigoAcesso(empresa, codigoAcesso);
        validarChamado(empresa, chamado);

        if (pagamentoRequestDTO == null) {
            throw new CommerceException("Metodo de pagamento nao suportado");
        }

        BigDecimal valorFinal = pagamento.calcularValorFinal(
                pagamentoRequestDTO.getMetodoPagamento(),
                pagamentoRequestDTO.getValorTotal()
        );

        chamado.confirmarPagamento();
        chamadoRepository.save(chamado);
        
        return PagamentoResponseDTO.builder()
                .valorFinal(valorFinal)
                .build();
    }

    private Chamado buscarChamadoPeloId(Long chamadoId) {
        if (chamadoId == null || chamadoId <= 0) {
            throw new CommerceException("Chamado invalido");
        }

        return chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CommerceException("Chamado invalido"));
    }

    private void validarChamado(Empresa empresa, Chamado chamado) {
        if (chamado == null || chamado.getId() == null || chamado.getId() <= 0) {
            throw new CommerceException("Chamado invalido");
        }
        if (!chamado.getEmpresa().getId().equals(empresa.getId())) {
            throw new CommerceException("Chamado nao pertence a empresa");
        }
    }
    @Override
    @Transactional
    public ChamadoResponseDTO avancarStatus(Long empresaId, String codigoAcesso, Long chamadoId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa não encontrada."));

        if (!empresa.getCodigoAcesso().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado."));

        if (!chamado.getEmpresa().getId().equals(empresa.getId())) {
            throw new RuntimeException("O chamado não pertence à empresa informada.");
        }

        chamado.getEstado().avancar(chamado);

        if (chamado.getStatus().equals(ChamadoStatus.AGUARDANDO_TECNICO.getNome())) {

            Optional<Tecnico> tecnicoDisponivel = tecnicoRepository.findTecnicoAtivoMaisTempoParaEmpresa(empresaId);

            if (tecnicoDisponivel.isPresent()) {
                Tecnico tecnico = tecnicoDisponivel.get();
                chamado.atribuirTecnico(tecnico);
                mudarStatusTecnico(tecnico, StatusDisponibilidade.OCUPADO);
            }
            
        }

        Chamado chamadoSalvo = chamadoRepository.save(chamado);

        return ChamadoResponseDTO.builder()
                .id(chamadoSalvo.getId())
                .status(chamadoSalvo.getStatus())
                .clienteId(chamadoSalvo.getCliente() != null ? chamadoSalvo.getCliente().getId() : null)
                .empresaId(chamadoSalvo.getEmpresa() != null ? chamadoSalvo.getEmpresa().getId() : null)
                .servicoId(chamadoSalvo.getServico() != null ? chamadoSalvo.getServico().getId() : null)
                .enderecoAtendimento(chamadoSalvo.getEnderecoAtendimento())
                .build();
    }

    @Override
    @Transactional
    public ChamadoResponseDTO atribuirTecnico(Long empresaId, String codigoAcesso, Long chamadoId, Long tecnicoId) {
        Empresa empresa = buscarEmpresaPeloId(empresaId);
        validarCodigoAcesso(empresa, codigoAcesso);
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado."));
        validarChamado(empresa, chamado);
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(TecnicoNaoExisteException::new);
        if (!tecnico.getEmpresasAprovadoras().contains(empresa)) {
            throw new RuntimeException("O técnico não está aprovado por esta empresa.");
        }
        chamado.atribuirTecnico(tecnico);
        Chamado chamadoSalvo = chamadoRepository.save(chamado);
        return ChamadoResponseDTO.builder()
                .id(chamadoSalvo.getId())
                .status(chamadoSalvo.getStatus())
                .clienteId(chamadoSalvo.getCliente() != null ? chamadoSalvo.getCliente().getId() : null)
                .empresaId(chamadoSalvo.getEmpresa() != null ? chamadoSalvo.getEmpresa().getId() : null)
                .servicoId(chamadoSalvo.getServico() != null ? chamadoSalvo.getServico().getId() : null)
                .enderecoAtendimento(chamadoSalvo.getEnderecoAtendimento())
                .build();
    }

    private void mudarStatusTecnico(Tecnico tecnico, StatusDisponibilidade novoStatus) {
        tecnico.setStatusDisponibilidade(novoStatus);
        tecnico.setDataUltimaMudancaDisponibilidade(LocalDateTime.now());
        tecnicoRepository.save(tecnico);

        HistoricoDisponibilidade historico = HistoricoDisponibilidade.builder()
                .tecnicoId(tecnico.getId())
                .novoStatus(novoStatus)
                .dataHora(LocalDateTime.now())
                .build();
        historicoDisponibilidadeRepository.save(historico);
    }

}