package com.ufcg.psoft.commerce.model;

public interface ChamadoEstado {
    void avancarEstado(Chamado chamado);
    String getNome();
    void avancar(Chamado chamado);          
}