package com.ufcg.psoft.commerce.model;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("Pix")
public class PagamentoPix implements PagamentoStrategy {

    private static final BigDecimal PERCENTUAL_DESCONTO = BigDecimal.valueOf(0.05);

    @Override
    public BigDecimal aplicarDesconto(BigDecimal valorTotal) {
        if (valorTotal == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal fatorDesconto = BigDecimal.ONE.subtract(PERCENTUAL_DESCONTO);
        return valorTotal.multiply(fatorDesconto)
                .setScale(2, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getPercentualDesconto() {
        return PERCENTUAL_DESCONTO;
    }

    @Override
    public String getMetodo() {
        return "Pix";
    }
}
