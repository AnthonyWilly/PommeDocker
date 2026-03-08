package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.Chamado;
import com.ufcg.psoft.commerce.model.ChamadoStatus;
import com.ufcg.psoft.commerce.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
	void deleteAllByEmpresa(Empresa empresa);

	Optional<Chamado> findByIdAndClienteId(Long id, Long clienteId);

	// US20: "Visualizar seu próprio histórico" -> APENAS ID do CLIENTE
	@Query("SELECT c FROM Chamado c WHERE c.cliente.id = :clienteId " +
			"ORDER BY CASE WHEN c.status = com.ufcg.psoft.commerce.model.ChamadoStatus.CONCLUIDO THEN 1 ELSE 0 END ASC, " +
			"c.dataCriacao DESC")
	List<Chamado> findByClienteIdOrderByStatusEData(Long clienteId);
	List<Chamado> findByClienteIdAndStatusOrderByDataCriacaoDesc(Long clienteId, ChamadoStatus status);
}

