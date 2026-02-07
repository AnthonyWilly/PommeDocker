package com.ufcg.psoft.commerce.model;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("Pix")
public class PagamentoPix implements Pagamento {

    private static final BigDecimal PERCENTUAL_DESCONTO = BigDecimal.valueOf(0.05);

    @Override
    public BigDecimal aplicarDesconto(BigDecimal valorTotal) {
        throw new UnsupportedOperationException("Calculo de desconto para pix nao implementado");
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
