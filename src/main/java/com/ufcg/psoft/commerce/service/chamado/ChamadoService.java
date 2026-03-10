package com.ufcg.psoft.commerce.service.chamado;

import com.ufcg.psoft.commerce.dto.ChamadoPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import com.ufcg.psoft.commerce.model.ChamadoStatus;

import java.util.List;

public interface ChamadoService {
    ChamadoResponseDTO criarChamado(Long clienteId, String codigoAcesso, ChamadoPostPutRequestDTO dto);
    ChamadoResponseDTO confirmarPagamento(Long chamadoId, String codigoAcesso, String metodoPagamento);
    void removerChamado(Long chamadoId, String codigoAcesso);
    List<ChamadoResponseDTO> listarChamados(Long clienteId, String codigoAcesso);
    ChamadoResponseDTO buscarChamado(Long chamadoId);

    ChamadoResponseDTO buscarChamadoPorCliente(Long chamadoId, Long idCliente,  String codigoAcesso);

    List<ChamadoResponseDTO> listarChamadosCliente(Long idCliente,  String codigoAcesso);

    List<ChamadoResponseDTO> listarChamadosClientePorStatus(Long idCliente, ChamadoStatus chamadoStatus, String codigoAcesso);
    void cancelar(Long id, Long idCliente, String codigoAcesso);
    ChamadoResponseDTO confirmarConclusao(Long clienteId, String codigoAcesso, Long chamadoId);
}