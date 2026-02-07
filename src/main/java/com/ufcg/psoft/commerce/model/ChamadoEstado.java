package com.ufcg.psoft.commerce.model;

public interface ChamadoEstado {
    String getStatus();
    void confirmarPagamento(Chamado chamado);
}