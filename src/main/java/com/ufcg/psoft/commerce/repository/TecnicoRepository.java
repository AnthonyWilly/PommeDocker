package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.StatusTecnico;
import com.ufcg.psoft.commerce.model.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {
    List<Tecnico> findByNomeContaining(String nome);

    long countByStatusTecnico(StatusTecnico status);
}
