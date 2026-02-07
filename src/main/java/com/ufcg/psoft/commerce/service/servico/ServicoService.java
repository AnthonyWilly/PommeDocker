package com.ufcg.psoft.commerce.service.servico;
import com.ufcg.psoft.commerce.dto.ServicoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ServicoResponseDTO;
import java.util.List;

public interface ServicoService {

    ServicoResponseDTO alterar(Long idEmpresa,Long id, ServicoPostPutRequestDTO servicoPostPutRequestDTO, String codigoAcesso);

    List<ServicoResponseDTO> listar(Long idEmpresa);

    ServicoResponseDTO recuperar(Long idEmpresa, Long id);

    ServicoResponseDTO criar(ServicoPostPutRequestDTO servicoPostPutRequestDTO, String codigoAcesso, Long idEmpresa);

    void remover(Long idEmpresa, Long id, String codigoAcesso);

}


