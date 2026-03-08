package com.ufcg.psoft.commerce.service.tecnico;

import com.ufcg.psoft.commerce.dto.TecnicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.TecnicoResponseDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.CommerceException;
import com.ufcg.psoft.commerce.exception.TecnicoNaoExisteException;
import com.ufcg.psoft.commerce.model.HistoricoDisponibilidade;
import com.ufcg.psoft.commerce.model.StatusDisponibilidade;
import com.ufcg.psoft.commerce.model.Tecnico;
import com.ufcg.psoft.commerce.repository.HistoricoDisponibilidadeRepository;
import com.ufcg.psoft.commerce.repository.TecnicoRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TecnicoServiceImpl implements TecnicoService {

    @Autowired
    TecnicoRepository tecnicoRepository;

    @Autowired
    HistoricoDisponibilidadeRepository historicoDisponibilidadeRepository;

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
        return tecnicoRepository
            .findById(id)
            .map(tecnico -> modelMapper.map(tecnico, TecnicoResponseDTO.class))
            .orElseThrow(() ->
                new CommerceException("O tecnico consultado nao existe!")
            );
    }

    @Override
    public List<TecnicoResponseDTO> listar() {
        return tecnicoRepository
            .findAll()
            .stream()
            .map(tecnico -> modelMapper.map(tecnico, TecnicoResponseDTO.class))
            .toList();
    }

    @Override
    public List<TecnicoResponseDTO> listarPorNome(String nome) {
        return tecnicoRepository
            .findByNomeContaining(nome)
            .stream()
            .map(tecnico -> modelMapper.map(tecnico, TecnicoResponseDTO.class))
            .toList();
    }

    @Override
    public TecnicoResponseDTO alterar(
        Long id,
        String codigoAcesso,
        TecnicoPostPutRequestDTO tecnicoDTO
    ) {
        return tecnicoRepository
            .findById(id)
            .map(tecnico -> {
                if (!tecnico.getAcesso().equals(codigoAcesso)) {
                    throw new CodigoDeAcessoInvalidoException();
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
            .orElseThrow(() ->
                new CommerceException("O tecnico consultado nao existe!")
            );
    }

    @Override
    public void remover(Long id, String codigoAcesso) {
        Tecnico tecnico = tecnicoRepository
            .findById(id)
            .orElseThrow(() ->
                new CommerceException("O tecnico consultado nao existe!")
            );
        if (!tecnico.getAcesso().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }
        tecnicoRepository.deleteById(id);
    }

    @Override
    public TecnicoResponseDTO alterarDisponibilidade(
        Long id,
        String codigoAcesso,
        StatusDisponibilidade novoStatus
    ) {
        Tecnico tecnico = tecnicoRepository
            .findById(id)
            .orElseThrow(TecnicoNaoExisteException::new);
        if (!tecnico.getAcesso().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }
        LocalDateTime agora = LocalDateTime.now();
        tecnico.setStatusDisponibilidade(novoStatus);
        tecnico.setDataUltimaMudancaDisponibilidade(agora);
        tecnicoRepository.save(tecnico);
        historicoDisponibilidadeRepository.save(
            HistoricoDisponibilidade.builder()
                .tecnicoId(tecnico.getId())
                .novoStatus(novoStatus)
                .dataHora(agora)
                .build()
        );
        return modelMapper.map(tecnico, TecnicoResponseDTO.class);
    }

    @Override
    public void validarTecnicoDisponivel(Long id) {
        Tecnico tecnico = tecnicoRepository
            .findById(id)
            .orElseThrow(TecnicoNaoExisteException::new);
        if (tecnico.getStatusDisponibilidade() != StatusDisponibilidade.ATIVO) {
            throw new CommerceException(
                "O tecnico nao esta disponivel para atendimento!"
            );
        }
    }
}
