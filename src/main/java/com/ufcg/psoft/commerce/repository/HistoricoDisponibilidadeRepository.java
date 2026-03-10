package com.ufcg.psoft.commerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ufcg.psoft.commerce.model.HistoricoDisponibilidade;

public interface HistoricoDisponibilidadeRepository extends JpaRepository<HistoricoDisponibilidade, Long> {
    List<HistoricoDisponibilidade> findAllByTecnicoIdOrderByDataHoraDesc(Long tecnicoId);
}