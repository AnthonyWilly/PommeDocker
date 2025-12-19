package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.model.Empresa;
import java.util.List;

public interface EmpresaService {
    Empresa criar(EmpresaPostPutRequestDTO dto);
    Empresa recuperar(Long id);
    List<Empresa> listar();
    Empresa alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO dto);
    void remover(Long id, String codigoAcesso, String senhaAdmin);
}