package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.EmpresaNaoExisteException;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Empresa cadastrar(EmpresaPostPutRequestDTO empresaDTO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Empresa recuperar(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Empresa alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO empresaDTO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remover(Long id, String codigoAcesso, String senhaAdmin) {
        // TODO Auto-generated method stub
    }
}
