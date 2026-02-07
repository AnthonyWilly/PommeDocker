package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import java.util.List;

public interface ServicoService {

    ServicoResponseDTO alterar(Long empresaId,Long id, String codigoAcesso,ServicoPostPutRequestDTO servicoPostPutRequestDTO);

    List<ServicoResponseDTO> listar(Long idEmpresa);

    ServicoResponseDTO recuperar(Long idEmpresa, Long id);

    ServicoResponseDTO criar(Long idEmpresa, String codigoAcesso, ServicoPostPutRequestDTO servicoPostPutRequestDTO);

    void remover(Long idEmpresa, Long id, String codigoAcesso);

}


