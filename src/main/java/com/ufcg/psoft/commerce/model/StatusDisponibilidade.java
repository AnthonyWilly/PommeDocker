package com.ufcg.psoft.commerce.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusDisponibilidade {
    ATIVO(true),
    DESCANSO(false),
    OCUPADO(false);

    private final boolean disponivel;
}
