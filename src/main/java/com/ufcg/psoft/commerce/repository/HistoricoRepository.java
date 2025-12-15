package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistoricoRepository extends JpaRepository<Historico, Long> {
    List<Historico> findByIdCliente(Long idCliente);
}
