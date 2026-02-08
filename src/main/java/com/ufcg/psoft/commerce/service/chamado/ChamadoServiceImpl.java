package com.ufcg.psoft.commerce.service.chamado;

import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.exception.*;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChamadoServiceImpl implements ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ServicoRepository servicoRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ChamadoResponseDTO criarChamado(Long clienteId, String codigoAcesso, ChamadoPostPutRequestDTO dto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(ClienteNaoExisteException::new);
        
        Empresa empresa = empresaRepository.findById(dto.getEmpresaId())
                .orElseThrow(EmpresaNaoExisteException::new);
        
        Servico servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(ServicoNaoExisteException::new);

        if (!cliente.getCodigo().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        
        boolean servicoIsPremium = servico.getPlano() == Plano.PREMIUM;
        boolean clienteIsPremium = cliente.getPlanoAtual() == Plano.PREMIUM; 

        if (servicoIsPremium && !clienteIsPremium) {
            throw new PlanoInvalidoException();
        }

        String enderecoFinal = dto.getEnderecoAtendimento();
        if (enderecoFinal == null || enderecoFinal.isBlank()) {
            enderecoFinal = cliente.getEndereco();
        }

        ChamadoEstado estadoInicial = new ChamadoEstadoAguardandoPagamento();

        Chamado chamado = Chamado.builder()
                .cliente(cliente)
                .empresa(empresa)
                .servico(servico)
                .enderecoAtendimento(enderecoFinal)
                .dataCriacao(LocalDateTime.now())
                .estado(estadoInicial)
                .status(estadoInicial.getNome())
                .build();
        
        Chamado salvo = chamadoRepository.save(chamado);
        return modelMapper.map(salvo, ChamadoResponseDTO.class);
    }

    @Override
    public ChamadoResponseDTO confirmarPagamento(Long chamadoId, String codigoAcesso, String metodoPagamento) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CommerceException("Chamado não encontrado"));

        if (!chamado.getCliente().getCodigo().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        chamado.confirmarPagamento();
        
        Chamado salvo = chamadoRepository.save(chamado);
        return modelMapper.map(salvo, ChamadoResponseDTO.class);
    }

    @Override
    public void removerChamado(Long chamadoId, String codigoAcesso) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CommerceException("Chamado não encontrado"));

        boolean isCliente = chamado.getCliente().getCodigo().equals(codigoAcesso);
        boolean isEmpresa = chamado.getEmpresa().getCodigoAcesso().equals(codigoAcesso);

        if (!isCliente && !isEmpresa) {
            throw new CodigoDeAcessoInvalidoException();
        }

        chamadoRepository.delete(chamado);
    }
    
    @Override
    public List<ChamadoResponseDTO> listarChamados(Long clienteId, String codigoAcesso) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(ClienteNaoExisteException::new);

        if (!cliente.getCodigo().equals(codigoAcesso)) {
             throw new CodigoDeAcessoInvalidoException();
        }

         return chamadoRepository.findAll().stream()
                 .filter(c -> c.getCliente().getId().equals(clienteId))
                 .map(c -> modelMapper.map(c, ChamadoResponseDTO.class))
                 .collect(Collectors.toList());
    }

    @Override
    public ChamadoResponseDTO buscarChamado(Long chamadoId) {
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new CommerceException("Chamado não encontrado"));
        return modelMapper.map(chamado, ChamadoResponseDTO.class);
    }
}