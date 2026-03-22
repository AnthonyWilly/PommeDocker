package com.ufcg.psoft.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Plano {
    BASICO("Basico",15.50),
    PREMIUM("Premium",30.0) ;

    private final String tipoPlano;
    private final double preco;
}
