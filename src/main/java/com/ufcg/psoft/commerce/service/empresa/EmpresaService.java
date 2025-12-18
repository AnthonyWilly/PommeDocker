package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.model.Empresa;

public interface EmpresaService {
    Empresa cadastrar(EmpresaPostPutRequestDTO empresaDTO);
    Empresa recuperar(Long id);
    Empresa alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO empresaDTO);
    void remover(Long id, String codigoAcesso, String senhaAdmin);
}
