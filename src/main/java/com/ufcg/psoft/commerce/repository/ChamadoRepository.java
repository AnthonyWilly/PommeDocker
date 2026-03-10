package com.ufcg.psoft.commerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ufcg.psoft.commerce.model.Chamado;
import com.ufcg.psoft.commerce.model.Empresa;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
	void deleteAllByEmpresa(Empresa empresa);

	Optional<Chamado> findByIdAndClienteId(Long id, Long clienteId);

	@Query("SELECT c FROM Chamado c WHERE c.cliente.id = :clienteId " +
			"ORDER BY CASE WHEN c.status = 'CONCLUIDO' THEN 1 ELSE 0 END ASC, " +
			"c.dataCriacao DESC")
	List<Chamado> findByClienteIdOrderByStatusEData(Long clienteId);

	List<Chamado> findByClienteIdAndStatusOrderByDataCriacaoDesc(Long clienteId, String status);

	@Query("SELECT c FROM Chamado c WHERE c.empresa.id = :empresaId" +
			"AND c.status = 'AGUARDANDO_TECNICO' ORDER BY c.dataCriacao ASC LIMIT 1")
    Optional<Chamado> findChamadoMaisAntigoAguardando(@Param("empresaId") Long empresaId);

}