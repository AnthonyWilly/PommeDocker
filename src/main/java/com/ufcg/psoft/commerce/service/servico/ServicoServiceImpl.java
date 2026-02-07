package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.EmpresaNaoExisteException;
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
    public ServicoResponseDTO alterar(Long empresaId,Long id, String codigoAcesso,ServicoPostPutRequestDTO servicoPostPutRequestDTO) {
        validarEmpresa(empresaId, codigoAcesso);
        Servico servico = buscarServicoPeloId(id);
        modelMapper.map(servicoPostPutRequestDTO, servico);
        servicoRepository.save(servico);
        return modelMapper.map(servico, ServicoResponseDTO.class);
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
                .orElseThrow(EmpresaNaoExisteException::new);

        if (!empresa.getCodigoAcesso().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }

        return empresa;
    }

}

