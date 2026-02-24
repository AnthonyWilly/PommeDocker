package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import com.ufcg.psoft.commerce.dto.PagamentoRequestDTO;
import com.ufcg.psoft.commerce.dto.PagamentoResponseDTO;
import com.ufcg.psoft.commerce.dto.ChamadoResponseDTO;
import java.util.List;

public interface EmpresaService {
    EmpresaResponseDTO cadastrar(EmpresaPostPutRequestDTO dto);
    EmpresaResponseDTO recuperar(Long id);
    List<EmpresaResponseDTO> listar();
    EmpresaResponseDTO alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO dto);
    void remover(Long id, String codigoAcesso, String senhaAdmin);
    void aprovarTecnico(Long empresaId, Long tecnicoId, String codigoAcesso);
    void rejeitarTecnico(Long empresaId, Long tecnicoId, String codigoAcesso);
    

    PagamentoResponseDTO confirmarPagamento(Long empresaId, Long chamadoId, String codigoAcesso, PagamentoRequestDTO pagamentoRequestDTO);
    ChamadoResponseDTO avancarStatus(Long empresaId, String codigoAcesso, Long chamadoId);
    ChamadoResponseDTO atribuirTecnico(Long empresaId, String codigoAcesso, Long chamadoId, Long tecnicoId);
}