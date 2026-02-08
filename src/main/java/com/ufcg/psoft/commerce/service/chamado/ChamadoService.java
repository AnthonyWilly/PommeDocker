package com.ufcg.psoft.commerce.service.chamado;

import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import java.util.List;

public interface ChamadoService {
    ChamadoResponseDTO criarChamado(Long clienteId, String codigoAcesso, ChamadoPostPutRequestDTO dto);
    ChamadoResponseDTO confirmarPagamento(Long chamadoId, String codigoAcesso, String metodoPagamento);
    void removerChamado(Long chamadoId, String codigoAcesso);
    List<ChamadoResponseDTO> listarChamados(Long clienteId, String codigoAcesso);
    ChamadoResponseDTO buscarChamado(Long chamadoId);
}