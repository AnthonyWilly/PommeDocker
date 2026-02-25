package com.ufcg.psoft.commerce.model;

public interface ChamadoEstado {
    void confirmarPagamento(Chamado chamado);

    String getNome();
    void avancar(Chamado chamado);    
    
    void atribuirTecnico(Chamado chamado, Tecnico tecnico);
}