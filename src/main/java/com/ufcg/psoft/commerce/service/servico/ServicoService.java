package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import java.util.List;

public interface ServicoService {

    ServicoResponseDTO alterar(Long id, ServicoPostPutRequestDTO servicoPostPutRequestDTO, String codigoAcesso);

    List<ServicoResponseDTO> listar();

    ServicoResponseDTO recuperar(Long id);

    ServicoResponseDTO criar(ServicoPostPutRequestDTO servicoPostPutRequestDTO, String codigoAcesso);

    void remover(Long idEmpresa, Long idServico, String codigoAcesso);

    List<ServicoResponseDTO> listarPorNome(String nome);
}


