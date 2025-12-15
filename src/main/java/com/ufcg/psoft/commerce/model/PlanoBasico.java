package com.ufcg.psoft.commerce.model;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PlanoBasico implements Plano {
    public static final String ID = "BASICO";

    public String getPlano() { return ID; }
    public BigDecimal getPreco() { return new BigDecimal("10.00"); }
}

