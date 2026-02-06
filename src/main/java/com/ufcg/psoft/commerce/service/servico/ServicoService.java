package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import java.util.List;

public interface ServicoService {

    ServicoResponseDTO alterar(Long id, ServicoPostPutRequestDTO servicoPostPutRequestDTO);

    List<ServicoResponseDTO> listar();

    ServicoResponseDTO recuperar(Long id);

    ServicoResponseDTO criar(ServicoPostPutRequestDTO servicoPostPutRequestDTO);

    void remover(Long idEmpresa, Long idServico);

    List<ServicoResponseDTO> listarPorNome(String nome);
}


