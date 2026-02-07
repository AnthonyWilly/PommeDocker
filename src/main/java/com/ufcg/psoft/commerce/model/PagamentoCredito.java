package com.ufcg.psoft.commerce.model;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("Credito")
public class PagamentoCredito implements Pagamento {

    private static final BigDecimal PERCENTUAL_DESCONTO = BigDecimal.ZERO;

    @Override
    public BigDecimal aplicarDesconto(BigDecimal valorTotal) {
        throw new UnsupportedOperationException("Calculo de desconto para credito nao implementado");
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
