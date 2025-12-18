package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.HistoricoPlano;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoPlanoRepository extends JpaRepository<HistoricoPlano, Long> {

    List<HistoricoPlano> findAllByIdClienteOrderByDataDesc(Long id);
    
}
