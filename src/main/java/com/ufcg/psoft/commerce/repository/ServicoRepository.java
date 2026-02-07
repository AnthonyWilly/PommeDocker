package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByIdEmpresa(Long idServico);
}
