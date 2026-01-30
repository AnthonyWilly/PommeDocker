package com.ufcg.psoft.commerce.model;

import java.math.BigDecimal;

public interface Pagamento {

    BigDecimal aplicarDesconto(BigDecimal valorTotal);

    BigDecimal getPercentualDesconto();

    String getMetodo();
}
