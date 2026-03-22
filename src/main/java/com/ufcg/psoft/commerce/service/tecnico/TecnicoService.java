package com.ufcg.psoft.commerce.service.tecnico;

import java.util.List;

import com.ufcg.psoft.commerce.dto.TecnicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.TecnicoResponseDTO;
import com.ufcg.psoft.commerce.model.StatusDisponibilidade;
import java.util.List;

public interface TecnicoService {
    TecnicoResponseDTO criar(TecnicoPostPutRequestDTO tecnicoDTO);

    TecnicoResponseDTO recuperar(Long id);

    List<TecnicoResponseDTO> listar();

    List<TecnicoResponseDTO> listarPorNome(String nome);

    TecnicoResponseDTO alterar(
        Long id,
        String codigoAcesso,
        TecnicoPostPutRequestDTO tecnicoDTO
    );

    void remover(Long id, String codigoAcesso);

    TecnicoResponseDTO alterarDisponibilidade(
        Long id,
        String codigoAcesso,
        StatusDisponibilidade novoStatus
    );

    void validarTecnicoDisponivel(Long id);

    void marcarComoOcupado(Long id);

    void marcarComoAtivo(Long id);
}
