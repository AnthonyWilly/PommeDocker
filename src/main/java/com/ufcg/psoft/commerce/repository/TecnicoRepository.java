package com.ufcg.psoft.commerce.repository;

import java.util.List;
import java.util.Optional;

import com.ufcg.psoft.commerce.model.StatusTecnico;
import com.ufcg.psoft.commerce.model.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ufcg.psoft.commerce.model.Tecnico;

public interface TecnicoRepository extends JpaRepository<Tecnico, Long> {

    List<Tecnico> findByNomeContaining(String nome);

    @Query("SELECT t FROM Tecnico t JOIN t.empresasAprovadoras e WHERE " +
            "e.id = :empresaId AND t.statusDisponibilidade = 'ATIVO' " +
            "ORDER BY t.dataUltimaMudancaDisponibilidade ASC LIMIT 1")
    Optional<Tecnico> findTecnicoAtivoMaisTempoParaEmpresa(@Param("empresaId") Long empresaId);

    long countByStatusTecnico(StatusTecnico status);
}
