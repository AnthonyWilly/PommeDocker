package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.Chamado;
import com.ufcg.psoft.commerce.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

	void deleteAllByEmpresa(Empresa empresa);
}