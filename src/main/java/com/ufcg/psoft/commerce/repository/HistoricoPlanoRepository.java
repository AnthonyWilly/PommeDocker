package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.HistoricoPlano;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoPlanoRepository extends JpaRepository<HistoricoPlano, Long> {
    List<HistoricoPlano> findAllByIdClienteOrderByDataDesc(Long idCliente);
}
