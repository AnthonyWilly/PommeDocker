package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.Plano;
import com.ufcg.psoft.commerce.model.Servico;
import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByEmpresa_Id(Long empresaId);
    
    @Query("SELECT s FROM Servico s WHERE " +
            "s.disponivel AND " +
            "s.plano IN :planosPermitidos AND " + 
            "(:tipo IS NULL OR s.tipo = :tipo) AND " +
            "(:empresaId IS NULL OR s.empresa.id = :empresaId) AND " +
            "(:urgencia IS NULL OR s.urgencia = :urgencia) AND " +
            "(:precoMin IS NULL OR s.preco >= :precoMin) AND " +
            "(:precoMax IS NULL OR s.preco <= :precoMax)")
    List<Servico> findAllComFiltros(
            @Param("tipo") TipoServico tipo, 
            @Param("empresaId") Long empresaId,
            @Param("urgencia") Urgencia urgencia,
            @Param("precoMin") Double precoMin,
            @Param("precoMax") Double precoMax,
            @Param("planosPermitidos") List<Plano> planosPermitidos
    );
}
