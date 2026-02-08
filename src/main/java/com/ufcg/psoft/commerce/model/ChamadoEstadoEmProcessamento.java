package com.ufcg.psoft.commerce.model;

public class ChamadoEstadoEmProcessamento implements ChamadoEstado {
    
    @Override
    public void confirmarPagamento(Chamado chamado) {
        System.out.println("O pagamento já foi confirmado anteriormente.");
    }

    @Override
    public String getNome() {
        return "EM_PROCESSAMENTO";
    }
}