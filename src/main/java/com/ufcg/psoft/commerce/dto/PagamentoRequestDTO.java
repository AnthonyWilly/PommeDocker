package com.ufcg.psoft.commerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoRequestDTO {
    private BigDecimal valorTotal; //isso vem direto do serviço associado ao chamado
    private String metodoPagamento;
}
