package com.ufcg.psoft.commerce.model;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("Credito")
public class PagamentoCredito implements PagamentoStrategy {

    private static final BigDecimal PERCENTUAL_DESCONTO = BigDecimal.ZERO;

    @Override
    public BigDecimal aplicarDesconto(BigDecimal valorTotal) {
        if (valorTotal == null) {
            return BigDecimal.ZERO;
        }

        return valorTotal.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getPercentualDesconto() {
        return PERCENTUAL_DESCONTO;
    }

    @Override
    public String getMetodo() {
        return "Credito";
    }
}
