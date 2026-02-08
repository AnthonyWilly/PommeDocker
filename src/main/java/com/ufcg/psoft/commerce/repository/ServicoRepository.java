package com.ufcg.psoft.commerce.repository;

import com.ufcg.psoft.commerce.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
    List<Servico> findByEmpresa_Id(Long empresaId);
    
    @Query("SELECT s FROM Servico s WHERE " +
           "s.idPlano IN :planosPermitidos AND " + 
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
            @Param("planosPermitidos") List<String> planosPermitidos
    );
}
