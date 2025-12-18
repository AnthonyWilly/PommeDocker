package com.ufcg.psoft.commerce.service.tecnico;

import com.ufcg.psoft.commerce.dto.TecnicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.TecnicoResponseDTO;

import java.util.List;

public interface TecnicoService {

    TecnicoResponseDTO criar(TecnicoPostPutRequestDTO tecnicoDTO);

    TecnicoResponseDTO recuperar(Long id);

    List<TecnicoResponseDTO> listar();

    List<TecnicoResponseDTO> listarPorNome(String nome);

    TecnicoResponseDTO alterar(Long id, String codigoAcesso, TecnicoPostPutRequestDTO tecnicoDTO);

    void remover(Long id, String codigoAcesso);
}
