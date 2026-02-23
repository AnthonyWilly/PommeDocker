package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoFiltroDTO;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.exception.ClienteNaoExisteException;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.EmpresaNaoExisteException;
import com.ufcg.psoft.commerce.exception.ServicoNaoExisteException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.ClienteRepository;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import com.ufcg.psoft.commerce.service.notificacao.ServicoObserver;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



@Service
public class ServicoServiceImpl implements ServicoService {

    private final Set<ServicoObserver> observadores = new LinkedHashSet<>();

    @Autowired
    ServicoRepository servicoRepository;
    @Autowired
    EmpresaRepository empresaRepository;
    @Autowired
    ClienteRepository clienteRepository;
    @Autowired
    ModelMapper modelMapper;

    private Servico buscarServicoPeloId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(ServicoNaoExisteException::new);
    }

    @Override
    public ServicoResponseDTO alterar(Long empresaId,Long id, String codigoAcesso,ServicoPostPutRequestDTO servicoPostPutRequestDTO) {
        validarEmpresa(empresaId, codigoAcesso);
        Servico servico = buscarServicoPeloId(id);
        modelMapper.map(servicoPostPutRequestDTO, servico);
        servicoRepository.save(servico);
        return new ServicoResponseDTO(servico);
    }

    @Override
    public ServicoResponseDTO criar(Long empresaId, String codigoAcesso, ServicoPostPutRequestDTO servicoPostPutRequestDTO) {
        Empresa empresa = validarEmpresa(empresaId, codigoAcesso);
        Servico servico = modelMapper.map(servicoPostPutRequestDTO, Servico.class);
        servico.setEmpresa(empresa);
        servicoRepository.save(servico);
        return modelMapper.map(servico, ServicoResponseDTO.class);
    }

    @Override
    public void remover(Long empresaId,Long id, String codigoAcesso) {
        validarEmpresa(empresaId, codigoAcesso);
        Servico servico = buscarServicoPeloId(id);
        servicoRepository.delete(servico);
    }

    @Override
    public List<ServicoResponseDTO> listar(Long empresaId) {
        List<Servico> servicos =
                servicoRepository.findByEmpresa_Id(empresaId);
        return servicos.stream()
                .map(ServicoResponseDTO::new)
                .toList();
    }


    @Override
    public ServicoResponseDTO recuperar(Long empresaId,Long id) {
        Servico servico = buscarServicoPeloId(id);
        return new ServicoResponseDTO(servico);
    }

    private Empresa validarEmpresa(Long empresaId, String codigoAcesso) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(EmpresaNaoExisteException::new);

        if (!empresa.getCodigoAcesso().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        return empresa;
    }

    @Override
    public ServicoResponseDTO alterarDisponibilidade(Long empresaId, Long servicoId, String codigoAcesso, boolean disponivel) {
        validarEmpresa(empresaId, codigoAcesso);
        Servico servico = buscarServicoPeloId(servicoId);
        if (!servico.getEmpresa().getId().equals(empresaId)) {
            throw new ServicoNaoExisteException();
        }
        boolean eraIndisponivel = !servico.isDisponivel();
        servico.setDisponivel(disponivel);
        servicoRepository.save(servico);
        if (disponivel && eraIndisponivel) {
            observadores.forEach(obs -> obs.notificar(servico));
            servico.getInteressados().forEach(cliente -> cliente.notificar(servico));
        }
        return new ServicoResponseDTO(servico);
    }

    @Override
    public void registrarInteresse(Long clienteId, Long servicoId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(ClienteNaoExisteException::new);
        Servico servico = buscarServicoPeloId(servicoId);
        if (!servico.getInteressados().contains(cliente)) {
            servico.getInteressados().add(cliente);
            servicoRepository.save(servico);
        }
    }

    @Override
    public void adicionarObservador(ServicoObserver observer) {
        observadores.add(observer);
    }

    @Override
    public List<ServicoResponseDTO> listarCatalogoServicoCliente(Long clienteId, ServicoFiltroDTO filtro) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteNaoExisteException());

        List<Plano> planos = new ArrayList<Plano>();
        planos.add(Plano.BASICO);

        if(Plano.PREMIUM.equals(cliente.getPlanoAtual()))
            planos.add(Plano.PREMIUM);

        List<Servico> servicos = servicoRepository.findAllComFiltros(
            filtro.getTipo(),
            filtro.getEmpresaId(),
            filtro.getUrgencia(),
            filtro.getPrecoMin(),
            filtro.getPrecoMax(),
            planos
        );

        return servicos.stream()
                .map(ServicoResponseDTO::new)
                .collect(Collectors.toList());
    }

}

