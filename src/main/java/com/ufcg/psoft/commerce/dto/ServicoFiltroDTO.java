package com.ufcg.psoft.commerce.dto;

import com.ufcg.psoft.commerce.model.TipoServico;
import com.ufcg.psoft.commerce.model.Urgencia;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicoFiltroDTO {
    private TipoServico tipo;
    private Long empresaId;
    private Urgencia urgencia;
    private Double precoMin;
    private Double precoMax;
}
