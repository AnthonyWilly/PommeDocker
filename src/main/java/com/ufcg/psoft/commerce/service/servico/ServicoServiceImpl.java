package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.exception.ServicoNaoExisteException;
import com.ufcg.psoft.commerce.model.*;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import com.ufcg.psoft.commerce.repository.ServicoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicoServiceImpl implements ServicoService {
    @Autowired
    ServicoRepository servicoRepository;
    @Autowired
    EmpresaRepository empresaRepository;
    @Autowired
    ModelMapper modelMapper;

    private Servico buscarServicoPeloId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(ServicoNaoExisteException::new);
    }

    @Override
    public ServicoResponseDTO alterar(Long empresaId,Long id, ServicoPostPutRequestDTO servicoPostPutRequestDTO, String codigoAcesso) {
        Servico servico = buscarServicoPeloId(id);
        Empresa empresa = validarEmpresa(empresaId, codigoAcesso);
        modelMapper.map(servicoPostPutRequestDTO, servico);

        return modelMapper.map(servico, ServicoResponseDTO.class);
    }

    @Override
    public ServicoResponseDTO criar(ServicoPostPutRequestDTO servicoPostPutRequestDTO,  String codigoAcesso, Long empresaId) {
        Empresa empresa = validarEmpresa(empresaId, codigoAcesso);
        Servico servico = modelMapper.map(servicoPostPutRequestDTO, Servico.class);
        servico.setEmpresa(empresa);
        servicoRepository.save(servico);
        return modelMapper.map(servico, ServicoResponseDTO.class);
    }

    @Override
    public void remover(Long empresaId,Long id, String codigoAcesso) {
        Servico servico = buscarServicoPeloId(id);
        Empresa empresa = validarEmpresa(empresaId, codigoAcesso);
        servicoRepository.delete(servico);
    }

    @Override
    public List<ServicoResponseDTO> listar(Long empresaId) {
        List<Servico> servicos = servicoRepository.findByIdEmpresa(empresaId);
        return servicos.stream()
                .map(ServicoResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public ServicoResponseDTO recuperar(Long empresaId,Long id) {
        Servico servico = buscarServicoPeloId(id);
        return new ServicoResponseDTO(servico);
    }

    private Empresa validarEmpresa(Long empresaId, String codigoAcesso) {

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa nao encontrada"));

        if (!empresa.getCodigoAcesso().equals(codigoAcesso)) {
            throw new RuntimeException("Codigo de acesso invalido!");
        }

        return empresa;
    }

}

