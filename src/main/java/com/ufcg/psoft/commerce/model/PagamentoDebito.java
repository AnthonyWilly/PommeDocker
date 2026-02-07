package com.ufcg.psoft.commerce.model;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("Debito")
public class PagamentoDebito implements Pagamento {

    private static final BigDecimal PERCENTUAL_DESCONTO = BigDecimal.valueOf(0.025);

    @Override
    public BigDecimal aplicarDesconto(BigDecimal valorTotal) {
        throw new UnsupportedOperationException("Calculo de desconto para debito nao implementado");
    }

    @Override
    public BigDecimal getPercentualDesconto() {
        return PERCENTUAL_DESCONTO;
    }

    @Override
    public String getMetodo() {
        return "Debito";
    }
}
