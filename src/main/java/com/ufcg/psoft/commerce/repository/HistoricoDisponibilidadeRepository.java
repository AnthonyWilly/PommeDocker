package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.HistoricoDisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoDisponibilidadeRepository extends JpaRepository<HistoricoDisponibilidade, Long> {
    List<HistoricoDisponibilidade> findAllByTecnicoIdOrderByDataHoraDesc(Long tecnicoId);
}
