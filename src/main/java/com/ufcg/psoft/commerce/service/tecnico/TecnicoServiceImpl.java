package com.ufcg.psoft.commerce.service.tecnico;

import com.ufcg.psoft.commerce.dto.TecnicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.TecnicoResponseDTO;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TecnicoServiceImpl implements TecnicoService {

    @Autowired
    TecnicoRepository tecnicoRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public TecnicoResponseDTO criar(TecnicoPostPutRequestDTO tecnicoDTO) {
        Tecnico tecnico = modelMapper.map(tecnicoDTO, Tecnico.class);
        tecnicoRepository.save(tecnico);
        return modelMapper.map(tecnico, TecnicoResponseDTO.class);
    }

    @Override
    public TecnicoResponseDTO recuperar(Long id) {
        return tecnicoRepository.findById(id)
                .map(tecnico -> modelMapper.map(tecnico, TecnicoResponseDTO.class))
                .orElse(null);
    }

    @Override
    public List<TecnicoResponseDTO> listar() {
        return tecnicoRepository.findAll().stream()
                .map(tecnico -> modelMapper.map(tecnico, TecnicoResponseDTO.class))
                .toList();
    }

    @Override
    public List<TecnicoResponseDTO> listarPorNome(String nome) {
        return tecnicoRepository.findByNomeContaining(nome).stream()
                .map(tecnico -> modelMapper.map(tecnico, TecnicoResponseDTO.class))
                .toList();
    }

    @Override
    public TecnicoResponseDTO alterar(Long id, String codigoAcesso, TecnicoPostPutRequestDTO tecnicoDTO) {
        return tecnicoRepository.findById(id)
                .map(tecnico -> {
                    if (!tecnico.getAcesso().equals(codigoAcesso)) {
                        throw new RuntimeException("Código de acesso inválido");
                    }
                    tecnico.setNome(tecnicoDTO.getNome());
                    tecnico.setEspecialidade(tecnicoDTO.getEspecialidade());
                    tecnico.setPlacaVeiculo(tecnicoDTO.getPlacaVeiculo());
                    tecnico.setTipoVeiculo(tecnicoDTO.getTipoVeiculo());
                    tecnico.setCorVeiculo(tecnicoDTO.getCorVeiculo());
                    tecnico.setAcesso(tecnicoDTO.getAcesso());
                    tecnicoRepository.save(tecnico);
                    return modelMapper.map(tecnico, TecnicoResponseDTO.class);
                })
                .orElse(null);
    }

    @Override
    public void remover(Long id, String codigoAcesso) {
        Tecnico tecnico = tecnicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));
        if (!tecnico.getAcesso().equals(codigoAcesso)) {
            throw new RuntimeException("Código de acesso inválido");
        }
        tecnicoRepository.deleteById(id);
    }

}
