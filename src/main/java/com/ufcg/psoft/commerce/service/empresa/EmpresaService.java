package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.dto.EmpresaResponseDTO;
import java.util.List;

public interface EmpresaService {
    EmpresaResponseDTO cadastrar(EmpresaPostPutRequestDTO dto);
    EmpresaResponseDTO recuperar(Long id);
    List<EmpresaResponseDTO> listar();
    EmpresaResponseDTO alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO dto);
    void remover(Long id, String codigoAcesso, String senhaAdmin);
}