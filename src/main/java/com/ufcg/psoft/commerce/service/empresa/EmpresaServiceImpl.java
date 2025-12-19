package com.ufcg.psoft.commerce.service.empresa;

import com.ufcg.psoft.commerce.dto.EmpresaPostPutRequestDTO;
import com.ufcg.psoft.commerce.exception.CodigoDeAcessoInvalidoException;
import com.ufcg.psoft.commerce.exception.EmpresaJaCadastradaException;
import com.ufcg.psoft.commerce.exception.EmpresaNaoExisteException;
import com.ufcg.psoft.commerce.exception.SenhaInvalidaException;
import com.ufcg.psoft.commerce.model.Empresa;
import com.ufcg.psoft.commerce.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    private static final String SENHA_ADMIN_PADRAO = "admin123";

    @Override
    public Empresa criar(EmpresaPostPutRequestDTO dto) {
        validarSenhaAdmin(dto.getSenhaAdmin());

        if (empresaRepository.findByCnpj(dto.getCnpj()).isPresent()) {
            throw new EmpresaJaCadastradaException();
        }

        Empresa empresa = Empresa.builder()
                .nome(dto.getNome())
                .cnpj(dto.getCnpj())
                .codigoAcesso(dto.getCodigoAcesso())
                .build();

        return empresaRepository.save(empresa);
    }

    @Override
    public Empresa recuperar(Long id) {
        return empresaRepository.findById(id)
                .orElseThrow(EmpresaNaoExisteException::new);
    }

    @Override
    public List<Empresa> listar() {
        return empresaRepository.findAll();
    }

    @Override
    public Empresa alterar(Long id, String codigoAcesso, EmpresaPostPutRequestDTO dto) {
        validarSenhaAdmin(dto.getSenhaAdmin());

        Empresa empresa = recuperar(id);
        validarCodigoAcesso(empresa, codigoAcesso);

        empresa.setNome(dto.getNome());
        empresa.setCnpj(dto.getCnpj());

        return empresaRepository.save(empresa);
    }

    @Override
    public void remover(Long id, String codigoAcesso, String senhaAdmin) {
        validarSenhaAdmin(senhaAdmin);

        Empresa empresa = recuperar(id);
        validarCodigoAcesso(empresa, codigoAcesso);

        empresaRepository.delete(empresa);
    }

    private void validarSenhaAdmin(String senhaAdmin) {
        if (!SENHA_ADMIN_PADRAO.equals(senhaAdmin)) {
            throw new SenhaInvalidaException();
        }
    }

    private void validarCodigoAcesso(Empresa empresa, String codigoAcesso) {
        if (!empresa.getCodigoAcesso().equals(codigoAcesso)) {
            throw new CodigoDeAcessoInvalidoException();
        }
    }
}