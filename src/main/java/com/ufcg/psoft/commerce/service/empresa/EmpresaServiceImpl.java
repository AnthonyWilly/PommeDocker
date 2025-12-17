package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Override
    public Empresa cadastrar(EmpresaPostPutRequestDTO dto) { return null; }

    @Override
    public Empresa recuperar(Long id) { return null; }

    @Override
    public List<Empresa> listar() { return null; }

    @Override
    public Empresa alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO dto) { return null; }

    @Override
    public void remover(Long id, String codigoAcesso, String senhaAdmin) { }
}